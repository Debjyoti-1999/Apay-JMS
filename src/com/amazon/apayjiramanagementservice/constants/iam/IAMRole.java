package com.amazon.apayjiramanagementservice.constants.iam;

import lombok.Getter;

@Getter
public enum IAMRole {

    RECHARGE_CYGNUS_ELASTIC_SEARCH_ACCESS_ROLE_PROD(
            "arn:aws:iam::916660361932:role/RechargeCygnusElasticsearchAccessRole",
            "ElasticsearchSession",
            "916660361932"
    ),

    RECHARGE_CYGNUS_ELASTIC_SEARCH_ACCESS_ROLE_BETA(
            "arn:aws:iam::051826734866:role/RechargeCygnusElasticSearchBeta",
            "ElasticsearchSession",
            "051826734866"
    );

    private final String roleArn;
    private final String sessionName;
    private final String accountId;

    IAMRole(String roleArn, String sessionName, String accountId) {
        this.roleArn = roleArn;
        this.sessionName = sessionName;
        this.accountId = accountId;
    }
}
