package com.analytics.repository;

import com.analytics.entity.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppRepository extends JpaRepository<App, Long> {
    
    Optional<App> findByAppId(String appId);
    
    List<App> findByUserId(Long userId);
    
    boolean existsByAppId(String appId);
    
    boolean existsByDomain(String domain);
}

