package com.analytics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAppResponse {

    private String appId;
    
    private String name;
    
    private String domain;
    
    private String apiKey;
    
    private LocalDateTime apiKeyExpiresAt;
    
    private LocalDateTime createdAt;
    
    private String message;
}

