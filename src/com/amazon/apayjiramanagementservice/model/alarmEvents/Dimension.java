package com.amazon.apayjiramanagementservice.model.alarmEvents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class Dimension {
    @JsonProperty("value")
    private String value;

    @JsonProperty("name")
    private String name;
}
