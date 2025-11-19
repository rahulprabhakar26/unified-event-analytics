package com.analytics.service;

import com.analytics.dto.request.EventCollectionRequest;
import com.analytics.dto.request.EventSummaryRequest;
import com.analytics.dto.response.EventSummaryResponse;
import com.analytics.dto.response.UserStatsResponse;
import com.analytics.entity.AnalyticsEvent;
import com.analytics.entity.App;
import com.analytics.entity.User;
import com.analytics.repository.AnalyticsEventRepository;
import com.analytics.repository.AppRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsEventRepository eventRepository;

    @Mock
    private AppRepository appRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private App testApp;
    private EventCollectionRequest eventRequest;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .build();

        testApp = App.builder()
                .id(1L)
                .appId("app_123")
                .name("Test App")
                .domain("example.com")
                .user(testUser)
                .build();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("browser", "Chrome");
        metadata.put("os", "Windows");

        eventRequest = EventCollectionRequest.builder()
                .event("button_click")
                .url("https://example.com")
                .referrer("https://google.com")
                .device("desktop")
                .ipAddress("192.168.1.1")
                .userId("user123")
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .build();
    }

    @Test
    void testCollectEvent() {
        when(eventRepository.save(any(AnalyticsEvent.class)))
                .thenReturn(new AnalyticsEvent());

        analyticsService.collectEvent(eventRequest, testApp);

        verify(eventRepository, times(1)).save(any(AnalyticsEvent.class));
    }

    @Test
    void testGetEventSummary_WithAppId() {
        when(appRepository.findByAppId("app_123")).thenReturn(Optional.of(testApp));
        when(eventRepository.countByAppIdAndEventNameAndTimestampBetween(
                any(), any(), any(), any())).thenReturn(100L);
        when(eventRepository.countDistinctUsersByAppIdAndEventNameAndTimestampBetween(
                any(), any(), any(), any())).thenReturn(50L);
        when(eventRepository.countByDeviceGrouped(any(), any(), any(), any()))
                .thenReturn(Arrays.asList(
                        new Object[]{"mobile", 60L},
                        new Object[]{"desktop", 40L}
                ));

        EventSummaryRequest request = EventSummaryRequest.builder()
                .event("button_click")
                .appId("app_123")
                .build();

        EventSummaryResponse response = analyticsService.getEventSummary(request, "test@example.com");

        assertNotNull(response);
        assertEquals("button_click", response.getEvent());
        assertEquals(100L, response.getCount());
        assertEquals(50L, response.getUniqueUsers());
        assertNotNull(response.getDeviceData());
    }

    @Test
    void testGetUserStats() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("browser", "Chrome");
        metadata.put("os", "Windows");

        AnalyticsEvent event = AnalyticsEvent.builder()
                .userId("user123")
                .ipAddress("192.168.1.1")
                .metadata(metadata)
                .build();

        when(eventRepository.countByUserId("user123")).thenReturn(150L);
        when(eventRepository.findTop10ByUserIdOrderByTimestampDesc("user123"))
                .thenReturn(Collections.singletonList(event));

        UserStatsResponse response = analyticsService.getUserStats("user123");

        assertNotNull(response);
        assertEquals("user123", response.getUserId());
        assertEquals(150L, response.getTotalEvents());
        assertEquals("192.168.1.1", response.getIpAddress());
    }
}

