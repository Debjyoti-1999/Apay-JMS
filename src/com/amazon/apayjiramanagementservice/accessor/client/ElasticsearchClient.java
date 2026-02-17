package com.amazon.apayjiramanagementservice.accessor.client;


import com.amazon.apayjiramanagementservice.config.elasticsearch.ElasticsearchClientConfigResolver;
import com.amazon.apayjiramanagementservice.config.elasticsearch.ElasticsearchClientConfigType;
import com.amazon.apayjiramanagementservice.config.elasticsearch.ElasticsearchClientConfigConstants;
import com.amazon.apayjiramanagementservice.exception.DependencyFailureException;
import io.github.acm19.aws.interceptor.http.AwsRequestSigningApacheInterceptor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.auth.aws.signer.AwsV4HttpSigner;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

import java.util.concurrent.ConcurrentHashMap;


@Log4j2
public final class ElasticsearchClient {
    private static final ConcurrentHashMap<ElasticsearchClientConfigType,
            ElasticsearchClient> CLIENT_MAP = new ConcurrentHashMap<>();
    private final RestHighLevelClient esClient;

    private static final String LAMBDA_REGION = System.getenv("AWS_REGION");

        private ElasticsearchClient(ElasticsearchClientConfigType clientType) {
            log.info("Creating Elasticsearch client for type: {}", clientType);
            try {
                this.esClient = createClient(clientType);
            } catch (Exception e) {
                log.error("Failed to create Elasticsearch client for type: {}", clientType, e);
                throw new DependencyFailureException("Failed to create Elasticsearch client for type: "
                        + clientType, e);
            }
        }

        public static ElasticsearchClient getInstance(ElasticsearchClientConfigType clientType) {
            return CLIENT_MAP.computeIfAbsent(clientType, ElasticsearchClient::new);
        }

    private RestHighLevelClient createClient(ElasticsearchClientConfigType clientType) {
        ElasticsearchClientConfigConstants.ElasticsearchClientConfig elasticsearchClientConfig =
                ElasticsearchClientConfigResolver.resolve(clientType);


        StsClient stsClient = StsClient.builder()
                .httpClientBuilder(ApacheHttpClient.builder())
                .region(Region.of(LAMBDA_REGION))
                .build();

        // Create AWS SDK v2 credentials provider
        AwsCredentialsProvider credentialsProvider = StsAssumeRoleCredentialsProvider.builder()
                .refreshRequest(AssumeRoleRequest.builder()
                        .roleArn(elasticsearchClientConfig.getElasticsearchIAMRole().getRoleArn())
                        .roleSessionName(elasticsearchClientConfig.getElasticsearchIAMRole().getSessionName())
                        .build())
                .stsClient(stsClient)
                .build();

        AwsV4HttpSigner signer = AwsV4HttpSigner.create();

        AwsRequestSigningApacheInterceptor interceptor = new AwsRequestSigningApacheInterceptor(
                elasticsearchClientConfig.getServiceName(),
                signer,
                credentialsProvider,
                elasticsearchClientConfig.getRegion()
        );

        return new RestHighLevelClient(
                RestClient.builder(
                                new HttpHost(
                                        elasticsearchClientConfig.getEndPoint(),
                                        elasticsearchClientConfig.getPort(),
                                        elasticsearchClientConfig.getHttpScheme()
                                ))
                        .setHttpClientConfigCallback(hacb ->
                                hacb.addInterceptorLast(interceptor)
                        )
        );
    }


    public RestHighLevelClient getEsClient() {
        return this.esClient;
    }
}
