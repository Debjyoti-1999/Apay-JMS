package com.amazon.apayjiramanagementservice.model.jira;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class Content {

    private List<TextContent> content;
    private String type;


}
