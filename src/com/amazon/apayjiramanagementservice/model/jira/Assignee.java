package com.amazon.apayjiramanagementservice.model.jira;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Assignee {
    @JsonProperty("accountId")
    private String accountId; // Atlassian accountId
}
