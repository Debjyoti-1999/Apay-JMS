package com.amazon.apayjiramanagementservice.accessor.client;

import com.amazon.apayjiramanagementservice.config.jira.JiraClientConfigConstants;
import com.amazon.apayjiramanagementservice.config.jira.JiraClientConfigType;
import com.amazon.apayjiramanagementservice.config.jira.JiraClientConfigResolver;
import com.amazon.apayjiramanagementservice.constants.secretsManager.SecretsManagerClientType;
import com.amazon.apayjiramanagementservice.exception.DependencyFailureException;
import com.amazon.apayjiramanagementservice.model.jira.JiraIssueRequest;
import com.amazon.apayjiramanagementservice.utils.SecretsManagerUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles communication with Jira REST API.
 */
@Log4j2
public final class JiraClient {

    private static final ConcurrentHashMap<JiraClientConfigType,
            JiraClient> CLIENT_MAP = new ConcurrentHashMap<>();

    private final String baseUrl;
    private final String authHeader;
    private final String userId;
    private final String apiToken;
    private final SecretsManagerClientType secretsManagerClientType;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private JiraClient(JiraClientConfigType clientType) {
        log.info("Creating jira client for type: {}", clientType);

        try {
            JiraClientConfigConstants.JiraClientConfig jiraClientConfig = JiraClientConfigResolver.resolve(clientType);
            this.baseUrl = jiraClientConfig.getBaseUrl();
            this.secretsManagerClientType = jiraClientConfig.getSecretsManagerClientType();
            this.userId = SecretsManagerUtils.getInstance(this.secretsManagerClientType).getSecret("USER_ID");
            this.apiToken = SecretsManagerUtils.getInstance(this.secretsManagerClientType).getSecret("API_TOKEN");
            this.authHeader = buildBasicAuthHeader(userId, apiToken);
        } catch (Exception e) {
            log.error("Failed to create Jira client for type: {}", clientType, e);
            throw new DependencyFailureException("Failed to create Jira client for type: " + clientType, e);
        }
    }

    public static JiraClient getInstance(JiraClientConfigType jiraClientConfigType) {
        return CLIENT_MAP.computeIfAbsent(jiraClientConfigType, JiraClient::new);
    }


    private String buildBasicAuthHeader(String userId, String token) {
        String credentials = userId + ":" + token;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    public HttpResponse<String> createIssue(JiraIssueRequest request) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", authHeader)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .build();

        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

    }

}
