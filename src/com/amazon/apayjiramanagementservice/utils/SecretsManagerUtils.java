package com.amazon.apayjiramanagementservice.utils;

import com.amazon.apayjiramanagementservice.constants.secretsManager.SecretsManagerClientType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SecretsManagerUtils {
    private static final ConcurrentHashMap<SecretsManagerClientType,
            SecretsManagerUtils> CLIENT_MAP = new ConcurrentHashMap<>();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final SecretsManagerClient client;

    private final String secretArn;

    private SecretsManagerUtils(SecretsManagerClientType secretsManagerClientType) {
        this.client = SecretsManagerClient.builder()
                .region(Region.of(secretsManagerClientType.getRegion()))
                .build();

        this.secretArn = secretsManagerClientType.getSecretsManagerARN();
    }

    public static SecretsManagerUtils getInstance(SecretsManagerClientType secretsManagerClientType) {
        return CLIENT_MAP.computeIfAbsent(secretsManagerClientType, SecretsManagerUtils::new);
    }

    public String getSecret(String key) throws JsonProcessingException {
        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(this.secretArn)
                .build();

        GetSecretValueResponse result = client.getSecretValue(request);
        String secretJson = result.secretString();
        Map<String, String> secretMap = objectMapper.readValue(secretJson, new TypeReference<>() {});
        return secretMap.get(key);
    }
}
