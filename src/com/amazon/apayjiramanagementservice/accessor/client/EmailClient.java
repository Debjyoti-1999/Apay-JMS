package com.amazon.apayjiramanagementservice.accessor.client;

import com.amazon.apayjiramanagementservice.config.email.EmailClientConfigConstants;
import com.amazon.apayjiramanagementservice.config.email.EmailClientConfigResolver;
import com.amazon.apayjiramanagementservice.config.email.EmailClientConfigType;
import com.amazon.apayjiramanagementservice.exception.DependencyFailureException;
import com.amazon.apayjiramanagementservice.model.email.EmailNotificationRequest;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Log4j2
public final class EmailClient {

    private static final ConcurrentHashMap<EmailClientConfigType, EmailClient> CLIENT_MAP = new ConcurrentHashMap<>();

    private final software.amazon.awssdk.services.ses.SesClient sesClient;
    private final String senderEmail;
    private final List<String> recipientEmails;

    private EmailClient(EmailClientConfigType configType) {
        log.info("Creating SES client for type: {}", configType);

        try {
            EmailClientConfigConstants.SesClientConfig config = EmailClientConfigResolver.resolve(configType);
            this.senderEmail = config.getSenderEmail();
            this.recipientEmails = config.getRecipientEmails();
            this.sesClient = software.amazon.awssdk.services.ses.SesClient.builder()
                    .region(Region.of(System.getenv("AWS_REGION")))
                    .build();
        } catch (Exception e) {
            log.error("Failed to create SES email client for type: {}", configType, e);
            throw new DependencyFailureException("Failed to create SES email client for type: " + configType, e);
        }
    }

    /**
     *
     * @param configType The SES client configuration type
     * @return The EmailClient instance
     */
    public static EmailClient getInstance(EmailClientConfigType configType) {
        return CLIENT_MAP.computeIfAbsent(configType, EmailClient::new);
    }

    /**
     * Sends an email
     *
     * @param request The email notification request containing subject, and body
     * @return The SendEmailResponse from SES
     */
    public SendEmailResponse sendEmail(EmailNotificationRequest request) {
        try {
            Destination.Builder destinationBuilder = Destination.builder()
                    .toAddresses(recipientEmails);

            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .source(senderEmail)
                    .destination(destinationBuilder.build())
                    .message(Message.builder()
                            .subject(Content.builder()
                                    .charset("UTF-8")
                                    .data(request.getSubject())
                                    .build())
                            .body(Body.builder()
                                    .html(Content.builder()
                                            .charset("UTF-8")
                                            .data(request.getBody())
                                            .build())
                                    .build())
                            .build())
                    .build();

            SendEmailResponse response = sesClient.sendEmail(emailRequest);
            log.info("Email sent successfully, message ID: {}", response.messageId());
            return response;

        } catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
            throw  e;
        }
    }

}
