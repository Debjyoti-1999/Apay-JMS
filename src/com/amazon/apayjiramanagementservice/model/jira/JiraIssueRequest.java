package com.amazon.apayjiramanagementservice.model.jira;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JiraIssueRequest {
    private Fields fields;
    private String update;
}
