package com.hoabanrestaurant.backendchat.dto;

import java.util.UUID;

public record ChatReq(String sessionId, UUID userId, String message) {
}
