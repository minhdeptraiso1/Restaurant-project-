// security/LoginRateLimiter.java
package com.hoabanrestaurant.backend.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimiter {
    // Cấu hình cơ bản
    private static final int MAX_ATTEMPTS = 5;        // tối đa 5 lần
    private static final long WINDOW_SEC = 300;       // trong 5 phút
    private static final long LOCK_SEC = 900;         // khoá 15 phút nếu vượt ngưỡng

    private static class Entry {
        int attempts;
        Instant windowStart;
        Instant lockedUntil;
    }

    private final Map<String, Entry> buckets = new ConcurrentHashMap<>();

    public void ensureAllowed(String key) {
        Entry e = buckets.computeIfAbsent(key, k -> {
            Entry n = new Entry();
            n.attempts = 0;
            n.windowStart = Instant.now();
            return n;
        });

        Instant now = Instant.now();

        // đang bị khoá?
        if (e.lockedUntil != null && e.lockedUntil.isAfter(now)) {
            throw new IllegalStateException("Tài khoản/tác nhân tạm bị khoá, thử lại sau.");
        }

        // reset cửa sổ
        if (e.windowStart.plusSeconds(WINDOW_SEC).isBefore(now)) {
            e.attempts = 0;
            e.windowStart = now;
        }
    }

    public void onSuccess(String key) {
        buckets.remove(key);
    }

    public void onFailure(String key) {
        Entry e = buckets.computeIfAbsent(key, k -> {
            Entry n = new Entry();
            n.windowStart = Instant.now();
            return n;
        });
        e.attempts++;
        if (e.attempts >= MAX_ATTEMPTS) {
            e.lockedUntil = Instant.now().plusSeconds(LOCK_SEC);
        }
    }
}
