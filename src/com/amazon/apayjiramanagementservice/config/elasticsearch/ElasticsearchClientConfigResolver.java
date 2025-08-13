package com.amazon.apayjiramanagementservice.config.elasticsearch;

public final class ElasticsearchClientConfigResolver {

    private ElasticsearchClientConfigResolver() {

    }

    public static ElasticsearchClientConfigConstants.ElasticsearchClientConfig resolve(
            ElasticsearchClientConfigType type
    ) {
        return switch (type) {
            case RECHARGE_CYGNUS_PROD ->
                    ElasticsearchClientConfigConstants.ElasticsearchClientConfig.
                            RECHARGE_CYGNUS_ELASTICSEARCH_CONFIG_PROD;
            case RECHARGE_CYGNUS_BETA ->
                    ElasticsearchClientConfigConstants.ElasticsearchClientConfig.
                            RECHARGE_CYGNUS_ELASTICSEARCH_CONFIG_BETA;
        };
    }
}
