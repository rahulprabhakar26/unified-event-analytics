package com.analytics.service;

import com.analytics.entity.ApiKey;
import com.analytics.entity.App;
import com.analytics.entity.User;
import com.analytics.exception.ResourceNotFoundException;
import com.analytics.repository.ApiKeyRepository;
import com.analytics.repository.AppRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyServiceTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private AppRepository appRepository;

    @InjectMocks
    private ApiKeyService apiKeyService;

    private App testApp;
    private ApiKey testApiKey;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        testApp = App.builder()
                .id(1L)
                .appId("app_123")
                .name("Test App")
                .domain("example.com")
                .user(testUser)
                .build();

        testApiKey = ApiKey.builder()
                .id(1L)
                .keyValue("test-api-key-123")
                .name("Test Key")
                .app(testApp)
                .status(ApiKey.ApiKeyStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .requestCount(0L)
                .build();
    }

    @Test
    void testGenerateApiKey() {
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(testApiKey);

        ApiKey result = apiKeyService.generateApiKey(testApp, "Test Key", 365);

        assertNotNull(result);
        verify(apiKeyRepository, times(1)).save(any(ApiKey.class));
    }

    @Test
    void testRevokeApiKey() {
        when(apiKeyRepository.findByKeyValue("test-api-key-123"))
                .thenReturn(Optional.of(testApiKey));
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(testApiKey);

        apiKeyService.revokeApiKey("test-api-key-123");

        verify(apiKeyRepository, times(1)).save(any(ApiKey.class));
    }

    @Test
    void testRevokeApiKey_NotFound() {
        when(apiKeyRepository.findByKeyValue("non-existent-key"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> apiKeyService.revokeApiKey("non-existent-key"));
    }

    @Test
    void testRegenerateApiKey() {
        when(apiKeyRepository.findByKeyValue("test-api-key-123"))
                .thenReturn(Optional.of(testApiKey));
        when(apiKeyRepository.save(any(ApiKey.class))).thenReturn(testApiKey);

        apiKeyService.regenerateApiKey("test-api-key-123", 365);

        verify(apiKeyRepository, times(2)).save(any(ApiKey.class));
    }
}

