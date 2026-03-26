package com.amazon.apayjiramanagementservice.handler.alarmHandler.serviceAlarmIHandlerImpl;

import com.amazon.apayjiramanagementservice.constants.ThirdParty.Fulfillers;
import com.amazon.apayjiramanagementservice.constants.elastisearch.ElasticsearchQueryParams;
import com.amazon.apayjiramanagementservice.handler.alarmHandler.serviceAlarmHandler.ServiceAlarmHandler;
import com.amazon.apayjiramanagementservice.model.alarmEvents.CloudwatchAlarmEvent;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * https://quip-amazon.com/tMbrAggoY3tF/SOP-for-BizOps-to-cut-JIRAs-to-3P
 */

@Log4j2
public final class HerdWorkStorageOrderPendingSILHandler implements ServiceAlarmHandler {

    private static HerdWorkStorageOrderPendingSILHandler instance;
    private final int maxLogsToQuery = 4;

    private HerdWorkStorageOrderPendingSILHandler() {

    }

    public static HerdWorkStorageOrderPendingSILHandler getInstance() {
        synchronized (HerdWorkStorageOrderPendingSILHandler.class) {
            if (instance == null) {
                instance = new HerdWorkStorageOrderPendingSILHandler();
            }
            return instance;
        }
    }

    @Override
    public void handleEvent(CloudwatchAlarmEvent cloudwatchAlarmEvent) throws Exception {

            if (!cloudwatchAlarmEvent.getNewStateValue().equals("ALARM")) {
                return;  // alarm state != ALARM  (no action required)
            }
            log.info("Alarmstate: {} | AlarmName: {} | AlarmDescription: {}", cloudwatchAlarmEvent.getNewStateValue(),
                    cloudwatchAlarmEvent.getAlarmName(), cloudwatchAlarmEvent.getAlarmDescription());

            List<String> listOfRequestIds = new ArrayList<>();
            Map<String, String> mapOfRequestIdToMakePaymentRequest = new HashMap<>();
            Map<String, String> mapOfRequestIdToMakePaymentResponse = new HashMap<>();
            Map<String, String> mapOfRequestIdToTrackingId = new HashMap<>();

            // Query for MAKE_PAYMENT where status=PENDING
            SearchResponse pendingResponses = searchKibanaLogsByPatternForLastHour(
                    ElasticsearchQueryParams.MESSAGE.getValue() + ":\"*MAKE_PAYMENT*\"" + " AND "
                    + ElasticsearchQueryParams.MESSAGE.getValue() + ":\"*status=PENDING*\"", maxLogsToQuery);


            for (SearchHit hit : pendingResponses.getHits().getHits()) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                String message = (String) sourceAsMap.get(ElasticsearchQueryParams.MESSAGE.getValue());
                String requestID = (String) sourceAsMap.get(ElasticsearchQueryParams.REQUEST_ID.getValue());
                listOfRequestIds.add(requestID);
                mapOfRequestIdToMakePaymentResponse.put(requestID, message);
            }

            //Query for MAKE_PAYMENT_REQUEST for each RequestId
            for (String requestId : listOfRequestIds) {
                SearchResponse makePaymentResponseForRequestID = searchKibanaLogsByPatternAndSize(
                        ElasticsearchQueryParams.REQUEST_ID.getValue() + ":\"" + requestId + "\" AND "
                        + ElasticsearchQueryParams.MESSAGE.getValue() + ":\"*MAKE_PAYMENT_REQUEST*\"", 1);

                for (SearchHit hit : makePaymentResponseForRequestID.getHits().getHits()) {
                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                    String message = (String) sourceAsMap.get(ElasticsearchQueryParams.MESSAGE.getValue());
                    mapOfRequestIdToMakePaymentRequest.put(requestId, message);
                }
            }

            //Get EuronetRefNo or BD-Traceid (trackingId for each RequestId)
            for (String requestId: listOfRequestIds) {
                Fulfillers fulfiller = getFulfiller(mapOfRequestIdToMakePaymentRequest.get(requestId));
                if (fulfiller != null) {
                    switch (fulfiller) {
                        case EURONET:
                            extractEuronetTrackingId(requestId, mapOfRequestIdToTrackingId);
                            break;
                        case BILLDESK:
                            extractBillDeskTrackingId(requestId, mapOfRequestIdToTrackingId);
                            break;
                        default:
                    }
                } else {
                    log.info("No fulfiller found for RequestId: {}", requestId);
                }
            }

            // Create JIRA tickets for each failed payment validation
            if (!listOfRequestIds.isEmpty()) {
                createJiraTicket(listOfRequestIds,
                        mapOfRequestIdToMakePaymentRequest, mapOfRequestIdToMakePaymentResponse,
                        mapOfRequestIdToTrackingId,
                        "Pending order count increase");

                sendEmailNotification("Order Pending Sil alarm jira ticket created",
                        "Automated email from JIRA Management Service " +
                                "\n List Of requestIds for Order Pending Sil alarm: " + listOfRequestIds);
            }

    }

}
