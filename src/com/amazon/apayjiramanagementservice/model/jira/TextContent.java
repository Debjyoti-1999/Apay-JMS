package com.amazon.apayjiramanagementservice.model.jira;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TextContent {

    private String text;
    private String type;
}
