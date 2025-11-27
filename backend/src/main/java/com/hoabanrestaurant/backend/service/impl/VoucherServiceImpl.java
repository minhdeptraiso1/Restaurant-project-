package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.dto.request.CreateVoucherReq;
import com.hoabanrestaurant.backend.dto.request.UpdateVoucherReq;
import com.hoabanrestaurant.backend.dto.response.VoucherBriefDto;
import com.hoabanrestaurant.backend.entity.Voucher;
import com.hoabanrestaurant.backend.enums.ErrorCode;
import com.hoabanrestaurant.backend.enums.VoucherStatus;
import com.hoabanrestaurant.backend.enums.VoucherType;
import com.hoabanrestaurant.backend.exception.BusinessException;
import com.hoabanrestaurant.backend.repository.VoucherRepository;
import com.hoabanrestaurant.backend.service.VoucherService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {
    private final VoucherRepository repo;

    @Override
    @Transactional
    public VoucherBriefDto create(CreateVoucherReq r) {
        repo.findByCodeIgnoreCase(r.code()).ifPresent(v -> {
            throw new BusinessException(ErrorCode.VOUCHER_CODE_EXISTS, "Mã voucher đã tồn tại");
        });
        if (r.type() == VoucherType.PERCENT && r.maxDiscount() == null) {
            // tuỳ rule: có thể cho null → không giới hạn
        }
        Voucher v = Voucher.builder()
                .id(UUID.randomUUID())
                .code(r.code().trim())
                .name(r.name())
                .type(r.type())
                .value(r.value())
                .minOrder(r.minOrder())
                .maxDiscount(r.maxDiscount())
                .pointCost(Objects.requireNonNullElse(r.pointCost(), 0L))
                .status(VoucherStatus.ACTIVE)
                .startAt(r.startAt())
                .endAt(r.endAt())
                .build();
        v = repo.save(v);
        return toBrief(v);
    }

    @Override
    @Transactional
    public VoucherBriefDto update(UUID id, UpdateVoucherReq r) {
        Voucher v = repo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOUCHER_NOT_FOUND, "Voucher không tồn tại"));
        if (r.name() != null) v.setName(r.name());
        if (r.type() != null) v.setType(r.type());
        if (r.value() != null) v.setValue(r.value());
        if (r.minOrder() != null) v.setMinOrder(r.minOrder());
        if (r.maxDiscount() != null) v.setMaxDiscount(r.maxDiscount());
        if (r.pointCost() != null) v.setPointCost(r.pointCost());
        if (r.startAt() != null) v.setStartAt(r.startAt());
        if (r.endAt() != null) v.setEndAt(r.endAt());
        if (r.status() != null) v.setStatus(r.status());
        return toBrief(repo.save(v));
    }

    @Override
    public VoucherBriefDto get(UUID id) {
        return repo.findById(id).map(this::toBrief)
                .orElseThrow(() -> new BusinessException(ErrorCode.VOUCHER_NOT_FOUND, "Voucher không tồn tại"));
    }

    @Override
    public Page<VoucherBriefDto> list(Pageable pageable) {
        return repo.findAll(pageable).map(this::toBrief);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }

    private VoucherBriefDto toBrief(Voucher v) {
        return new VoucherBriefDto(
                v.getId(), v.getCode(), v.getName(), v.getType(),
                v.getValue(), v.getPointCost(), v.getMinOrder(), v.getMaxDiscount(),
                v.getStatus(), v.getStartAt(), v.getEndAt()
        );
    }
}

