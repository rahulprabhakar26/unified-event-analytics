package com.analytics.service;

import com.analytics.dto.request.EventCollectionRequest;
import com.analytics.dto.request.EventSummaryRequest;
import com.analytics.dto.response.EventSummaryResponse;
import com.analytics.dto.response.UserStatsResponse;
import com.analytics.entity.AnalyticsEvent;
import com.analytics.entity.App;
import com.analytics.repository.AnalyticsEventRepository;
import com.analytics.repository.AppRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsEventRepository eventRepository;
    private final AppRepository appRepository;

    @Transactional
    public void collectEvent(EventCollectionRequest request, App app) {
        AnalyticsEvent event = AnalyticsEvent.builder()
                .app(app)
                .eventName(request.getEvent())
                .url(request.getUrl())
                .referrer(request.getReferrer())
                .device(request.getDevice())
                .ipAddress(request.getIpAddress())
                .userId(request.getUserId())
                .timestamp(request.getTimestamp())
                .metadata(request.getMetadata())
                .userAgent(request.getUserAgent())
                .build();

        eventRepository.save(event);
    }

    @Cacheable(value = "eventSummary", key = "#request.event + '_' + #request.appId + '_' + #request.startDate + '_' + #request.endDate + '_' + #userEmail")
    public EventSummaryResponse getEventSummary(EventSummaryRequest request, String userEmail) {
        LocalDateTime startDate = parseDate(request.getStartDate(), true);
        LocalDateTime endDate = parseDate(request.getEndDate(), false);

        Long count;
        Long uniqueUsers;
        Map<String, Long> deviceData = new HashMap<>();

        if (request.getAppId() != null && !request.getAppId().isEmpty()) {
            // Query for specific app
            App app = appRepository.findByAppId(request.getAppId())
                    .orElseThrow(() -> new RuntimeException("App not found"));

            count = eventRepository.countByAppIdAndEventNameAndTimestampBetween(
                    app.getId(), request.getEvent(), startDate, endDate);

            uniqueUsers = eventRepository.countDistinctUsersByAppIdAndEventNameAndTimestampBetween(
                    app.getId(), request.getEvent(), startDate, endDate);

            List<Object[]> deviceCounts = eventRepository.countByDeviceGrouped(
                    app.getId(), request.getEvent(), startDate, endDate);

            for (Object[] row : deviceCounts) {
                deviceData.put((String) row[0], (Long) row[1]);
            }
        } else {
            // Query across all apps belonging to the user
            count = eventRepository.countByEventNameAndTimestampBetween(
                    request.getEvent(), startDate, endDate);

            uniqueUsers = eventRepository.countDistinctUsersByEventNameAndTimestampBetween(
                    request.getEvent(), startDate, endDate);

            List<Object[]> deviceCounts = eventRepository.countAllAppsByDeviceGrouped(
                    request.getEvent(), startDate, endDate);

            for (Object[] row : deviceCounts) {
                deviceData.put((String) row[0], (Long) row[1]);
            }
        }

        return EventSummaryResponse.builder()
                .event(request.getEvent())
                .count(count)
                .uniqueUsers(uniqueUsers)
                .deviceData(deviceData)
                .build();
    }

    @Cacheable(value = "userStats", key = "#userId")
    public UserStatsResponse getUserStats(String userId) {
        Long totalEvents = eventRepository.countByUserId(userId);

        List<AnalyticsEvent> recentEvents = eventRepository.findTop10ByUserIdOrderByTimestampDesc(userId);

        Map<String, Object> deviceDetails = new HashMap<>();
        String ipAddress = null;

        if (!recentEvents.isEmpty()) {
            AnalyticsEvent latestEvent = recentEvents.get(0);
            if (latestEvent.getMetadata() != null) {
                deviceDetails.put("browser", latestEvent.getMetadata().get("browser"));
                deviceDetails.put("os", latestEvent.getMetadata().get("os"));
                deviceDetails.put("screenSize", latestEvent.getMetadata().get("screenSize"));
            }
            ipAddress = latestEvent.getIpAddress();
        }

        return UserStatsResponse.builder()
                .userId(userId)
                .totalEvents(totalEvents)
                .deviceDetails(deviceDetails)
                .ipAddress(ipAddress)
                .build();
    }

    private LocalDateTime parseDate(String dateStr, boolean isStartDate) {
        if (dateStr == null || dateStr.isEmpty()) {
            // Default to last 30 days
            if (isStartDate) {
                return LocalDateTime.now().minusDays(30);
            } else {
                return LocalDateTime.now();
            }
        }

        LocalDate date = LocalDate.parse(dateStr);
        return isStartDate ? date.atStartOfDay() : date.atTime(LocalTime.MAX);
    }
}

