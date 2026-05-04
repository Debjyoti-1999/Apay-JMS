package com.amazon.apayjiramanagementservice.config.email;

import lombok.Getter;

import java.util.List;


public final class EmailClientConfigConstants {

    @Getter
    public enum SesClientConfig {
        RECHARGE_CYGNUS_SES_CLIENT_CONFIG_BETA(
                "chadebjy@amazon.com",
                List.of("debjyotisonuabhi@gmail.com")
        ),

        RECHARGE_CYGNUS_SES_CLIENT_CONFIG_PROD(
                "ace-serviceaccount-amazon-india-payments-apay-jms@amazon.com",
                List.of("hfc-platform-primary-oncall@amazon.com")
        );

        private final String senderEmail;
        private final List<String> recipientEmails;

        SesClientConfig(String senderEmail, List<String> recipientEmails) {
            this.senderEmail = senderEmail;
            this.recipientEmails = recipientEmails;
        }
    }

    private EmailClientConfigConstants() {

    }
}
