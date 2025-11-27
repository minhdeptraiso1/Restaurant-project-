package com.hoabanrestaurant.backendchat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonValidator {

    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode validateOrFallback(String rawJson) {
        try {
            return mapper.readTree(rawJson);
        } catch (Exception e) {
            // fallback JSON an toàn
            String fallback = """
                {
                  "reply": "Xin lỗi, tôi chưa hiểu yêu cầu của bạn.",
                  "intent": "UNKNOWN"
                }
            """;
            try {
                return mapper.readTree(fallback);
            } catch (Exception ignore) {
                throw new RuntimeException("Fallback JSON cũng lỗi - không thể xảy ra.");
            }
        }
    }
}
