package com.hoabanrestaurant.backend.controller;

import com.hoabanrestaurant.backend.dto.request.CreateVoucherReq;
import com.hoabanrestaurant.backend.dto.request.UpdateVoucherReq;
import com.hoabanrestaurant.backend.dto.response.VoucherBriefDto;
import com.hoabanrestaurant.backend.exception.ApiResponse;
import com.hoabanrestaurant.backend.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/vouchers")
@RequiredArgsConstructor
public class VoucherAdminController {
    private final VoucherService service;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<VoucherBriefDto> create(@Valid @RequestBody CreateVoucherReq req) {
        return ApiResponse.ok(service.create(req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ApiResponse<VoucherBriefDto> update(@PathVariable UUID id,
                                               @Valid @RequestBody UpdateVoucherReq req) {
        return ApiResponse.ok(service.update(id, req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ApiResponse<VoucherBriefDto> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.get(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<Page<VoucherBriefDto>> list(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(service.list(PageRequest.of(page, size)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable UUID id) {
        service.delete(id);
        return ApiResponse.ok("Đã xoá");
    }
}

