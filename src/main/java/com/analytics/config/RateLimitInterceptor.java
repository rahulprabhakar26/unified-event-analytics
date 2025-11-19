package com.analytics.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitingConfig rateLimitingConfig;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {

        String apiKey = request.getHeader("X-API-Key");
        String authHeader = request.getHeader("Authorization");
        
        // Determine rate limit based on authentication method
        long capacity;
        Duration refillDuration = Duration.ofMinutes(1);
        String key;

        if (apiKey != null && !apiKey.isEmpty()) {
            // API Key authentication - higher limit
            capacity = 1000;
            key = "api_key_" + apiKey;
        } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // JWT authentication - moderate limit
            capacity = 200;
            key = "jwt_" + authHeader.substring(7, Math.min(authHeader.length(), 20));
        } else {
            // No authentication - lower limit
            capacity = 100;
            String ip = getClientIP(request);
            key = "ip_" + ip;
        }

        Bucket bucket = rateLimitingConfig.resolveBucket(key, capacity, refillDuration);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), 
                    "You have exhausted your API request quota");
            return false;
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}

