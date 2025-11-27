package com.hoabanrestaurant.backend.dto.response;

import com.hoabanrestaurant.backend.enums.OrderStatus;
import com.hoabanrestaurant.backend.enums.OrderTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderDto(
        UUID id,
        UUID userId,
        UUID tableId,
        OrderTypes types,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal tax,
        BigDecimal total,
        String note,
        List<OrderItemDto> items,
        String appliedVoucherCode

) {
}

