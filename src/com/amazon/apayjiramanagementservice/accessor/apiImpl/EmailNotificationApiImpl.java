package com.amazon.apayjiramanagementservice.accessor.apiImpl;

import com.amazon.apayjiramanagementservice.accessor.api.EmailNotificationApi;
import com.amazon.apayjiramanagementservice.accessor.client.EmailClient;
import com.amazon.apayjiramanagementservice.config.email.EmailClientConfigType;
import com.amazon.apayjiramanagementservice.model.email.EmailNotificationRequest;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EmailNotificationApiImpl implements EmailNotificationApi {

    private final EmailClient emailClient;

    public EmailNotificationApiImpl(EmailClientConfigType emailClientConfigType) {
        this.emailClient = EmailClient.getInstance(emailClientConfigType);
    }

    @Override
    public void sendEmail(EmailNotificationRequest emailNotificationRequest) {
        emailClient.sendEmail(emailNotificationRequest);
    }
}
