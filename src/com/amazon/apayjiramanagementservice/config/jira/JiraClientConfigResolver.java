package com.amazon.apayjiramanagementservice.config.jira;

public final class JiraClientConfigResolver {

    private JiraClientConfigResolver() {

    }
    public static JiraClientConfigConstants.JiraClientConfig resolve(JiraClientConfigType type) {
        return switch (type) {
            case RECHARGE_CYGNUS_JIRA_CLIENT_PROD ->
                JiraClientConfigConstants.JiraClientConfig.RECHARGE_CYGNUS_JIRA_CLIENT_CONFIG_PROD;
            case RECHARGE_CYGNUS_JIRA_CLIENT_BETA ->
                    JiraClientConfigConstants.JiraClientConfig.RECHARGE_CYGNUS_JIRA_CLIENT_CONFIG_BETA;
        };
    }
}
