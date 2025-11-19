package com.analytics.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSummaryRequest {

    private String event;
    
    private String startDate;
    
    private String endDate;
    
    private String appId;
}

