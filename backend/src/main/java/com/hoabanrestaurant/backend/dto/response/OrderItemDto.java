package com.hoabanrestaurant.backend.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDto(
        UUID id, String itemType, UUID itemId,
        String name, BigDecimal unitPrice, int quantity, BigDecimal lineTotal
) {
}