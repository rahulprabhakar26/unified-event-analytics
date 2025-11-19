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
public class ApiKeyResponse {

    private Long id;
    
    private String keyValue;
    
    private String name;
    
    private String status;
    
    private LocalDateTime expiresAt;
    
    private Long requestCount;
    
    private LocalDateTime lastUsedAt;
    
    private LocalDateTime createdAt;
}

