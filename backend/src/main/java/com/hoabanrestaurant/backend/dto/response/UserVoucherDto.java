package com.hoabanrestaurant.backend.dto.response;

import com.hoabanrestaurant.backend.entity.Voucher;

import java.time.Instant;
import java.util.UUID;

public record UserVoucherDto(
        UUID id, String code, String name, Voucher voucher,
        boolean redeemed, Instant redeemedAt
) {
}
