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
 * https://quip-amazon.com/tMbrAggoY3tF/SOP-for-BizOps-to-cut-JIRAs-to-3P.
 */

@Log4j2
public final class ValidateBillPaymentLatencyHandler implements ServiceAlarmHandler {

    private static ValidateBillPaymentLatencyHandler instance;

    private final int maxLogsToQuery = 4;

    private ValidateBillPaymentLatencyHandler() {

    }

    public static ValidateBillPaymentLatencyHandler getInstance() {
        synchronized (ValidateBillPaymentLatencyHandler.class) {
            if (instance == null) {
                instance = new ValidateBillPaymentLatencyHandler();
            }
            return instance;
        }
    }

    @Override
    public void handleEvent(CloudwatchAlarmEvent cloudwatchAlarmEvent) throws Exception {

            if (!cloudwatchAlarmEvent.getNewStateValue().equals("ALARM")) {
                return; // alarm state != ALARM  (no action required)
            }

            log.info("Alarmstate: {} | AlarmName: {} | AlarmDescription: {}", cloudwatchAlarmEvent.getNewStateValue(),
                    cloudwatchAlarmEvent.getAlarmName(), cloudwatchAlarmEvent.getAlarmDescription());

            List<String> listOfRequestIds = new ArrayList<>();
            Map<String, String> mapOfRequestIdToPaymentRequest = new HashMap<>();
            Map<String, String> mapOfRequestIdToPaymentResponse = new HashMap<>();
        Map<String, String> mapOfRequestIdToTrackingId = new HashMap<>();

            SearchResponse searchResponseForRequestIDs = searchKibanaLogsByPatternForLastHour(
                    ElasticsearchQueryParams.MESSAGE.getValue()
                            + ":\"*VALIDATE_PAYMENT_RESPONSE*\""
                            + " AND "
                    + ElasticsearchQueryParams.MESSAGE.getValue()
                            + ":\"*failed*\"", maxLogsToQuery);

            for (SearchHit hit : searchResponseForRequestIDs.getHits().getHits()) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                String message = (String) sourceAsMap.get(ElasticsearchQueryParams.MESSAGE.getValue());
                String requestID = (String) sourceAsMap.get(ElasticsearchQueryParams.REQUEST_ID.getValue());
                listOfRequestIds.add(requestID);
                mapOfRequestIdToPaymentResponse.put(requestID, message);
            }

            for (String requestId : listOfRequestIds) {
                log.info("ValidateBill response for request Id: {} | {} ", requestId,
                        mapOfRequestIdToPaymentResponse.get(requestId));

                SearchResponse validatePaymentResponseForRequestID = searchKibanaLogsByPatternForLastTwoHours(
                        ElasticsearchQueryParams.REQUEST_ID.getValue()
                                + ":\""
                                + requestId + "\"" + " AND "
                        + ElasticsearchQueryParams.MESSAGE.getValue()
                        + ":\"*VALIDATE_PAYMENT_REQUEST*\"", 1);

                for (SearchHit hit : validatePaymentResponseForRequestID.getHits().getHits()) {
                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                    String message = (String) sourceAsMap.get(ElasticsearchQueryParams.MESSAGE.getValue());
                    mapOfRequestIdToPaymentRequest.put(requestId, message);
                }

            }

        //Get EuronetRefNo or BD-Traceid (trackingId for each RequestId)
        for (String requestId: listOfRequestIds) {
            Fulfillers fulfiller = getFulfiller(mapOfRequestIdToPaymentRequest.get(requestId));
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
                    mapOfRequestIdToPaymentRequest, mapOfRequestIdToPaymentResponse,
                        mapOfRequestIdToTrackingId,
                    "Issue in ValidateBill API");
            }

    }


}
