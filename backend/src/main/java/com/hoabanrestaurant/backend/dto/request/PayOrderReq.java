package com.hoabanrestaurant.backend.dto.request;


import com.hoabanrestaurant.backend.enums.PaymentMethod;

import java.math.BigDecimal;

public record PayOrderReq(
        PaymentMethod method,
        BigDecimal amount
) {
}

