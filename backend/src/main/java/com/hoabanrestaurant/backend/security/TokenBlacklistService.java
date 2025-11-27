// security/TokenBlacklistService.java
package com.hoabanrestaurant.backend.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {
    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    public void revoke(String jti, Instant exp) {
        if (jti != null && exp != null) blacklist.put(jti, exp);
    }

    public boolean isRevoked(String jti) {
        Instant exp = blacklist.get(jti);
        if (exp == null) return false;
        if (exp.isBefore(Instant.now())) {
            blacklist.remove(jti);
            return false;
        }
        return true;
    }
}
