package com.amazon.apayjiramanagementservice.accessor.api;

import com.amazon.apayjiramanagementservice.model.email.EmailNotificationRequest;

public interface EmailNotificationApi {

    void sendEmail(EmailNotificationRequest emailNotificationRequest);
}
