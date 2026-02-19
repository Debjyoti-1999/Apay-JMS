package com.amazon.apayjiramanagementservice.handler.alarmHandler.serviceAlarmHandler;

import com.amazon.apayjiramanagementservice.accessor.apiImpl.ElasticsearchAccessorApiImpl;
import com.amazon.apayjiramanagementservice.accessor.apiImpl.JiraAccessorApiImpl;
import com.amazon.apayjiramanagementservice.config.elasticsearch.ElasticsearchClientConfigType;
import com.amazon.apayjiramanagementservice.config.jira.JiraClientConfigType;
import com.amazon.apayjiramanagementservice.constants.ThirdParty.Fulfillers;
import com.amazon.apayjiramanagementservice.constants.elastisearch.ElasticsearchQueryParams;
import com.amazon.apayjiramanagementservice.helper.JiraIssueFactory;
import com.amazon.apayjiramanagementservice.model.alarmEvents.CloudwatchAlarmEvent;
import com.amazon.apayjiramanagementservice.model.jira.JiraIssueRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//Sample details: https://amazon-india-payments.atlassian.net/rest/api/3/issue/HFCEN-1129
// IssueType id: Bug

public interface ServiceAlarmHandler {

    Logger log = LogManager.getLogger(ServiceAlarmHandler.class);

    String JIRA_ISSUE_TYPE = "Bug";

    int ONE_HOUR_INTERVAL_IN_MINUTES = 60; // to search logs since last 1 hour

    int TWO_HOUR_INTERVAL_IN_MINUTES = 120; //to search logs since last 2 hours

    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    String EURONET_REF_NO = "EuronetRefNo";

    String BD_TRACE_ID = "BD-Traceid";


    void handleEvent(CloudwatchAlarmEvent cloudwatchAlarmEvent) throws Exception;

    /**
     * Creates JIRA tickets for the given request IDs, grouping them by fulfiller.
     *
     * @param requestIds List of request IDs to create tickets for
     * @param requestIdToPaymentRequest Map of request ID to payment request details
     * @param requestIdToPaymentResponse Map of request ID to payment response details
     * @param ticketSummary The summary text for the JIRA ticket
     */
    default void createJiraTicket(List<String> requestIds,
                                  Map<String, String> requestIdToPaymentRequest,
                                  Map<String, String> requestIdToPaymentResponse,
                                  Map<String, String> mapOfRequestIdToTrackingId,
                                  String ticketSummary
    ) {

        JiraAccessorApiImpl jiraAccessor = new
                JiraAccessorApiImpl(JiraClientConfigType.RECHARGE_CYGNUS_JIRA_CLIENT_PROD);

        Map<Fulfillers, List<String>> fulfillerToRequestIds = new HashMap<>();
        Map<Fulfillers, StringBuilder> fulfillerToDescription = new HashMap<>();

        for (String requestId : requestIds) {
            String paymentRequest = requestIdToPaymentRequest.get(requestId);
            String paymentResponse = requestIdToPaymentResponse.get(requestId);

            //Get fulfiller from request
            Fulfillers fulfiller = getFulfiller(paymentRequest);
            if (fulfiller != null) {

                fulfillerToRequestIds.computeIfAbsent(fulfiller, k -> new ArrayList<>()).add(requestId);

                //map log details(for each fulfiller) to fulfiller
                StringBuilder description = fulfillerToDescription.computeIfAbsent(fulfiller, k -> new StringBuilder());
                description.append(mapOfRequestIdToTrackingId.get(requestId)).append("\n");
                description.append(paymentRequest).append("\n").append(paymentResponse).append("\n\n");

            } else {
                // Could not determine fulfiller for request ID
                log.info("Could not determine fulfiller for request ID: " + requestId);
            }
        }

        // Create separate JIRA tickets for each fulfiller
        for (Map.Entry<Fulfillers, List<String>> entry : fulfillerToRequestIds.entrySet()) {
            Fulfillers fulfiller = entry.getKey();
            String description = fulfillerToDescription.get(fulfiller).toString();

            JiraIssueFactory jiraIssueFactory = new JiraIssueFactory(fulfiller.getProjectID(), JIRA_ISSUE_TYPE);
            JiraIssueRequest jiraIssueRequest = jiraIssueFactory.createIssueWithPriority(
                    ticketSummary,
                    description,
                    fulfiller.getAssigneeID(),
                    fulfiller.getPriorityName(),
                    fulfiller.getLabels()
            );

            jiraAccessor.createJiraIssue(jiraIssueRequest);

        }
    }


    /**
     * Creates JIRA tickets for the given request IDs, for a particular fulfiller
     *
     * @param requestIds List of request IDs to create tickets for
     * @param requestIdToPaymentRequest Map of request ID to payment request details
     * @param requestIdToPaymentResponse Map of request ID to payment response details
     * @param ticketSummary The summary text for the JIRA ticket
     * @param fulfiller Fulfiller to create ticket for
     */

    default void createJiraTicket(List<String> requestIds,
                                  Map<String, String> requestIdToPaymentRequest,
                                  Map<String, String> requestIdToPaymentResponse,
                                  Map<String, String> mapOfRequestIdToTrackingId,
                                  String ticketSummary,
                                  Fulfillers fulfiller
    ) {

        JiraAccessorApiImpl jiraAccessor = new
                JiraAccessorApiImpl(JiraClientConfigType.RECHARGE_CYGNUS_JIRA_CLIENT_PROD);

        StringBuilder description = new StringBuilder();

        for (String requestId : requestIds) {
            String paymentRequest = requestIdToPaymentRequest.get(requestId);
            String paymentResponse = requestIdToPaymentResponse.get(requestId);
            description.append(mapOfRequestIdToTrackingId.get(requestId)).append("\n");
            description.append(paymentRequest).append("\n").append(paymentResponse).append("\n\n");
        }

        JiraIssueFactory jiraIssueFactory = new JiraIssueFactory(fulfiller.getProjectID(), JIRA_ISSUE_TYPE);
        JiraIssueRequest jiraIssueRequest = jiraIssueFactory.createIssueWithPriority(
                    ticketSummary,
                    description.toString(),
                    fulfiller.getAssigneeID(),
                    fulfiller.getPriorityName(),
                    fulfiller.getLabels()
        );
        jiraAccessor.createJiraIssue(jiraIssueRequest);

    }

    /**
     *
     * @param paymentRequest Fetches FULFILLER from payment request
     * @return Fulfiller name
     */
    default Fulfillers getFulfiller(String paymentRequest) {
        if (paymentRequest == null || paymentRequest.isEmpty()) {
            log.info("paymentRequest is null or empty");
            return null;
        }
        String upperCaseRequest = paymentRequest.toUpperCase();
        log.info("upperCaseRequest: " + upperCaseRequest);

        for (Fulfillers fulfiller : Fulfillers.values()) {
            if (upperCaseRequest.contains(fulfiller.name())) {
                return fulfiller;
            }
        }
        return null;
    }


    /**
     * Extracts EuronetRefNo for the given request ID and stores it in the provided map.
     *
     * @param requestId The request ID to extract tracking ID from
     * @param mapOfRequestIdToTrackingId Map to store the extracted tracking ID
     */
    default void extractEuronetTrackingId(String requestId, Map<String, String> mapOfRequestIdToTrackingId)
            throws IOException {

            SearchResponse searchResponse = searchKibanaLogsByPatternForLastHour(
                    ElasticsearchQueryParams.REQUEST_ID.getValue() + ":\"" + requestId + "\"" +
                            " AND " + ElasticsearchQueryParams.MESSAGE.getValue() + ":\"*" + EURONET_REF_NO + "*\"", 1);

            String message = getFirstMessageFromLogs(searchResponse);
            if (message != null) {
                String euronetRefNo = getIDFromLogs(message, Pattern.compile("\"EuronetRefNo\"\\s*:\\s*(\\d+)"));
                if (euronetRefNo != null) {
                    mapOfRequestIdToTrackingId.put(requestId, euronetRefNo);
                } else {
                    log.info("No EuronetRefNo found for RequestId: {}", requestId);
                }
            }
    }

    /**
     * Extracts BD-Traceid  for the given request ID and stores it in the provided map.
     *
     * @param requestId The request ID to extract tracking ID for
     * @param mapOfRequestIdToTrackingId Map to store the extracted tracking ID
     */
    default void extractBillDeskTrackingId(String requestId, Map<String, String> mapOfRequestIdToTrackingId)
            throws IOException {

            SearchResponse searchResponse = searchKibanaLogsByPatternForLastHour(
                    ElasticsearchQueryParams.REQUEST_ID.getValue() + ":\"" + requestId + "\"" +
                            " AND " + ElasticsearchQueryParams.MESSAGE.getValue() + ":\"*" + BD_TRACE_ID + "*\"", 1);

            String message = getFirstMessageFromLogs(searchResponse);
            if (message != null) {
                String traceId = getIDFromLogs(message, Pattern.compile("BD-Traceid=([^,}\\s]+)"));
                if (traceId != null) {
                    mapOfRequestIdToTrackingId.put(requestId, traceId);
                } else {
                    log.info("No BD-Trace-ID found for RequestId: {}", requestId);
                }
            }
    }


    /**
     * Get logs for last 1 hour
     * @param messagePattern pattern to search for
     * @param size no of logs to search for
     * @return
     * @throws IOException
     */
    default SearchResponse searchKibanaLogsByPatternForLastHour(String messagePattern, int size) throws IOException {
        return new ElasticsearchAccessorApiImpl(
                ElasticsearchClientConfigType.RECHARGE_CYGNUS_PROD)
                .queryLogsByMessage("logstash-cygnus-applicationlog-"
                                + LocalDate.now(ZoneId.of("UTC")).format(DATE_FORMATTER),
                        messagePattern,
                        ONE_HOUR_INTERVAL_IN_MINUTES, size);
    }


    /**
     * Get logs based on size for last 1 hour
     * @param messagePattern pattern to search for
     * @param size no of logs to search for
     * @return
     * @throws IOException
     */

    default SearchResponse searchKibanaLogsByPatternForLastTwoHours(String messagePattern,
                                                                    int size) throws IOException {
        return new ElasticsearchAccessorApiImpl(
                ElasticsearchClientConfigType.RECHARGE_CYGNUS_PROD)
                .queryLogsByMessage("logstash-cygnus-applicationlog-"
                                + LocalDate.now(ZoneId.of("UTC")).format(DATE_FORMATTER),
                        messagePattern,
                        TWO_HOUR_INTERVAL_IN_MINUTES, size);
    }


    /**
     * Method to get the first log message from list
     * @param searchResponse
     * @return
     */
    default String getFirstMessageFromLogs(SearchResponse searchResponse) {

        log.info("Search response size: {}", searchResponse.getHits().getTotalHits());
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            log.info("Log from search response: {}", sourceAsMap.get(ElasticsearchQueryParams.MESSAGE.getValue()));
            return (String) sourceAsMap.get(ElasticsearchQueryParams.MESSAGE.getValue());
        }
        return null;
    }


    /**
     * To extract the pattern from logs
     * @param message
     * @param pattern
     * @return
     */
    default String getIDFromLogs(String message, Pattern pattern) {

        if (message == null || message.isEmpty()) {
            return null;
        }
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

}
