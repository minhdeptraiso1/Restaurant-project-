package com.hoabanrestaurant.backend.dto.request;

import com.hoabanrestaurant.backend.enums.VoucherStatus;
import com.hoabanrestaurant.backend.enums.VoucherType;

import java.math.BigDecimal;
import java.time.Instant;

// dto/request/UpdateVoucherReq.java
public record UpdateVoucherReq(
        String name, VoucherType type, BigDecimal value,
        BigDecimal minOrder, BigDecimal maxDiscount,
        Long pointCost, Instant startAt, Instant endAt,
        VoucherStatus status                       // ACTIVE/INACTIVE
) {
}

