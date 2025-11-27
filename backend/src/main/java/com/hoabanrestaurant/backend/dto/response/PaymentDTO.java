package com.hoabanrestaurant.backend.dto.response;

import lombok.Builder;

public abstract class PaymentDTO {
    @Builder
    public record VNPayResponse(String code, String message, String paymentUrl, String txnRef) {
    }

}
