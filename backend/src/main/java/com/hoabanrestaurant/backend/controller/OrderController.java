package com.hoabanrestaurant.backend.controller;

import com.hoabanrestaurant.backend.dto.request.AddOrderItemReq;
import com.hoabanrestaurant.backend.dto.request.ApplyVoucherReq;
import com.hoabanrestaurant.backend.dto.request.CreateOrderReq;
import com.hoabanrestaurant.backend.dto.request.OpenByQrReq;
import com.hoabanrestaurant.backend.dto.request.PayOrderReq;
import com.hoabanrestaurant.backend.dto.request.UpdateOrderStatusReq;
import com.hoabanrestaurant.backend.dto.response.OrderDto;
import com.hoabanrestaurant.backend.exception.ApiResponse;
import com.hoabanrestaurant.backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Staff mở order tại bàn
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @PostMapping("/open")
    public ApiResponse<OrderDto> openByStaff(@Valid @RequestBody CreateOrderReq req) {
        return ApiResponse.ok(orderService.create(null, req));
    }

    // Mở order dine-in bằng QR
    @PostMapping("/open-by-qr")
    public ApiResponse<OrderDto> openByQr(
            @AuthenticationPrincipal Jwt jwtOrNull,
            @Valid @RequestBody OpenByQrReq req) {

        UUID uid = jwtOrNull != null ?
                UUID.fromString(jwtOrNull.getClaimAsString("uid")) : null;

        return ApiResponse.ok(orderService.openByQr(uid, req.qrCode(), req.note()));
    }

    // Thêm món vào order (dine-in)
    @PostMapping("/{orderId}/items")
    public ApiResponse<OrderDto> addItem(
            @PathVariable UUID orderId,
            @Valid @RequestBody AddOrderItemReq req) {
        return ApiResponse.ok(orderService.addItem(orderId, req));
    }

    // Áp voucher chung
    @PostMapping("/{orderId}/apply-voucher")
    public ApiResponse<OrderDto> applyVoucher(
            @PathVariable UUID orderId,
            @Valid @RequestBody ApplyVoucherReq req) {
        return ApiResponse.ok(orderService.applyVoucher(orderId, req));
    }

    // Áp voucher thuộc user
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/{orderId}/apply-user-voucher/{userVoucherId}")
    public ApiResponse<OrderDto> applyUserVoucher(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID orderId,
            @PathVariable UUID userVoucherId) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("uid"));
        return ApiResponse.ok(orderService.applyUserVoucher(orderId, userId, userVoucherId));
    }

    // Thanh toán order
    @PostMapping("/{orderId}/pay")
    public ApiResponse<OrderDto> pay(
            @PathVariable UUID orderId,
            @Valid @RequestBody PayOrderReq req) {
        return ApiResponse.ok(orderService.pay(orderId, req));
    }

    // Xem đơn hàng của user
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my-orders")
    public ApiResponse<List<OrderDto>> myOrders(@AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getClaimAsString("uid"));
        return ApiResponse.ok(orderService.getUserOrders(uid));
    }

    // Xem tất cả orders (staff/admin)
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @GetMapping
    public ApiResponse<List<OrderDto>> list() {
        return ApiResponse.ok(orderService.getAllOrders());
    }

    // Xem chi tiết
    @GetMapping("/{orderId}")
    public ApiResponse<OrderDto> getOrder(@PathVariable UUID orderId) {
        return ApiResponse.ok(orderService.getOrderById(orderId));
    }

    // Update status
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','CUSTOMER')")
    public ApiResponse<OrderDto> updateStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusReq req) {

        return ApiResponse.ok(orderService.updateStatus(id, req, jwt));
    }

    // Cleanup đơn rỗng
    @DeleteMapping("/cleanup-empty")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> cleanEmpty() {
        int deleted = orderService.cleanEmptyOrders();
        return ApiResponse.ok("Đã xóa " + deleted + " đơn hàng trống");
    }

    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @GetMapping("/stats/by-status-today")
    public ApiResponse<?> statsByStatusToday() {
        return ApiResponse.ok(orderService.getOrderStatsToday());
    }


}

