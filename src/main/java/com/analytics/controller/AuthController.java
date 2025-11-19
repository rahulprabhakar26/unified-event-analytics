package com.analytics.controller;

import com.analytics.dto.request.RegisterAppRequest;
import com.analytics.dto.response.ApiKeyResponse;
import com.analytics.dto.response.ApiResponse;
import com.analytics.dto.response.RegisterAppResponse;
import com.analytics.service.ApiKeyService;
import com.analytics.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & API Key Management", description = "Endpoints for user authentication and API key management")
public class AuthController {

    private final AppService appService;
    private final ApiKeyService apiKeyService;

    @PostMapping("/register")
    @Operation(summary = "Register a new app and generate API key", 
               description = "Registers a new website/app and generates an API key for authentication",
               security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<RegisterAppResponse>> registerApp(
            @Valid @RequestBody RegisterAppRequest request,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        RegisterAppResponse response = appService.registerApp(request, userEmail);
        return ResponseEntity.ok(ApiResponse.success("App registered successfully", response));
    }

    @GetMapping("/api-keys")
    @Operation(summary = "Get API keys for an app", 
               description = "Retrieves all API keys associated with a specific app",
               security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<List<ApiKeyResponse>>> getApiKeys(
            @RequestParam String appId,
            Authentication authentication) {
        
        List<ApiKeyResponse> apiKeys = apiKeyService.getApiKeysByAppId(appId);
        return ResponseEntity.ok(ApiResponse.success(apiKeys));
    }

    @PostMapping("/revoke")
    @Operation(summary = "Revoke an API key", 
               description = "Revokes an API key to prevent further use",
               security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<String>> revokeApiKey(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String apiKey = request.get("apiKey");
        apiKeyService.revokeApiKey(apiKey);
        return ResponseEntity.ok(ApiResponse.success("API key revoked successfully", null));
    }

    @PostMapping("/regenerate")
    @Operation(summary = "Regenerate an API key", 
               description = "Regenerates a new API key while revoking the old one",
               security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<ApiKeyResponse>> regenerateApiKey(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String oldApiKey = request.get("apiKey");
        int validityDays = Integer.parseInt(request.getOrDefault("validityDays", "365"));
        
        ApiKeyResponse newKey = apiKeyService.regenerateApiKey(oldApiKey, validityDays);
        return ResponseEntity.ok(ApiResponse.success("API key regenerated successfully", newKey));
    }

    @GetMapping("/google/callback")
    @Operation(summary = "Google OAuth callback", 
               description = "Callback endpoint for Google OAuth authentication")
    public ResponseEntity<ApiResponse<Map<String, String>>> googleCallback(
            @RequestParam String token) {
        
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", 
                Map.of("token", token, "message", "Use this token in Authorization header as 'Bearer <token>'")));
    }
}

