package com.amazon.apayjiramanagementservice.accessor.api;

import com.amazon.apayjiramanagementservice.model.jira.JiraIssueRequest;


public interface JiraAccessorApi {

    void createJiraIssue(JiraIssueRequest request);
}
