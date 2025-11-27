package com.hoabanrestaurant.backend.controller;

import com.hoabanrestaurant.backend.dto.request.CreateTableReq;
import com.hoabanrestaurant.backend.dto.response.TableDto;
import com.hoabanrestaurant.backend.exception.ApiResponse;
import com.hoabanrestaurant.backend.service.TableService;
import com.hoabanrestaurant.backend.util.QrSigner;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/tables")
@RequiredArgsConstructor
public class TableController {
    private final TableService service;
    private final QrSigner qrSigner;
    

    @PostMapping("/issue")
    public Map<String, Object> issue(@RequestParam UUID tableId,
                                     @RequestParam(defaultValue = "1800") long ttlSeconds) {
        String code = qrSigner.issue(tableId, java.time.Duration.ofSeconds(ttlSeconds));
        return Map.of(
                "tableId", tableId,
                "ttlSeconds", ttlSeconds,
                "qrCode", code  // <-- Chuỗi này chính là giá trị bạn gửi vào open-by-qr
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PostMapping
    public ApiResponse<TableDto> create(@Valid @RequestBody CreateTableReq req) {
        return ApiResponse.ok(service.create(req));
    }

    @GetMapping
    public ApiResponse<List<TableDto>> listAll() {
        return ApiResponse.ok(service.getAll());
    }

    @GetMapping("/by-area/{areaId}")
    public ApiResponse<List<TableDto>> listByArea(@PathVariable UUID areaId) {
        return ApiResponse.ok(service.listByArea(areaId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PutMapping("/{id}")
    public ApiResponse<TableDto> updateTable(@PathVariable UUID id, @Valid @RequestBody CreateTableReq req) {
        return ApiResponse.ok(service.updateTable(id, req));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteTable(@PathVariable UUID id) {
        service.deleteTable(id);
        return ApiResponse.ok("");
    }

    /**
     * Lấy danh sách bàn trống trong khoảng thời gian
     * GET /v1/tables/available?startTime=2024-12-25T18:00:00Z&endTime=2024-12-25T20:00:00Z
     */
    @GetMapping("/available")
    public ApiResponse<List<TableDto>> getAvailableTables(
            @RequestParam Instant startTime,
            @RequestParam Instant endTime) {
        return ApiResponse.ok(service.getAvailableTables(startTime, endTime));
    }

    /**
     * Lấy danh sách bàn trống trong khu vực cụ thể
     * GET /v1/tables/available/by-area/{areaId}?startTime=...&endTime=...
     */
    @GetMapping("/available/by-area/{areaId}")
    public ApiResponse<List<TableDto>> getAvailableTablesByArea(
            @PathVariable UUID areaId,
            @RequestParam Instant startTime,
            @RequestParam Instant endTime) {
        return ApiResponse.ok(service.getAvailableTablesByArea(areaId, startTime, endTime));
    }
}
