package com.amazon.apayjiramanagementservice.constants.elastisearch;

public enum ElastisearchCustomPatterns {
    RECHARGE_CYGNUS_FETCH_BILL_RESPONSE("FETCH_BILL_RESPONSE");

    private final String term;

    ElastisearchCustomPatterns(String term) {
        this.term = term;
    }

    public String getValue() {
        return term;
    }
}
