package com.amazon.apayjiramanagementservice.handler.alarmHandler.serviceAlarmIHandlerImpl;

import com.amazon.apayjiramanagementservice.constants.ThirdParty.Fulfillers;
import com.amazon.apayjiramanagementservice.constants.elastisearch.ElasticsearchQueryParams;
import com.amazon.apayjiramanagementservice.handler.alarmHandler.serviceAlarmHandler.ServiceAlarmHandler;
import com.amazon.apayjiramanagementservice.model.alarmEvents.CloudwatchAlarmEvent;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * https://quip-amazon.com/tMbrAggoY3tF/SOP-for-BizOps-to-cut-JIRAs-to-3P.
 */
@Log4j2
public final class OrderSuccessRateHandler implements ServiceAlarmHandler {

    private static final String MERCHANT_REF_NO = "MerchantRefNo";

    private static final String SOURCE_REF_NO = "source_ref_no";

    private static OrderSuccessRateHandler instance;

    private final int maxLogsToQuery = 4;

    private OrderSuccessRateHandler() {

    }

    public static OrderSuccessRateHandler getInstance() {
        synchronized (OrderSuccessRateHandler.class) {
            if (instance == null) {
                instance = new OrderSuccessRateHandler();
            }
            return instance;
        }
    }

    @Override
    public void handleEvent(CloudwatchAlarmEvent cloudwatchAlarmEvent) throws IOException {
            if (!cloudwatchAlarmEvent.getNewStateValue().equals("ALARM")) {
                return; // alarm state != ALARM  (no action required)
            }

            log.info("Alarmstate: {} | AlarmName: {} | AlarmDescription: {}", cloudwatchAlarmEvent.getNewStateValue(),
                    cloudwatchAlarmEvent.getAlarmName(), cloudwatchAlarmEvent.getAlarmDescription());

            // Try primary query first
            boolean primaryQuerySuccess = handlePrimaryQuery();
            // If primary query didn't find results, try fallback query
            if (!primaryQuerySuccess) {
                handleFallbackQuery();
            }
    }

    // Primary query method - handles MAKE_PAYMENT_RESPONSE queries
    private boolean handlePrimaryQuery() throws IOException {
        List<String> listOfRequestIds = new ArrayList<>();
        Map<String, String> mapOfRequestIdToTrackingId = new HashMap<>();
        Map<String, String> mapOfRequestIdToMakePaymentResponse = new HashMap<>();
        Map<String, String> mapOfMerchantRefNoToPaymentRequest = new HashMap<>();
        Map<String, String> mapOfSourceRefNoToPaymentRequest = new HashMap<>();
        Map<String, String> mapOfMerchantRefNoToPaymentResponse = new HashMap<>();
        Map<String, String> mapOfSourceRefNoToPaymentResponse = new HashMap<>();

        // Primary query: get failed MAKE_PAYMENT_RESPONSES
        SearchResponse searchResponseForRequestIDs = searchKibanaLogsByPatternForLastHour(
                ElasticsearchQueryParams.MESSAGE.getValue() + ":\"*MAKE_PAYMENT_RESPONSE*\"" + " AND "
                        + ElasticsearchQueryParams.MESSAGE.getValue() + ":\"*failed*\"", maxLogsToQuery);

        boolean foundMakePaymentLogs = false;

        //collect requestIDs
        for (SearchHit hit : searchResponseForRequestIDs.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String requestId = (String) sourceAsMap.get(ElasticsearchQueryParams.REQUEST_ID.getValue());
            String message = (String) sourceAsMap.get(ElasticsearchQueryParams.MESSAGE.getValue());
            listOfRequestIds.add(requestId);
            mapOfRequestIdToMakePaymentResponse.put(requestId, message);
        }

        //if no logs found, trigger fallback
        if (listOfRequestIds.isEmpty()) {
            return false;
        }

        //extract the merchantRefNo and source_ref_no
         extractReferenceNumbers(searchResponseForRequestIDs,
                mapOfMerchantRefNoToPaymentResponse, mapOfSourceRefNoToPaymentResponse);


        // Collect corresponding MAKE_PAYMENT_REQUEST logs
        collectPaymentRequestLogs(mapOfMerchantRefNoToPaymentResponse.keySet(),
                mapOfMerchantRefNoToPaymentRequest);
        collectPaymentRequestLogs(mapOfSourceRefNoToPaymentResponse.keySet(),
                mapOfSourceRefNoToPaymentRequest);

        //Get EuronetRefNo or BD-Traceid (trackingId for each RequestId)
        for (String requestId: listOfRequestIds) {
            Fulfillers fulfiller = getFulfiller(mapOfRequestIdToMakePaymentResponse.get(requestId));
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


        // Create JIRA tickets for primary query results
        if (!mapOfMerchantRefNoToPaymentRequest.isEmpty()) {
            createJiraTicket(listOfRequestIds,
                    mapOfMerchantRefNoToPaymentRequest, mapOfMerchantRefNoToPaymentResponse,
                    mapOfRequestIdToTrackingId,
                    "Drop in order success rate");
        }
        if (!mapOfSourceRefNoToPaymentRequest.isEmpty()) {
            createJiraTicket(listOfRequestIds,
                    mapOfSourceRefNoToPaymentRequest, mapOfSourceRefNoToPaymentResponse,
                    mapOfRequestIdToTrackingId,
                    "Drop in order success rate");
        }

        return true;
    }

    // Fallback query method - handles LIST_PAYMENT_RESPONSE queries
    private boolean handleFallbackQuery() throws IOException {
        log.info("No MAKE_PAYMENT_RESPONSE logs found, falling back to LIST_PAYMENT_RESPONSE query");
        List<String> listOfRequestIds = new ArrayList<>();
        Map<String, String> mapOfRequestIdToTrackingId = new HashMap<>();
        Map<String, String> mapOfRequestIdToMakePaymentRequest = new HashMap<>();
        Map<String, String> mapOfMerchantRefNoToPaymentRequest = new HashMap<>();
        Map<String, String> mapOfSourceRefNoToPaymentRequest = new HashMap<>();
        Map<String, String> mapOfMerchantRefNoToPaymentResponse = new HashMap<>();
        Map<String, String> mapOfSourceRefNoToPaymentResponse = new HashMap<>();

        //Get LIST_PAYMENT_RESPONSE logs for last 1 hour
        SearchResponse listPaymentResponseSearch = searchKibanaLogsByPatternForLastHour(
                ElasticsearchQueryParams.MESSAGE.getValue() + ":\"*LIST_PAYMENT_RESPONSE*\"" + " AND "
                        + ElasticsearchQueryParams.MESSAGE.getValue() + ":\"*failed*\"", maxLogsToQuery);

        for (SearchHit hit : listPaymentResponseSearch.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String requestId = (String) sourceAsMap.get(ElasticsearchQueryParams.REQUEST_ID.getValue());
            String message = (String) sourceAsMap.get(ElasticsearchQueryParams.MESSAGE.getValue());
            listOfRequestIds.add(requestId);
            mapOfRequestIdToMakePaymentRequest.put(requestId, message);
        }

        // Process LIST_PAYMENT_RESPONSE logs to find corresponding MAKE_PAYMENT logs
        processListPaymentResponseLogs(listPaymentResponseSearch,
                mapOfMerchantRefNoToPaymentRequest, mapOfSourceRefNoToPaymentRequest,
                mapOfMerchantRefNoToPaymentResponse, mapOfSourceRefNoToPaymentResponse);

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


        // Create JIRA tickets
        if (!mapOfMerchantRefNoToPaymentRequest.isEmpty()) {
            createJiraTicket(listOfRequestIds,
                    mapOfMerchantRefNoToPaymentRequest, mapOfMerchantRefNoToPaymentResponse,
                    mapOfRequestIdToTrackingId,
                    "Drop in order success rate", Fulfillers.EURONET);
        }
        if (!mapOfSourceRefNoToPaymentRequest.isEmpty()) {
            createJiraTicket(listOfRequestIds,
                    mapOfSourceRefNoToPaymentRequest, mapOfSourceRefNoToPaymentResponse,
                    mapOfRequestIdToTrackingId,
                    "Drop in order success rate", Fulfillers.BILLDESK);
        }

        return true;
    }


    //method to get MerchantRefNo from logs
    private String getMerchantRefNo(String message) {
            Pattern pattern = Pattern.compile("\"MerchantRefNo\":\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
    }

    //method to get source_ref_no from logs
    private String getSourceRefNo(String message) {
            Pattern pattern = Pattern.compile("\"source_ref_no\":\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
    }

    //method to get orderId from logs
    private String getOrderId(String message) {
            Pattern pattern = Pattern.compile("orderId=([^,\\)]+)");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
    }


    // Generic method to search logs by identifier and message pattern
    private SearchResponse searchLogsByIdentifierAndMessage(String identifier, String messagePattern)
            throws IOException {
        return searchKibanaLogsByPatternAndSize(ElasticsearchQueryParams.MESSAGE.getValue()
                + ":\"*" + messagePattern + "*\""
                + " AND " + ElasticsearchQueryParams.MESSAGE.getValue() + ":\"*" + identifier + "*\"", 1);
    }

    // Generic method to extract reference numbers (MerchantRefNo/source_ref_no) from search response
    private boolean extractReferenceNumbers(SearchResponse searchResponse,
                                          Map<String, String> merchantRefNoMap,
                                          Map<String, String> sourceRefNoMap) {
        boolean foundLogs = false;
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            foundLogs = true;
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String message = (String) sourceAsMap.get(ElasticsearchQueryParams.MESSAGE.getValue());

            if (message.contains(MERCHANT_REF_NO)) {
                String merchantRefNo = getMerchantRefNo(message);
                merchantRefNoMap.put(merchantRefNo, message);
            }
            if (message.contains(SOURCE_REF_NO)) {
                String sourceRefNo = getSourceRefNo(message);
                sourceRefNoMap.put(sourceRefNo, message);
            }
        }
        return foundLogs;
    }

    // Generic method to collect payment request logs for given reference numbers
    //message:"*MAKE_PAYMENT_REQUEST*" AND message:"*405-0343766-8955569*"
    private void collectPaymentRequestLogs(Iterable<String> referenceNumbers,
                                         Map<String, String> resultMap) throws IOException {
        for (String refNumber : referenceNumbers) {
            SearchResponse searchResponse = searchKibanaLogsByPatternAndSize(
                    ElasticsearchQueryParams.MESSAGE.getValue()
                            + ":\"*" + refNumber + "*\"" + " AND "
                            + ElasticsearchQueryParams.MESSAGE.getValue() + ":\"*" + "MAKE_PAYMENT_REQUEST" + "*\"", 1);

            for (SearchHit hit : searchResponse.getHits().getHits()) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                String message = (String) sourceAsMap.get(ElasticsearchQueryParams.MESSAGE.getValue());
                resultMap.put(refNumber, message);
            }
        }
    }

    // Method to process LIST_PAYMENT_RESPONSE logs and collect corresponding MAKE_PAYMENT logs
    private void processListPaymentResponseLogs(SearchResponse listPaymentResponseSearch,
                                              Map<String, String> mapOfMerchantRefNoToPaymentRequest,
                                              Map<String, String> mapOfSourceRefNoToPaymentRequest,
                                              Map<String, String> mapOfMerchantRefNoToPaymentResponse,
                                              Map<String, String> mapOfSourceRefNoToPaymentResponse)
            throws IOException {
        for (SearchHit hit : listPaymentResponseSearch.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            // Get RequestId from logs
            String requestId = (String) sourceAsMap.get(ElasticsearchQueryParams.REQUEST_ID.getValue());
            if (requestId != null) {
                // Query for LIST_PAYMENT_REQUEST using RequestId
                SearchResponse listPaymentRequestSearch = searchKibanaLogsByPatternAndSize(
                        ElasticsearchQueryParams.REQUEST_ID.getValue()
                                + ":\"" + requestId + "\"" + " AND "
                                + ElasticsearchQueryParams.MESSAGE.getValue()
                                + ":\"*" + "LIST_PAYMENT_REQUEST" + "*\"", 1);

                for (SearchHit requestHit : listPaymentRequestSearch.getHits().getHits()) {
                    Map<String, Object> requestSourceAsMap = requestHit.getSourceAsMap();
                    String requestMessage = (String) requestSourceAsMap.
                            get(ElasticsearchQueryParams.MESSAGE.getValue());
                    // Extract orderId from LIST_PAYMENT_REQUEST
                    String orderId = getOrderId(requestMessage);

                    // Query for MAKE_PAYMENT_RESPONSE using orderId
                    // message:"*MAKE_PAYMENT_RESPONSE*" AND message:"*407-6808582-3935549*"
                    SearchResponse makePaymentResponseSearch = searchKibanaLogsByPatternAndSize(
                            ElasticsearchQueryParams.MESSAGE.getValue()
                                    + ":\"*" + orderId + "*\"" + " AND "
                                    + ElasticsearchQueryParams.MESSAGE.getValue()
                                    + ":\"*" + "MAKE_PAYMENT_RESPONSE" + "*\"", 1);

                    // Extract reference numbers from MAKE_PAYMENT_RESPONSE logs
                    extractReferenceNumbers(makePaymentResponseSearch,
                            mapOfMerchantRefNoToPaymentResponse, mapOfSourceRefNoToPaymentResponse);

                    // Collect corresponding MAKE_PAYMENT_REQUEST logs
                    collectPaymentRequestLogs(mapOfMerchantRefNoToPaymentResponse.keySet(),
                            mapOfMerchantRefNoToPaymentRequest);
                    collectPaymentRequestLogs(mapOfSourceRefNoToPaymentResponse.keySet(),
                            mapOfSourceRefNoToPaymentRequest);

                }
            }
        }
    }

}
