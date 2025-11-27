package com.hoabanrestaurant.backendchat.memory;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatMemoryStore {

    private final RedisTemplate<String, String> redis;

    private String key(String sessionId) {
        return "chat:session:" + sessionId;
    }

    public void addUserMessage(String sessionId, String msg) {
        redis.opsForList().rightPush(key(sessionId), "USER: " + msg);
    }

    public void addAiMessage(String sessionId, String msg) {
        redis.opsForList().rightPush(key(sessionId), "AI: " + msg);
    }

    public List<String> getHistory(String sessionId) {
        List<String> history = redis.opsForList().range(key(sessionId), 0, -1);
        return history != null ? history : new ArrayList<>();
    }

    public void clear(String sessionId) {
        redis.delete(key(sessionId));
    }
}
