package com.hoabanrestaurant.backendchat.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    private static final int LIMIT = 20;         // 20 requests
    private static final long WINDOW = 60_000;   // 60s

    static class RateInfo {
        int count;
        long startTime = Instant.now().toEpochMilli();
    }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> rateLimitFilter() {
        FilterRegistrationBean<OncePerRequestFilter> bean = new FilterRegistrationBean<>();

        bean.setFilter(new OncePerRequestFilter() {

            private final Map<String, RateInfo> users = new ConcurrentHashMap<>();

            @Override
            protected void doFilterInternal(
                    HttpServletRequest req,
                    HttpServletResponse res,
                    FilterChain chain) throws ServletException, IOException {

                String ip = req.getRemoteAddr();
                users.putIfAbsent(ip, new RateInfo());

                RateInfo info = users.get(ip);

                long now = Instant.now().toEpochMilli();

                // Reset window
                if (now - info.startTime > WINDOW) {
                    info.count = 0;
                    info.startTime = now;
                }

                if (info.count >= LIMIT) {
                    res.setStatus(429);
                    res.getWriter().write("Too many requests - please wait");
                    return;
                }

                info.count++;

                chain.doFilter(req, res);
            }
        });

        bean.addUrlPatterns("/api/ai/*");
        return bean;
    }
}
