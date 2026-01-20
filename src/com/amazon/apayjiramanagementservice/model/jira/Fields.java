package com.amazon.apayjiramanagementservice.model.jira;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Fields {

    @JsonProperty("summary")
    private String summary;

    @JsonProperty(value = "duedate")
    private String dueDate;

    @JsonProperty(value = "issuetype")
    private IssueType issueType;

    @JsonProperty("project")
    private Project project;

    @JsonProperty("description")
    private Document description;

    @JsonProperty("assignee")
    private Assignee assignee;

    @JsonProperty("priority")
    private Priority priority;

    @JsonProperty("labels")
    private List<String> labels;
}
