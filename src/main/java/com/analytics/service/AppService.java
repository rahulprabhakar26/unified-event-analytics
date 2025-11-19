package com.analytics.service;

import com.analytics.dto.request.RegisterAppRequest;
import com.analytics.dto.response.RegisterAppResponse;
import com.analytics.entity.ApiKey;
import com.analytics.entity.App;
import com.analytics.entity.User;
import com.analytics.exception.ResourceNotFoundException;
import com.analytics.repository.AppRepository;
import com.analytics.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppService {

    private final AppRepository appRepository;
    private final UserRepository userRepository;
    private final ApiKeyService apiKeyService;

    @Transactional
    public RegisterAppResponse registerApp(RegisterAppRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create app
        App app = App.builder()
                .appId(generateAppId())
                .name(request.getName())
                .domain(request.getDomain())
                .description(request.getDescription())
                .user(user)
                .build();

        app = appRepository.save(app);

        // Generate API key
        ApiKey apiKey = apiKeyService.generateApiKey(app, request.getApiKeyName(), 365);

        return RegisterAppResponse.builder()
                .appId(app.getAppId())
                .name(app.getName())
                .domain(app.getDomain())
                .apiKey(apiKey.getKeyValue())
                .apiKeyExpiresAt(apiKey.getExpiresAt())
                .createdAt(app.getCreatedAt())
                .message("App registered successfully")
                .build();
    }

    public App getAppByAppId(String appId) {
        return appRepository.findByAppId(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App not found"));
    }

    private String generateAppId() {
        return "app_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}

