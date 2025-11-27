package com.hoabanrestaurant.backend.dto.response;

import com.hoabanrestaurant.backend.enums.VoucherStatus;
import com.hoabanrestaurant.backend.enums.VoucherType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record VoucherBriefDto(
        UUID id,
        String code,
        String name,
        VoucherType type,
        BigDecimal value,
        Long pointCost,
        BigDecimal minOrder,
        BigDecimal maxDiscount,
        VoucherStatus status,
        Instant startAt,
        Instant endAt
) {
}
