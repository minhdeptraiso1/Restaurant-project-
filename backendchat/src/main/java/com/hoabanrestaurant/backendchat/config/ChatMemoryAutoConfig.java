package com.hoabanrestaurant.backendchat.config;

import com.hoabanrestaurant.backendchat.memory.ChatMemoryStore;

import com.hoabanrestaurant.backendchat.memory.ChatMemoryStoreIpml.InMemoryMemoryStore;
import com.hoabanrestaurant.backendchat.memory.ChatMemoryStoreIpml.RedisMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
@Configuration
public class ChatMemoryAutoConfig {

    @Bean
    public ChatMemoryStore memoryStore(StringRedisTemplate redisTemplate) {
        try {
            // Try ping Redis
            redisTemplate.hasKey("chat:test");

            log.info("🔵 Redis available → Using RedisMemoryStore");
            return new RedisMemoryStore(redisTemplate);

        } catch (Exception ex) {
            log.warn("🟡 Redis unavailable → Using InMemoryMemoryStore");
            return new InMemoryMemoryStore();
        }
    }
}
