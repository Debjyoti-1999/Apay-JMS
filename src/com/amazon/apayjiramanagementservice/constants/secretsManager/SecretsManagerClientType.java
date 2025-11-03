package com.amazon.apayjiramanagementservice.constants.secretsManager;

import lombok.Getter;

@Getter
public enum SecretsManagerClientType {
    JMS_SECRETS_MANAGER_PROD("arn:aws:secretsmanager:us-east-1:384657437616:secret:JMSSecrets-8wfcHg", "us-east-1"),
    JMS_SECRETS_MANAGER_BETA("arn:aws:secretsmanager:us-east-1:051826734866:secret:JMSSecrets-dTdMnK", "us-east-1");

    private final String secretsManagerARN;
    private final String region;

    SecretsManagerClientType(String secretsManagerARN, String region) {
        this.secretsManagerARN = secretsManagerARN;
        this.region = region;
    }
}
