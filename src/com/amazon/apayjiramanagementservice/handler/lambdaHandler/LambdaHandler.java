package com.amazon.apayjiramanagementservice.handler.lambdaHandler;

import com.amazon.apayjiramanagementservice.constants.CloudwatchAlarms.AlarmProperties;
import com.amazon.apayjiramanagementservice.exception.DependencyFailureException;
import com.amazon.apayjiramanagementservice.exception.InternalServiceException;
import com.amazon.apayjiramanagementservice.exception.InvalidInputException;
import com.amazon.apayjiramanagementservice.handler.alarmHandler.AlarmHandlerFactory;
import com.amazon.apayjiramanagementservice.model.alarmEvents.CloudwatchAlarmEvent;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;



@Log4j2
public class LambdaHandler implements RequestHandler<SQSEvent, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {

        long startTime = System.currentTimeMillis();
        log.info("Received sqs event: {}", startTime);

        try {
             processEvent(sqsEvent, context);

        } catch (DependencyFailureException | InvalidInputException | InternalServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to process event due to unknown internal error", e);
        }

        log.info("Successfully processed event, time taken in millis: {}", System.currentTimeMillis() - startTime);

        return "SUCCESS";
    }

    private void processEvent(SQSEvent sqsEvent, Context context) throws Exception {
            for (SQSEvent.SQSMessage sqsMessage : sqsEvent.getRecords()) {

                CloudwatchAlarmEvent alarmEvent = objectMapper.readValue(objectMapper.readTree(sqsMessage.getBody()).
                                get("Message").asText(),
                        CloudwatchAlarmEvent.class);

                handleAlarmEvent(alarmEvent);
            }
        log.info("Successfully processed all SQS messages");
    }

    private void handleAlarmEvent(CloudwatchAlarmEvent alarmEvent) throws Exception {
        String alarmName = alarmEvent.getAlarmName();

        for (AlarmProperties alarmProperty : AlarmProperties.values()) {
            if (alarmName.equals(alarmProperty.getName()) || alarmName.contains(alarmProperty.getName())) {
                log.info("Processing event for alarm: {}", alarmProperty.getName());
                AlarmHandlerFactory.getInstance().getServiceAlarmHandler(alarmProperty).handleEvent(alarmEvent);
                return;
            }
        }
        throw new InternalServiceException("No AlarmProperties match found for alarm: " + alarmName);
    }

}
