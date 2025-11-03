package com.amazon.apayjiramanagementservice.constants.elastisearch;

public enum ElasticsearchQueryParams {

    TIMESTAMP("@timestamp"),
    MESSAGE("message"),
    LEVEL("level"),
    SERVICE_NAME("service.name"),
    HOST("host"),
    METHOD_NAME("method.name"),
    REQUEST_ID("RequestId");

    private final String fieldName;

    ElasticsearchQueryParams(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getValue() {
        return fieldName;
    }
}
