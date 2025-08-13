package com.amazon.apayjiramanagementservice.helper;

import com.amazon.apayjiramanagementservice.model.jira.Assignee;
import com.amazon.apayjiramanagementservice.model.jira.Content;
import com.amazon.apayjiramanagementservice.model.jira.Document;
import com.amazon.apayjiramanagementservice.model.jira.JiraIssueRequest;
import com.amazon.apayjiramanagementservice.model.jira.TextContent;
import com.amazon.apayjiramanagementservice.model.jira.Fields;
import com.amazon.apayjiramanagementservice.model.jira.IssueType;
import com.amazon.apayjiramanagementservice.model.jira.Priority;
import com.amazon.apayjiramanagementservice.model.jira.Project;

import java.util.List;

public class JiraIssueFactory {

    private final String defaultProjectId;
    private final String defaultIssueTypeId;

    public JiraIssueFactory(String defaultProjectId, String defaultIssueTypeId) {
        this.defaultProjectId = defaultProjectId;
        this.defaultIssueTypeId = defaultIssueTypeId;
    }

    public JiraIssueRequest createIssue(String summary, String descriptionText,
                                        String dueDate, String assigneeAccountId) {
        return createIssueWithPriority(summary, descriptionText, dueDate, assigneeAccountId, null);
    }

    public JiraIssueRequest createIssueWithPriority(String summary, String descriptionText, String dueDate,
                                                    String assigneeAccountId, String priorityId) {
        TextContent textContent = TextContent.builder()
                .text(descriptionText)
                .type("text")
                .build();

        Content content = Content.builder()
                .content(List.of(textContent))
                .type("paragraph")
                .build();

        Document description = Document.builder()
                .type("doc")
                .version(1)
                .content(List.of(content))
                .build();

        Assignee assignee = Assignee.builder().accountId(assigneeAccountId).build();

        Fields.FieldsBuilder fieldsBuilder = Fields.builder()
                .summary(summary)
                .dueDate(dueDate)
                .issueType(IssueType.builder().id(defaultIssueTypeId).build())
                .project(Project.builder().id(defaultProjectId).build())
                .description(description)
                .assignee(assignee);

        if (priorityId != null) {
            fieldsBuilder.priority(Priority.builder().id(priorityId).build());
        }

        Fields fields = fieldsBuilder.build();

        return JiraIssueRequest.builder()
                .fields(fields)
                .build();
    }
}
