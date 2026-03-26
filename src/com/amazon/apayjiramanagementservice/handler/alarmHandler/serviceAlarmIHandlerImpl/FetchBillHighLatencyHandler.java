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
public final class FetchBillHighLatencyHandler implements ServiceAlarmHandler {

    private static FetchBillHighLatencyHandler instance;

    private final int maxLogsToQuery = 4;

    private FetchBillHighLatencyHandler() {

    }

    public static FetchBillHighLatencyHandler getInstance() {
        synchronized (FetchBillHighLatencyHandler.class) {
            if (instance == null) {
                instance = new FetchBillHighLatencyHandler();
            }
            return instance;
        }
    }

    @Override
    public void handleEvent(CloudwatchAlarmEvent cloudwatchAlarmEvent) throws Exception {

            if (!cloudwatchAlarmEvent.getNewStateValue().equals("ALARM")) { // alarm state != ALARM (no action required)
                return;
            }

            log.info("Alarmstate: {} | AlarmName: {} | AlarmDescription: {}", cloudwatchAlarmEvent.getNewStateValue(),
                    cloudwatchAlarmEvent.getAlarmName(), cloudwatchAlarmEvent.getAlarmDescription());

            List<String> listOfRequestIds = new ArrayList<>();
            Map<String, String> mapOfRequestIdToFetchBillRequest = new HashMap<>();
            Map<String, String> mapOfRequestIdToFetchBillResponse = new HashMap<>();
            Map<String, String> mapOfRequestIdToTrackingId = new HashMap<>();

            SearchResponse searchResponseForRequestIDs = searchKibanaLogsByPatternForLastHour(
                    ElasticsearchQueryParams.MESSAGE.getValue()
                    + ":\"*FETCH_BILL_RESPONSE*\""
                    + " AND "
                    + ElasticsearchQueryParams.MESSAGE.getValue()
                    + ":\"*ERROR*\"", maxLogsToQuery);

            for (SearchHit hit : searchResponseForRequestIDs.getHits().getHits()) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                String message = (String) sourceAsMap.get(ElasticsearchQueryParams.MESSAGE.getValue());
                String requestID = (String) sourceAsMap.get(ElasticsearchQueryParams.REQUEST_ID.getValue());

                listOfRequestIds.add(requestID);
                mapOfRequestIdToFetchBillResponse.put(requestID, message);
            }

            //Get payment request for each requestId
            for (String requestId : listOfRequestIds) {
                SearchResponse fetchBillResponseForRequestID = searchKibanaLogsByPatternAndSize(
                        ElasticsearchQueryParams.REQUEST_ID.getValue()
                        + ":\"" + requestId + "\""
                        + " AND "
                        + ElasticsearchQueryParams.MESSAGE.getValue()
                                + ":\"*FETCH_BILL_REQUEST*\"", 1);

                mapOfRequestIdToFetchBillRequest.put(requestId, getFirstMessageFromLogs(fetchBillResponseForRequestID));
            }

            //Get EuronetRefNo or BD-Traceid (trackingId for each RequestId)
            for (String requestId: listOfRequestIds) {
                log.info("Fetchbill response for request Id: {} | {} ", requestId,
                        mapOfRequestIdToFetchBillRequest.get(requestId));
                Fulfillers fulfiller = getFulfiller(mapOfRequestIdToFetchBillRequest.get(requestId));
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

            // Create JIRA tickets for each high latency fetch bill request and
            // send email notification after successful ticket creation
            if (!listOfRequestIds.isEmpty()) {
                createJiraTicket(listOfRequestIds,
                    mapOfRequestIdToFetchBillRequest, mapOfRequestIdToFetchBillResponse,
                        mapOfRequestIdToTrackingId,
                    "High Latency Issue in FetchBill API");

                sendEmailNotification("FetchBill high latency alarm jira ticket created",
                        "Automated email from JIRA Management Service " +
                                "\n List Of requestIds for order success rate alarm: " + listOfRequestIds);

            }
    }

}
