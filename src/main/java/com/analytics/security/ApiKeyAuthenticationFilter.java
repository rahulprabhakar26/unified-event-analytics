package com.analytics.security;

import com.analytics.entity.ApiKey;
import com.analytics.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Only check API key for analytics collection endpoint
        if (!request.getRequestURI().startsWith("/api/analytics/collect")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String apiKey = request.getHeader("X-API-Key");

        if (apiKey == null || apiKey.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "API Key is missing");
            return;
        }

        try {
            ApiKey key = apiKeyRepository.findByKeyValue(apiKey)
                    .orElse(null);

            if (key == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
                return;
            }

            if (!key.isValid()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "API Key is expired or revoked");
                return;
            }

            // Update usage statistics asynchronously would be better, but for simplicity:
            apiKeyRepository.incrementRequestCount(apiKey, LocalDateTime.now());

            // Set authentication in context
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(key.getApp(), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Store API key in request attribute for later use
            request.setAttribute("apiKey", key);
            request.setAttribute("app", key.getApp());

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error validating API Key");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

