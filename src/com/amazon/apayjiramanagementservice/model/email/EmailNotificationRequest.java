package com.amazon.apayjiramanagementservice.model.email;

import lombok.Builder;
import lombok.Data;


/**
 * Model representing an email notification request.
 */
@Data
@Builder
public class EmailNotificationRequest {

    private String subject;

    private String body;

}
