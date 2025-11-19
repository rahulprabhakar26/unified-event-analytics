package com.analytics.controller;

import com.analytics.dto.request.EventCollectionRequest;
import com.analytics.dto.request.EventSummaryRequest;
import com.analytics.dto.response.ApiResponse;
import com.analytics.dto.response.EventSummaryResponse;
import com.analytics.dto.response.UserStatsResponse;
import com.analytics.entity.App;
import com.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for analytics event collection and reporting")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/collect")
    @Operation(summary = "Collect analytics event", 
               description = "Submits analytics events from a website or mobile app. Requires API key in X-API-Key header",
               security = @SecurityRequirement(name = "api-key"))
    public ResponseEntity<ApiResponse<String>> collectEvent(
            @Valid @RequestBody EventCollectionRequest request,
            HttpServletRequest httpRequest) {
        
        App app = (App) httpRequest.getAttribute("app");
        analyticsService.collectEvent(request, app);
        
        return ResponseEntity.ok(ApiResponse.success("Event collected successfully", null));
    }

    @GetMapping("/event-summary")
    @Operation(summary = "Get event summary", 
               description = "Retrieves analytics summary for a specific event type with optional filters",
               security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<EventSummaryResponse>> getEventSummary(
            @Parameter(description = "Event name to filter") @RequestParam String event,
            @Parameter(description = "Start date (YYYY-MM-DD)") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (YYYY-MM-DD)") @RequestParam(required = false) String endDate,
            @Parameter(description = "App ID to filter (optional, if not provided fetches across all apps)") 
            @RequestParam(required = false) String appId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        
        EventSummaryRequest summaryRequest = EventSummaryRequest.builder()
                .event(event)
                .startDate(startDate)
                .endDate(endDate)
                .appId(appId)
                .build();
        
        EventSummaryResponse response = analyticsService.getEventSummary(summaryRequest, userEmail);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user-stats")
    @Operation(summary = "Get user statistics", 
               description = "Returns stats based on unique users with event counts and device details",
               security = @SecurityRequirement(name = "bearer-jwt"))
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats(
            @Parameter(description = "User ID to fetch statistics for") @RequestParam String userId,
            Authentication authentication) {
        
        UserStatsResponse response = analyticsService.getUserStats(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

