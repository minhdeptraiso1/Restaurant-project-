package com.hoabanrestaurant.backend.service;

import com.hoabanrestaurant.backend.dto.request.RedeemVoucherReq;
import com.hoabanrestaurant.backend.dto.response.UserVoucherDto;
import com.hoabanrestaurant.backend.dto.response.VoucherBriefDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface LoyaltyService {
    UserVoucherDto redeem(UUID userId, RedeemVoucherReq req);

    List<UserVoucherDto> myAvailableVouchers(UUID userId);

    UserVoucherDto redeemById(UUID userId, UUID voucherId);

    Page<VoucherBriefDto> redeemableCatalog(UUID userId, Pageable pg);

    long currentPoints(UUID userId);
}
