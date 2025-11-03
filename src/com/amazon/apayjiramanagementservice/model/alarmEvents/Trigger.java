package com.amazon.apayjiramanagementservice.model.alarmEvents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Trigger {
    @JsonProperty("MetricName")
    private String metricName;

    @JsonProperty("Namespace")
    private String namespace;

    @JsonProperty("StatisticType")
    private String statisticType;

    @JsonProperty("Statistic")
    private String statistic;

    @JsonProperty("Unit")
    private String unit;

    @JsonProperty("Dimensions")
    private List<Dimension> dimensions;

    @JsonProperty("Period")
    private Integer period;

    @JsonProperty("EvaluationPeriods")
    private Integer evaluationPeriods;

    @JsonProperty("ComparisonOperator")
    private String comparisonOperator;

    @JsonProperty("Threshold")
    private Double threshold;

    @JsonProperty("TreatMissingData")
    private String treatMissingData;

    @JsonProperty("EvaluateLowSampleCountPercentile")
    private String evaluateLowSampleCountPercentile;
}
