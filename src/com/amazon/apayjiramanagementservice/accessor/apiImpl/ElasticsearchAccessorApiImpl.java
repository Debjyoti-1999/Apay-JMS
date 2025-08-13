package com.amazon.apayjiramanagementservice.accessor.apiImpl;

import com.amazon.apayjiramanagementservice.accessor.api.ElasticsearchAccessorApi;
import com.amazon.apayjiramanagementservice.accessor.client.ElasticsearchClient;
import com.amazon.apayjiramanagementservice.config.elasticsearch.ElasticsearchClientConfigType;
import com.amazon.apayjiramanagementservice.constants.elastisearch.ElasticsearchQueryParams;
import com.amazon.apayjiramanagementservice.exception.InternalServiceException;
import com.amazon.apayjiramanagementservice.exception.InvalidInputException;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.io.IOException;

@Log4j2
public class ElasticsearchAccessorApiImpl implements ElasticsearchAccessorApi {

    private final RestHighLevelClient esClient;
    private static final int MAX_RETRIES = 2;

    public ElasticsearchAccessorApiImpl(ElasticsearchClientConfigType clientType) {
        this.esClient = ElasticsearchClient.getInstance(clientType).getEsClient();
    }

    @Override
    public SearchResponse queryLogsByMessage(String indices, String message, int minutesAgo)
            throws IOException, InternalServiceException {
        // Input validation
        validateQueryLogsByMessageInput(indices, message, minutesAgo);

            SearchRequest searchRequest = new SearchRequest(indices);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            searchSourceBuilder.query(
                    QueryBuilders.boolQuery()
                            .must(QueryBuilders.matchQuery(ElasticsearchQueryParams.MESSAGE.getValue(), message))
                            .must(QueryBuilders.rangeQuery(ElasticsearchQueryParams.TIMESTAMP.getValue())
                                    .gte(Instant.now().minus(minutesAgo, ChronoUnit.MINUTES).toString())
                                    .lte(Instant.now().toString()))
            );

            searchSourceBuilder.sort(ElasticsearchQueryParams.TIMESTAMP.getValue(), SortOrder.ASC);
            searchRequest.source(searchSourceBuilder);

            return executeWithRetry(searchRequest, MAX_RETRIES);
    }

    @Override
    public SearchResponse queryLogsWithConditions(String indices, String message, String level,
                                                  String serviceName, int minutesAgo)
            throws IOException, InternalServiceException {
        // Input validation
        validateQueryLogsWithConditionsInput(indices, message, level, serviceName, minutesAgo);
            SearchRequest searchRequest = new SearchRequest(indices);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            searchSourceBuilder.query(
                    QueryBuilders.boolQuery()
                            .must(QueryBuilders.matchQuery(ElasticsearchQueryParams.MESSAGE.getValue(), message))
                            .must(QueryBuilders.matchQuery(ElasticsearchQueryParams.LEVEL.getValue(), level))
                            .must(QueryBuilders.matchQuery(ElasticsearchQueryParams.SERVICE_NAME.getValue(),
                                    serviceName))
                            .must(QueryBuilders.rangeQuery(ElasticsearchQueryParams.TIMESTAMP.getValue())
                                    .gte(Instant.now().minus(minutesAgo, ChronoUnit.MINUTES).toString())
                                    .lte(Instant.now().toString()))
            );

            searchSourceBuilder.sort(ElasticsearchQueryParams.TIMESTAMP.getValue(), SortOrder.DESC);
            searchRequest.source(searchSourceBuilder);

            return executeWithRetry(searchRequest, MAX_RETRIES);
    }

    private SearchResponse executeWithRetry(SearchRequest request, int maxRetries)
            throws InternalServiceException, IOException {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                log.info("Search query: {}", request.source().toString());
                return esClient.search(request, RequestOptions.DEFAULT);
            } catch (IOException e) {
                attempts++;
                if (attempts == maxRetries) throw e;
                log.warn("Retry attempt {} after error: {}", attempts, e.getMessage());
                try {
                    Thread.sleep(1000 * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new InternalServiceException("Interrupted during retry", ie);
                }
            }
        }
        log.error("Failed after {} attempts", maxRetries);
        throw new InternalServiceException("Failed after " + maxRetries + " attempts");
    }


    private void validateQueryLogsByMessageInput(String indices, String message, int minutesAgo) {
        if (indices == null || indices.trim().isEmpty()) {
            throw new InvalidInputException("Indices parameter cannot be null or empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new InvalidInputException("Message parameter cannot be null or empty");
        }
        if (minutesAgo < 0) {
            throw new InvalidInputException("MinutesAgo parameter must be non-negative, got: " + minutesAgo);
        }
    }

    private void validateQueryLogsWithConditionsInput(String indices, String message, String level,
                                                     String serviceName, int minutesAgo) {
        if (indices == null || indices.trim().isEmpty()) {
            throw new InvalidInputException("Indices parameter cannot be null or empty");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new InvalidInputException("Message parameter cannot be null or empty");
        }
        if (level == null || level.trim().isEmpty()) {
            throw new InvalidInputException("Level parameter cannot be null or empty");
        }
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new InvalidInputException("ServiceName parameter cannot be null or empty");
        }
        if (minutesAgo < 0) {
            throw new InvalidInputException("MinutesAgo parameter must be non-negative" + minutesAgo);
        }
    }

}
