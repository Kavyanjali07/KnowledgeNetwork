package com.knowledgenetwork.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();
    private final Map<String, Long> windows = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.enabled:false}")
    private boolean enabled;

    @Value("${app.rate-limit.requests-per-minute:120}")
    private int requestsPerMinute;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!enabled) {
            return true;
        }

        String key = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        long window = now / 60_000L;

        windows.putIfAbsent(key, window);
        AtomicInteger count = counters.computeIfAbsent(key, ignored -> new AtomicInteger());

        if (windows.get(key) != window) {
            windows.put(key, window);
            count.set(0);
        }

        if (count.incrementAndGet() > requestsPerMinute) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests\"}");
            return false;
        }

        return true;
    }
}
