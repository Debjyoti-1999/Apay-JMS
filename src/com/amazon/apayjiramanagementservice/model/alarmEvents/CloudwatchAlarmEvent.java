package com.amazon.apayjiramanagementservice.model.alarmEvents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 * Reference link for Cloudwatch events POJO for deserializing SNS events:
 *
 * <a href="https://code.amazon.com/packages/ADSPCanariesService/blobs/mainline/--/src/main/java/com/amazon/adsp/canaries/api/domain/event/CloudWatchAlarmEvent.java"></a>
 * <a href="https://code.amazon.com/packages/HaystackAdminToolsLambda/blobs/34fae2b7bb494995da91bd38d80c491b377dd854/--/src/com/amazon/haystack/admintoolslambda/request/events/CloudWatchAlarmEvent.java#L43"></a>
 * <a href="https://code.amazon.com/packages/CanaryTicketCreator/blobs/mainline/--/src/main/java/com/amazon/canaryticketcreator/lambda/domain/event/CloudWatchAlarmEvent.java"></a>
 *
 *
 * {
 *   "AlarmName": "JmsServiceAlarm",
 *   "AlarmDescription": "JMS Service Error Alarm",
 *   "AWSAccountId": "123456789012",
 *   "NewStateValue": "ALARM",
 *   "NewStateReason": "Threshold Crossed: 1 datapoint [2.0 (06/04/23 02:12:00)] was greater than the threshold (1.0).",
 *   "StateChangeTime": "2023-06-04T02:17:06.186+0000",
 *   "Region": "US-EAST-1",
 *   "AlarmArn": "arn:aws:cloudwatch:us-east-1:123456789012:alarm:JmsServiceAlarm",
 *   "OldStateValue": "OK",
 *   "Trigger": {
 *     "MetricName": "Errors",
 *     "Namespace": "AWS/Lambda",
 *     "StatisticType": "Statistic",
 *     "Statistic": "SUM",
 *     "Unit": null,
 *     "Dimensions": [
 *       {
 *         "value": "JmsServiceLambda",
 *         "name": "FunctionName"
 *       }
 *     ],
 *     "Period": 300,
 *     "EvaluationPeriods": 1,
 *     "ComparisonOperator": "GreaterThanThreshold",
 *     "Threshold": 1.0,
 *     "TreatMissingData": "missing",
 *     "EvaluateLowSampleCountPercentile": ""
 *   }
 * }
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloudwatchAlarmEvent {
    @JsonProperty("AlarmName")
    private String alarmName;

    @JsonProperty("AlarmDescription")
    private String alarmDescription;

    @JsonProperty("AWSAccountId")
    private String awsAccountId;

    @JsonProperty("NewStateValue")
    private String newStateValue;

    @JsonProperty("NewStateReason")
    private String newStateReason;

    @JsonProperty("StateChangeTime")
    private String stateChangeTime;

    @JsonProperty("Region")
    private String region;

    @JsonProperty("AlarmArn")
    private String alarmArn;

    @JsonProperty("OldStateValue")
    private String oldStateValue;

    @JsonProperty("Unit")
    private String unit;

    @JsonProperty("Trigger")
    private Trigger trigger;
}
