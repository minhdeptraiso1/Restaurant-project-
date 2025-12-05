package com.hoabanrestaurant.backendchat.memory.ChatMemoryStoreIpml;

import com.hoabanrestaurant.backendchat.memory.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@RequiredArgsConstructor
public class RedisMemoryStore implements ChatMemoryStore {

    private final StringRedisTemplate redis;

    /* ==============================
     * REDIS KEYS
     * ============================== */
    private String keyHistory(String sessionId) {
        return "chat:session:" + sessionId + ":history";
    }

    private String keyPending(String sessionId) {
        return "chat:session:" + sessionId + ":pendingBooking";
    }

    private String keyConfirmKeyword(String sessionId) {
        return "chat:session:" + sessionId + ":confirmKey";
    }


    /* ==============================
     * 1) HISTORY
     * ============================== */

    @Override
    public void addUserMessage(String sessionId, String msg) {
        redis.opsForList().rightPush(keyHistory(sessionId), "USER: " + msg);
    }

    @Override
    public void addAiMessage(String sessionId, String msg) {
        redis.opsForList().rightPush(keyHistory(sessionId), "AI: " + msg);
    }

    @Override
    public List<String> getHistory(String sessionId) {
        List<String> h = redis.opsForList().range(keyHistory(sessionId), 0, -1);
        return h != null ? h : List.of();
    }


    /* ==============================
     * 2) CLEAR ALL
     * ============================== */
    @Override
    public void clear(String sessionId) {
        redis.delete(keyHistory(sessionId));
        redis.delete(keyPending(sessionId));
        redis.delete(keyConfirmKeyword(sessionId));
    }


    /* ==============================
     * 3) PENDING BOOKING JSON
     * ============================== */

    @Override
    public void savePendingBooking(String sessionId, String json) {
        redis.opsForValue().set(keyPending(sessionId), json);
    }

    @Override
    public String getPendingBooking(String sessionId) {
        return redis.opsForValue().get(keyPending(sessionId));
    }


    /* ==============================
     * 4) CONFIRM KEYWORD
     * ============================== */

    @Override
    public void saveConfirmKeyword(String sessionId, String keyword) {
        redis.opsForValue().set(keyConfirmKeyword(sessionId), keyword.toLowerCase());
    }

    @Override
    public String getConfirmKeyword(String sessionId) {
        return redis.opsForValue().get(keyConfirmKeyword(sessionId));
    }

    @Override
    public void clearConfirmKeyword(String sessionId) {
        redis.delete(keyConfirmKeyword(sessionId));
    }
}
