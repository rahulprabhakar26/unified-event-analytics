package com.analytics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {

    private String userId;
    
    private Long totalEvents;
    
    private Map<String, Object> deviceDetails;
    
    private String ipAddress;
}

