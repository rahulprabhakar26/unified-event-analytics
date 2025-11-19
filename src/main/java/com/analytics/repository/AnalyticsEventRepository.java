package com.analytics.repository;

import com.analytics.entity.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {
    
    @Query("SELECT COUNT(e) FROM AnalyticsEvent e WHERE e.app.id = :appId AND e.eventName = :eventName " +
           "AND e.timestamp BETWEEN :startDate AND :endDate")
    Long countByAppIdAndEventNameAndTimestampBetween(
        @Param("appId") Long appId,
        @Param("eventName") String eventName,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(DISTINCT e.userId) FROM AnalyticsEvent e WHERE e.app.id = :appId AND e.eventName = :eventName " +
           "AND e.timestamp BETWEEN :startDate AND :endDate AND e.userId IS NOT NULL")
    Long countDistinctUsersByAppIdAndEventNameAndTimestampBetween(
        @Param("appId") Long appId,
        @Param("eventName") String eventName,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT e.device, COUNT(e) FROM AnalyticsEvent e WHERE e.app.id = :appId AND e.eventName = :eventName " +
           "AND e.timestamp BETWEEN :startDate AND :endDate GROUP BY e.device")
    List<Object[]> countByDeviceGrouped(
        @Param("appId") Long appId,
        @Param("eventName") String eventName,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(e) FROM AnalyticsEvent e WHERE e.eventName = :eventName " +
           "AND e.timestamp BETWEEN :startDate AND :endDate")
    Long countByEventNameAndTimestampBetween(
        @Param("eventName") String eventName,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(DISTINCT e.userId) FROM AnalyticsEvent e WHERE e.eventName = :eventName " +
           "AND e.timestamp BETWEEN :startDate AND :endDate AND e.userId IS NOT NULL")
    Long countDistinctUsersByEventNameAndTimestampBetween(
        @Param("eventName") String eventName,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT e.device, COUNT(e) FROM AnalyticsEvent e WHERE e.eventName = :eventName " +
           "AND e.timestamp BETWEEN :startDate AND :endDate GROUP BY e.device")
    List<Object[]> countAllAppsByDeviceGrouped(
        @Param("eventName") String eventName,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(e) FROM AnalyticsEvent e WHERE e.userId = :userId")
    Long countByUserId(@Param("userId") String userId);
    
    List<AnalyticsEvent> findTop10ByUserIdOrderByTimestampDesc(String userId);
    
    @Query("SELECT e FROM AnalyticsEvent e WHERE e.userId = :userId ORDER BY e.timestamp DESC")
    List<AnalyticsEvent> findByUserIdOrderByTimestampDesc(@Param("userId") String userId);
}

