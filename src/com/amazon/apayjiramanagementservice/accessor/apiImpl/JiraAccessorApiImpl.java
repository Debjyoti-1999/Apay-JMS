package com.amazon.apayjiramanagementservice.accessor.apiImpl;

import com.amazon.apayjiramanagementservice.accessor.api.JiraAccessorApi;
import com.amazon.apayjiramanagementservice.accessor.client.JiraClient;
import com.amazon.apayjiramanagementservice.config.jira.JiraClientConfigType;
import com.amazon.apayjiramanagementservice.exception.DependencyFailureException;
import com.amazon.apayjiramanagementservice.exception.InternalServiceException;
import com.amazon.apayjiramanagementservice.exception.InvalidInputException;
import com.amazon.apayjiramanagementservice.model.jira.JiraIssueRequest;
import lombok.extern.log4j.Log4j2;

import java.net.http.HttpResponse;

@Log4j2
public class JiraAccessorApiImpl implements JiraAccessorApi {

    private final JiraClient jiraClient;

    public JiraAccessorApiImpl(JiraClientConfigType jiraClientConfigType) {
        this.jiraClient = JiraClient.getInstance(jiraClientConfigType);
    }

    @Override
    public void createJiraIssue(JiraIssueRequest request) {
            // Input validation
            validateJiraIssueRequest(request);
                HttpResponse<String> response = executeWithRetry(request, 2);
                if (response != null && (response.statusCode() < 200 || response.statusCode() >= 300)) {
                    log.error("Failed to create jira ticket: {} | Status code: {}",
                            response.body(), response.statusCode());

                    throw new DependencyFailureException(
                            response.body() // failure in jira while creating ticket
                    );
                }
    }

    private HttpResponse<String> executeWithRetry(JiraIssueRequest request, int maxRetries)
            throws InternalServiceException, DependencyFailureException {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                return jiraClient.createIssue(request);
            } catch (Exception e) {
                attempts++;
                if (attempts == maxRetries) throw new DependencyFailureException(e.getMessage(), e);
                log.warn("JIRA api retry attempt {} after error: {}", attempts, e.getMessage());
                try {
                    Thread.sleep(1000L * attempts);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted during retry", ie);
                    throw new InternalServiceException("Interrupted during retry", ie);
                }
            }
        }
        return null;
    }


    private void validateJiraIssueRequest(JiraIssueRequest request) {
        if (request == null) {
            throw new InvalidInputException("JiraIssueRequest cannot be null");
        }
        if (request.getFields() == null) {
            throw new InvalidInputException("JiraIssueRequest fields cannot be null");
        }
        if (request.getFields().getProject() == null) {
            throw new InvalidInputException("Project information is required in JiraIssueRequest");
        }
        if (request.getFields().getIssueType() == null) {
            throw new InvalidInputException("Issue type is required in JiraIssueRequest");
        }
        if (request.getFields().getSummary() == null || request.getFields().getSummary().trim().isEmpty()) {
            throw new InvalidInputException("Summary is required and cannot be empty in JiraIssueRequest");
        }
    }
}
