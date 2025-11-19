package com.analytics.controller;

import com.analytics.dto.request.EventCollectionRequest;
import com.analytics.dto.response.EventSummaryResponse;
import com.analytics.entity.App;
import com.analytics.entity.User;
import com.analytics.service.AnalyticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnalyticsService analyticsService;

    private EventCollectionRequest eventRequest;

    @BeforeEach
    void setUp() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("browser", "Chrome");

        eventRequest = EventCollectionRequest.builder()
                .event("button_click")
                .url("https://example.com")
                .device("desktop")
                .ipAddress("192.168.1.1")
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .build();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetEventSummary() throws Exception {
        EventSummaryResponse response = EventSummaryResponse.builder()
                .event("button_click")
                .count(100L)
                .uniqueUsers(50L)
                .deviceData(Map.of("mobile", 60L, "desktop", 40L))
                .build();

        when(analyticsService.getEventSummary(any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/analytics/event-summary")
                        .param("event", "button_click")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.event").value("button_click"))
                .andExpect(jsonPath("$.data.count").value(100));
    }
}

