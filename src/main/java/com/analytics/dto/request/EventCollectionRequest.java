package com.analytics.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCollectionRequest {

    @NotBlank(message = "Event name is required")
    private String event;

    @NotBlank(message = "URL is required")
    private String url;

    private String referrer;

    @NotBlank(message = "Device type is required")
    private String device;

    @NotBlank(message = "IP address is required")
    private String ipAddress;

    private String userId;

    @NotNull(message = "Timestamp is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;

    private Map<String, Object> metadata;

    private String userAgent;
}

