package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.dto.request.RedeemVoucherReq;
import com.hoabanrestaurant.backend.dto.response.UserVoucherDto;
import com.hoabanrestaurant.backend.dto.response.VoucherBriefDto;
import com.hoabanrestaurant.backend.entity.LoyaltyAccount;
import com.hoabanrestaurant.backend.entity.User;
import com.hoabanrestaurant.backend.entity.UserVoucher;
import com.hoabanrestaurant.backend.entity.Voucher;
import com.hoabanrestaurant.backend.enums.ErrorCode;
import com.hoabanrestaurant.backend.enums.VoucherStatus;
import com.hoabanrestaurant.backend.exception.BusinessException;
import com.hoabanrestaurant.backend.repository.LoyaltyAccountRepository;
import com.hoabanrestaurant.backend.repository.UserVoucherRepository;
import com.hoabanrestaurant.backend.repository.VoucherRepository;
import com.hoabanrestaurant.backend.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoyaltyServiceImpl implements LoyaltyService {

    private final VoucherRepository voucherRepo;
    private final UserVoucherRepository userVoucherRepo;
    private final LoyaltyAccountRepository loyaltyRepo;

    @Override
    @Transactional
    public UserVoucherDto redeem(UUID userId, RedeemVoucherReq req) {
        Voucher v = voucherRepo.findByCodeIgnoreCase(req.code())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Voucher không tồn tại"));
        // Kiểm tra trạng thái & thời gian
        Instant now = Instant.now();
        if (v.getStatus() != VoucherStatus.ACTIVE)
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Voucher không hoạt động");
        if (v.getStartAt() != null && now.isBefore(v.getStartAt()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Voucher chưa bắt đầu");
        if (v.getEndAt() != null && now.isAfter(v.getEndAt()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Voucher đã hết hạn");

        // Tài khoản điểm
        LoyaltyAccount acc = loyaltyRepo.findById(userId)
                .orElseGet(() -> loyaltyRepo.save(LoyaltyAccount.builder().userId(userId).points(0).build()));

        if (v.getPointCost() <= 0)
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Voucher này không cho phép đổi bằng điểm");
        if (acc.getPoints() < v.getPointCost())
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Điểm không đủ để đổi voucher");

        // Tránh cấp trùng voucher chưa dùng
        if (userVoucherRepo.existsByUser_IdAndVoucher_IdAndRedeemedFalse(userId, v.getId()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Bạn đã có voucher này trong ví");

        // Trừ điểm & cấp user_voucher
        acc.setPoints(acc.getPoints() - v.getPointCost());
        acc.setUpdatedAt(Instant.now());
        loyaltyRepo.save(acc);

        UserVoucher uv = new UserVoucher();
        uv.setId(UUID.randomUUID());
        uv.setUser(new User() {{
            setId(userId);
        }});
        uv.setVoucher(v);
        uv.setRedeemed(false);
        userVoucherRepo.save(uv);

        return new UserVoucherDto(uv.getId(), v.getCode(), v.getName(), uv.getVoucher(), uv.isRedeemed(), uv.getRedeemedAt());
    }

    @Override
    public List<UserVoucherDto> myAvailableVouchers(UUID userId) {
        return userVoucherRepo.findByUser_IdAndRedeemedFalse(userId).stream()
                .map(uv -> new UserVoucherDto(uv.getId(), uv.getVoucher().getCode(), uv.getVoucher().getName(),
                        uv.getVoucher(), uv.isRedeemed(), uv.getRedeemedAt()))
                .toList();
    }

    @Override
    public long currentPoints(UUID userId) {
        return loyaltyRepo.findById(userId).map(LoyaltyAccount::getPoints).orElse(0L);
    }

    @Override
    public Page<VoucherBriefDto> redeemableCatalog(UUID userId, Pageable pg) {
        long points = currentPoints(userId);
        var page = voucherRepo.findRedeemable(points, Instant.now(), pg);
        return page.map(v -> new VoucherBriefDto(
                v.getId(), v.getCode(), v.getName(), v.getType(),
                v.getValue(), v.getPointCost(), v.getMinOrder(), v.getMaxDiscount(),
                v.getStatus(), v.getStartAt(), v.getEndAt()
        ));
    }

    @Override
    @Transactional
    public UserVoucherDto redeemById(UUID userId, UUID voucherId) {
        var v = voucherRepo.findById(voucherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Voucher không tồn tại"));
        // Tái dùng logic kiểm tra như hàm redeem(code)
        Instant now = Instant.now();
        if (v.getStatus() != VoucherStatus.ACTIVE)
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Voucher không hoạt động");
        if (v.getStartAt() != null && now.isBefore(v.getStartAt()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Voucher chưa bắt đầu");
        if (v.getEndAt() != null && now.isAfter(v.getEndAt()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Voucher đã hết hạn");
        var acc = loyaltyRepo.findById(userId)
                .orElseGet(() -> loyaltyRepo.save(LoyaltyAccount.builder().userId(userId).points(0).build()));
        if (v.getPointCost() <= 0)
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Voucher này không cho đổi bằng điểm");
        if (acc.getPoints() < v.getPointCost())
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Điểm không đủ để đổi voucher");
        if (userVoucherRepo.existsByUser_IdAndVoucher_IdAndRedeemedFalse(userId, v.getId()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Bạn đã có voucher này trong ví");

        acc.setPoints(acc.getPoints() - v.getPointCost());
        acc.setUpdatedAt(Instant.now());
        loyaltyRepo.save(acc);

        User u = new User();
        u.setId(userId);

        var uv = userVoucherRepo.save(UserVoucher.builder()
                .id(UUID.randomUUID())
                .user(u)
                .voucher(v)
                .redeemed(false)
                .build());

        return new UserVoucherDto(uv.getId(), v.getCode(), v.getName(), uv.getVoucher(), uv.isRedeemed(), uv.getRedeemedAt());
    }
}
