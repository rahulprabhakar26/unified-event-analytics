package com.analytics.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAppRequest {

    @NotBlank(message = "App name is required")
    private String name;

    @NotBlank(message = "Domain is required")
    @Pattern(regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+(/.*)?$", 
             message = "Invalid domain format")
    private String domain;

    private String description;

    @NotBlank(message = "API key name is required")
    private String apiKeyName;
}

