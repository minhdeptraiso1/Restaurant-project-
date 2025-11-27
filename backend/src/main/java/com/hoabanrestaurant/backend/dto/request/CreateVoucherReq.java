package com.hoabanrestaurant.backend.dto.request;

import com.hoabanrestaurant.backend.enums.VoucherType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

// dto/request/CreateVoucherReq.java
public record CreateVoucherReq(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull VoucherType type,          // PERCENT | FIXED
        @NotNull BigDecimal value,          // % hoặc số tiền
        @NotNull BigDecimal minOrder,
        BigDecimal maxDiscount,              // chỉ áp với PERCENT (optional)
        @NotNull Long pointCost,            // điểm cần để đổi (>=0)
        Instant startAt, Instant endAt
) {
}

