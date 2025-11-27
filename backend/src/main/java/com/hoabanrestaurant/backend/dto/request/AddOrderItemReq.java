package com.hoabanrestaurant.backend.dto.request;

import java.util.UUID;

public record AddOrderItemReq(
        String itemType,
        UUID itemId,
        int quantity
) {
}

