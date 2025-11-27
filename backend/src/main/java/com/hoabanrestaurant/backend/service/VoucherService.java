package com.hoabanrestaurant.backend.service;

import com.hoabanrestaurant.backend.dto.request.CreateVoucherReq;
import com.hoabanrestaurant.backend.dto.request.UpdateVoucherReq;
import com.hoabanrestaurant.backend.dto.response.VoucherBriefDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VoucherService {
    VoucherBriefDto create(CreateVoucherReq req);

    VoucherBriefDto update(UUID id, UpdateVoucherReq req);

    VoucherBriefDto get(UUID id);

    Page<VoucherBriefDto> list(Pageable pageable);

    void delete(UUID id); // optional
}
