package com.amazon.apayjiramanagementservice.accessor.api;


import com.amazon.apayjiramanagementservice.exception.InternalServiceException;
import org.elasticsearch.action.search.SearchResponse;

import java.io.IOException;

// Interface for API to access Elasticsearch.
public interface ElasticsearchAccessorApi {
    /**.
     * Query logs by message content within a specific time range
     *
     * @param message The message content to search for
     * @param minutesAgo Time range in minutes from current time
     * @return SearchResponse containing the query results
     * @throws IOException if the search request fails
     */
    SearchResponse queryLogsByMessage(String indices, String message, int minutesAgo)
            throws IOException, InternalServiceException;

    /**.
     * Query logs with multiple conditions within a specific time range
     *
     * @param message The message content to search for
     * @param level The log level to filter by
     * @param serviceName The service name to filter by
     * @param minutesAgo Time range in minutes from current time
     * @return SearchResponse containing the query results
     * @throws IOException if the search request fails
     */
    SearchResponse queryLogsWithConditions(String indices, String message, String level,
                                           String serviceName, int minutesAgo)
            throws IOException, InternalServiceException;
}
