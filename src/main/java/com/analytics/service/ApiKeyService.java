package com.analytics.service;

import com.analytics.dto.response.ApiKeyResponse;
import com.analytics.entity.ApiKey;
import com.analytics.entity.App;
import com.analytics.exception.ResourceNotFoundException;
import com.analytics.repository.ApiKeyRepository;
import com.analytics.repository.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final AppRepository appRepository;
    private static final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public ApiKey generateApiKey(App app, String name, int validityDays) {
        String keyValue = generateSecureApiKey();
        
        ApiKey apiKey = ApiKey.builder()
                .keyValue(keyValue)
                .name(name)
                .app(app)
                .status(ApiKey.ApiKeyStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusDays(validityDays))
                .requestCount(0L)
                .build();

        return apiKeyRepository.save(apiKey);
    }

    public List<ApiKeyResponse> getApiKeysByAppId(String appId) {
        App app = appRepository.findByAppId(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App not found"));

        return apiKeyRepository.findByAppId(app.getId()).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeApiKey(String keyValue) {
        ApiKey apiKey = apiKeyRepository.findByKeyValue(keyValue)
                .orElseThrow(() -> new ResourceNotFoundException("API Key not found"));

        apiKey.setStatus(ApiKey.ApiKeyStatus.REVOKED);
        apiKey.setRevokedAt(LocalDateTime.now());
        apiKeyRepository.save(apiKey);
    }

    @Transactional
    public ApiKeyResponse regenerateApiKey(String oldKeyValue, int validityDays) {
        ApiKey oldKey = apiKeyRepository.findByKeyValue(oldKeyValue)
                .orElseThrow(() -> new ResourceNotFoundException("API Key not found"));

        // Revoke old key
        oldKey.setStatus(ApiKey.ApiKeyStatus.REVOKED);
        oldKey.setRevokedAt(LocalDateTime.now());
        apiKeyRepository.save(oldKey);

        // Generate new key
        ApiKey newKey = generateApiKey(oldKey.getApp(), oldKey.getName(), validityDays);
        return convertToResponse(newKey);
    }

    @Transactional
    public void updateExpiredKeys() {
        apiKeyRepository.updateExpiredKeys(ApiKey.ApiKeyStatus.EXPIRED, LocalDateTime.now());
    }

    private String generateSecureApiKey() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private ApiKeyResponse convertToResponse(ApiKey apiKey) {
        return ApiKeyResponse.builder()
                .id(apiKey.getId())
                .keyValue(apiKey.getKeyValue())
                .name(apiKey.getName())
                .status(apiKey.getStatus().name())
                .expiresAt(apiKey.getExpiresAt())
                .requestCount(apiKey.getRequestCount())
                .lastUsedAt(apiKey.getLastUsedAt())
                .createdAt(apiKey.getCreatedAt())
                .build();
    }
}

