package com.hoabanrestaurant.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ReviewRes(
        UUID id,
        String customerName,
        int rating,
        String comment,
        Instant createdAt,
        String avatarUrl
) {
}
