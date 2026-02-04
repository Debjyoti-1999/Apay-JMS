package com.amazon.apayjiramanagementservice.config.elasticsearch;

import com.amazon.apayjiramanagementservice.constants.iam.IAMRole;
import lombok.Getter;

import static com.amazon.apayjiramanagementservice.constants.iam.IAMRole.
        RECHARGE_CYGNUS_ELASTIC_SEARCH_ACCESS_ROLE_BETA;
import static com.amazon.apayjiramanagementservice.constants.iam.IAMRole.RECHARGE_CYGNUS_ELASTIC_SEARCH_ACCESS_ROLE_PROD;

/**.
 * Configuration class for Elasticsearch client
 */
public final class ElasticsearchClientConfigConstants {

    public static final String ES_ENDPOINT_FOR_RECHARGE_CYGNUS_PROD =
            "search-hfc-cygnus-elk-prod-4dbn5hqqvacxqxerbz75qfdasq.ap-south-2.es.amazonaws.com";

    public static final String ES_ENDPOINT_FOR_RECHARGE_CYGNUS_BETA =
            "search-rechargecygnuselasticsearch-kd2et4xlaoloiaztwhmjenb7k4.ap-south-1.es.amazonaws.com";



    /**.
     * Configuration ENUM for Elasticsearch client
     */

    @Getter
    public enum ElasticsearchClientConfig {
        RECHARGE_CYGNUS_ELASTICSEARCH_CONFIG_PROD(ES_ENDPOINT_FOR_RECHARGE_CYGNUS_PROD,
                60000, "es", "https", 443,
                RECHARGE_CYGNUS_ELASTIC_SEARCH_ACCESS_ROLE_PROD, "ap-south-2"),

        RECHARGE_CYGNUS_ELASTICSEARCH_CONFIG_BETA(ES_ENDPOINT_FOR_RECHARGE_CYGNUS_BETA,
                60000, "es", "https", 443,
                RECHARGE_CYGNUS_ELASTIC_SEARCH_ACCESS_ROLE_BETA, "ap-south-1");

        private final String endPoint;
        private final int maxRetryTimeout;
        private final String serviceName;
        private final String httpScheme;
        private final int port;
        private final IAMRole elasticsearchIAMRole;
        private final String region;

        ElasticsearchClientConfig(String endPoint, int maxRetryTimeout,
                                  String serviceName, String httpScheme,
                                  int port, IAMRole elasticsearchIAMRole, String region) {
            this.endPoint = endPoint;
            this.maxRetryTimeout = maxRetryTimeout;
            this.serviceName = serviceName;
            this.httpScheme = httpScheme;
            this.port = port;
            this.elasticsearchIAMRole = elasticsearchIAMRole;
            this.region = region;
        }

    }

    private ElasticsearchClientConfigConstants() {

    }
}
