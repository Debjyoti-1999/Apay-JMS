package com.amazon.apayjiramanagementservice.model.jira;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Document {

    private String type;
    private Integer version;
    private List<Content> content;
}
