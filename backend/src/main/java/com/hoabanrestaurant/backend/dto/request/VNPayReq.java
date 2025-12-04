package com.hoabanrestaurant.backend.dto.request;

import java.util.UUID;

public record VNPayReq(
        UUID orderId,
        String bankCode,
        String language
) {
}