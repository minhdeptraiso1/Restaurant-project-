package com.hoabanrestaurant.backend.controller;

import com.hoabanrestaurant.backend.dto.request.RedeemVoucherReq;
import com.hoabanrestaurant.backend.dto.response.UserVoucherDto;
import com.hoabanrestaurant.backend.dto.response.VoucherBriefDto;
import com.hoabanrestaurant.backend.exception.ApiResponse;
import com.hoabanrestaurant.backend.service.LoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;


    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/me/points")
    public ApiResponse<Long> myPoints(@AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString((String) jwt.getClaim("uid"));
        return ApiResponse.ok(loyaltyService.currentPoints(uid));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/redeem")
    public ApiResponse<com.hoabanrestaurant.backend.dto.response.UserVoucherDto> redeem(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RedeemVoucherReq req) {
        UUID uid = UUID.fromString((String) jwt.getClaim("uid"));
        return ApiResponse.ok(loyaltyService.redeem(uid, req));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/me/vouchers")
    public ApiResponse<List<UserVoucherDto>> myVouchers(
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString((String) jwt.getClaim("uid"));
        return ApiResponse.ok(loyaltyService.myAvailableVouchers(uid));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/catalog")
    public ApiResponse<Page<VoucherBriefDto>> catalog(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UUID uid = UUID.fromString((String) jwt.getClaim("uid"));
        var pg = org.springframework.data.domain.PageRequest.of(page, size);
        return ApiResponse.ok(loyaltyService.redeemableCatalog(uid, pg));
    }
    
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/redeem/{voucherId}")
    public ApiResponse<UserVoucherDto> redeemById(@AuthenticationPrincipal Jwt jwt,
                                                  @PathVariable UUID voucherId) {
        UUID uid = UUID.fromString((String) jwt.getClaim("uid"));
        return ApiResponse.ok(loyaltyService.redeemById(uid, voucherId));
    }
}
