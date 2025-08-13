package com.amazon.apayjiramanagementservice.config.jira;

import com.amazon.apayjiramanagementservice.constants.secretsManager.SecretsManagerClientType;
import lombok.Getter;


public final class JiraClientConfigConstants {

    @Getter
    public enum JiraClientConfig {
        RECHARGE_CYGNUS_JIRA_CLIENT_CONFIG_BETA("https://amazon-sandbox.atlassian.net/rest/api/3/issue",
                 SecretsManagerClientType.JMS_SECRETS_MANAGER_BETA),

        RECHARGE_CYGNUS_JIRA_CLIENT_CONFIG_PROD("",
                SecretsManagerClientType.JMS_SECRETS_MANAGER_PROD);

        private final String baseUrl;
        private final SecretsManagerClientType secretsManagerClientType;

        JiraClientConfig(String baseUrl, SecretsManagerClientType secretsManagerClientType) {
            this.baseUrl = baseUrl;
            this.secretsManagerClientType = secretsManagerClientType;
        }
    }

    private JiraClientConfigConstants() {

    }

}
