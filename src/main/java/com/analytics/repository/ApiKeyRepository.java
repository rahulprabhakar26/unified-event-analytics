package com.analytics.repository;

import com.analytics.entity.ApiKey;
import com.analytics.entity.ApiKey.ApiKeyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    
    Optional<ApiKey> findByKeyValue(String keyValue);
    
    List<ApiKey> findByAppId(Long appId);
    
    List<ApiKey> findByAppIdAndStatus(Long appId, ApiKeyStatus status);
    
    @Modifying
    @Query("UPDATE ApiKey ak SET ak.requestCount = ak.requestCount + 1, ak.lastUsedAt = :lastUsed WHERE ak.keyValue = :keyValue")
    void incrementRequestCount(@Param("keyValue") String keyValue, @Param("lastUsed") LocalDateTime lastUsed);
    
    @Modifying
    @Query("UPDATE ApiKey ak SET ak.status = :status WHERE ak.expiresAt < :now AND ak.status = 'ACTIVE'")
    int updateExpiredKeys(@Param("status") ApiKeyStatus status, @Param("now") LocalDateTime now);
}

