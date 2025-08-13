package com.amazon.apayjiramanagementservice.lambda;

import com.amazon.apayjiramanagementservice.exception.DependencyFailureException;
import com.amazon.apayjiramanagementservice.exception.InternalServiceException;
import com.amazon.apayjiramanagementservice.exception.InvalidInputException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;


import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import lombok.extern.log4j.Log4j2;






@Log4j2
public class LambdaHandler implements RequestHandler<SQSEvent, String> {

    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        log.info("received sqs event");
        try {
            return processEvent(sqsEvent, context);
        } catch (DependencyFailureException | InternalServiceException | InvalidInputException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while processing event: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private String processEvent(SQSEvent sqsEvent, Context context) {
            return "SUCCESS";
    }

}
