package com.hoabanrestaurant.backend.dto.request;

import java.util.UUID;

public record ChatAiReq(String sessionId, UUID userId, String message) {
}
