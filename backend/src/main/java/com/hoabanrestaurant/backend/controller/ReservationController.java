package com.hoabanrestaurant.backend.controller;

import com.hoabanrestaurant.backend.dto.request.AssignTablesReq;
import com.hoabanrestaurant.backend.dto.request.CancelReservationReq;
import com.hoabanrestaurant.backend.dto.request.CreateReservationReq;
import com.hoabanrestaurant.backend.dto.response.ReservationDto;
import com.hoabanrestaurant.backend.exception.ApiResponse;
import com.hoabanrestaurant.backend.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService service;

    // Khách tạo đơn (chưa gán bàn)
    @PreAuthorize("hasAnyRole('CUSTOMER','STAFF','ADMIN')")
    @PostMapping
    public ApiResponse<ReservationDto> create(@AuthenticationPrincipal Jwt jwt,
                                              @Valid @RequestBody CreateReservationReq req) {
        UUID userId = UUID.fromString((String) jwt.getClaim("uid"));
        return ApiResponse.ok(service.create(userId, req));
    }

    // Nhân viên/Admin gán bàn (1..n) cho đơn
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @PostMapping("/{id}/assign-tables")
    public ApiResponse<ReservationDto> assign(@PathVariable UUID id,
                                              @Valid @RequestBody AssignTablesReq req,
                                              @AuthenticationPrincipal Jwt jwt) {
        String staffEmail = jwt.getSubject();
        return ApiResponse.ok(service.assignTables(id, req.tableIds(), staffEmail));
    }

    // Khách huỷ đơn của chính mình
    @PreAuthorize("hasAnyRole('CUSTOMER','STAFF','ADMIN')")
    @PostMapping("/{id}/cancel")
    public ApiResponse<?> cancelByUser(@PathVariable UUID id,
                                       @AuthenticationPrincipal Jwt jwt,
                                       @Valid @RequestBody CancelReservationReq req) {
        UUID userId = UUID.fromString((String) jwt.getClaim("uid"));
        service.cancelByUser(id, userId, req.reason());
        return ApiResponse.ok("Đã huỷ đơn");
    }

    // Nhân viên/Admin huỷ đơn
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @PostMapping("/{id}/cancel-by-staff")
    public ApiResponse<?> cancelByStaff(@PathVariable UUID id,
                                        @AuthenticationPrincipal Jwt jwt,
                                        @Valid @RequestBody CancelReservationReq req) {
        service.cancelByStaff(id, jwt.getSubject(), req.reason());
        return ApiResponse.ok("Đã huỷ đơn (nhân viên/admin)");
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','STAFF','ADMIN')")
    @GetMapping("/me")
    public ApiResponse<List<ReservationDto>> my(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString((String) jwt.getClaim("uid"));
        return ApiResponse.ok(service.myReservations(userId));
    }

    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @GetMapping
    public ApiResponse<List<ReservationDto>> all() {
        return ApiResponse.ok(service.allReservations());
    }

    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @GetMapping("/stats/by-status-today")
    public ApiResponse<?> statsByStatusToday() {
        return ApiResponse.ok(service.getReservationStatsToday());
    }
}