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


    /**
     * Method to query logs in kibana
     * @param indices
     * @param message
     * @param size
     * @return
     * @throws IOException
     * @throws InternalServiceException
     */
    SearchResponse queryLogsByMessageAndSize(String indices, String message, int size)
            throws IOException, InternalServiceException;
}
