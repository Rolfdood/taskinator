package com.taskinator.taskinator.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Simple in-memory rate limiter for authentication endpoints.
 * Allows a configurable number of requests per window per IP address on /api/v1/auth/**.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final int maxRequests;
    private final Duration window;
    private final boolean enabled;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitingFilter(
        @Value("${application.rate-limit.enabled:true}") boolean enabled,
        @Value("${application.rate-limit.max-requests:5}") int maxRequests,
        @Value("${application.rate-limit.window-seconds:60}") long windowSeconds) {
        this.enabled = enabled;
        this.maxRequests = maxRequests;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        if (!enabled || !isAuthEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = extractClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> new Bucket(maxRequests, window));

        if (!bucket.tryConsume()) {
            writeRateLimitResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && path.startsWith("/api/v1/auth/");
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private void writeRateLimitResponse(HttpServletResponse response) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Please try again later.");
        problem.setTitle("Rate Limit Exceeded");
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"title\":\"Rate Limit Exceeded\",\"detail\":\"Too many requests. Please try again later.\"}");
    }

    private static final class Bucket {

        private final int maxRequests;
        private final Duration window;
        private final AtomicInteger counter = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        Bucket(int maxRequests, Duration window) {
            this.maxRequests = maxRequests;
            this.window = window;
        }

        synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            if (now - windowStart > window.toMillis()) {
                windowStart = now;
                counter.set(0);
            }
            return counter.incrementAndGet() <= maxRequests;
        }
    }
}
