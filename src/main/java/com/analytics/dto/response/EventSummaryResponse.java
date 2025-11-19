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
public class EventSummaryResponse {

    private String event;
    
    private Long count;
    
    private Long uniqueUsers;
    
    private Map<String, Long> deviceData;
}

