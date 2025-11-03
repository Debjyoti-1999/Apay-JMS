package com.amazon.apayjiramanagementservice.accessor.api;


import com.amazon.apayjiramanagementservice.exception.InternalServiceException;
import org.elasticsearch.action.search.SearchResponse;

import java.io.IOException;

// Interface for API to access Elasticsearch.
public interface ElasticsearchAccessorApi {

    /**
     * Method to query logs in kibana
     * @param indices
     * @param message
     * @param minutesAgo
     * @return
     * @throws IOException
     * @throws InternalServiceException
     */
    SearchResponse queryLogsByMessage(String indices, String message, int minutesAgo)
            throws IOException, InternalServiceException;

    /**
     * Method to query logs in kibana
     * @param indices
     * @param message
     * @param minutesAgo
     * @param size
     * @return
     * @throws IOException
     * @throws InternalServiceException
     */
    SearchResponse queryLogsByMessage(String indices, String message, int minutesAgo, int size)
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
