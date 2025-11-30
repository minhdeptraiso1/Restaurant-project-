package com.hoabanrestaurant.backend.dto.response;

public record VNPayResponse(
        String code,
        String message,
        String paymentUrl
) {
}
