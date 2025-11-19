package com.analytics.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "analytics_events", indexes = {
    @Index(name = "idx_app_id", columnList = "app_id"),
    @Index(name = "idx_event_name", columnList = "eventName"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_device", columnList = "device"),
    @Index(name = "idx_app_event_timestamp", columnList = "app_id,eventName,timestamp")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", nullable = false)
    private App app;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(length = 2048)
    private String referrer;

    @Column(nullable = false)
    private String device;

    @Column(nullable = false)
    private String ipAddress;

    private String userId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> metadata;

    @Column(length = 500)
    private String userAgent;

    private String country;

    private String city;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

