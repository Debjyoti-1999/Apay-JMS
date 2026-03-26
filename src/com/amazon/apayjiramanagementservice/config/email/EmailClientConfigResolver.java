package com.amazon.apayjiramanagementservice.config.email;

import com.amazon.apayjiramanagementservice.exception.InvalidInputException;

public final class EmailClientConfigResolver {

    private EmailClientConfigResolver() {

    }

    /**
     * Resolves the SES client configuration based on the provided config type.
     *
     * @param configType The SES client config type
     * @return The corresponding SES client configuration
     * @throws InvalidInputException if the config type is not supported
     */
    public static EmailClientConfigConstants.SesClientConfig resolve(EmailClientConfigType configType) {
        switch (configType) {
            case RECHARGE_CYGNUS_SES_CLIENT_PROD:
                return EmailClientConfigConstants.SesClientConfig.RECHARGE_CYGNUS_SES_CLIENT_CONFIG_PROD;
            case RECHARGE_CYGNUS_SES_CLIENT_BETA:
                return EmailClientConfigConstants.SesClientConfig.RECHARGE_CYGNUS_SES_CLIENT_CONFIG_BETA;
            default:
                throw new InvalidInputException("Unsupported SES client config type: " + configType);
        }
    }
}
