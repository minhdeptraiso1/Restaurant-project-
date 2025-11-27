package com.hoabanrestaurant.backend.controller;

import com.hoabanrestaurant.backend.dto.request.AddOrderItemReq;
import com.hoabanrestaurant.backend.dto.request.UpdateCartItemReq;
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

import java.util.UUID;

// CartController.java
@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final OrderService orderService;

    // Lấy hoặc tạo giỏ hàng
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping
    public ApiResponse<OrderDto> getCart(@AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getClaimAsString("uid"));
        return ApiResponse.ok(orderService.getOrCreateCart(uid));
    }

    // Thêm item vào cart
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/items")
    public ApiResponse<OrderDto> addItem(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddOrderItemReq req) {

        UUID uid = UUID.fromString(jwt.getClaimAsString("uid"));
        return ApiResponse.ok(orderService.addItemToCart(uid, req));
    }

    // Cập nhật số lượng item
    @PreAuthorize("hasRole('CUSTOMER')")
    @PatchMapping("/items/{itemId}")
    public ApiResponse<OrderDto> updateItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID itemId,
            @RequestBody @Valid UpdateCartItemReq req) {

        UUID uid = UUID.fromString(jwt.getClaimAsString("uid"));
        return ApiResponse.ok(orderService.updateCartItem(uid, itemId, req));
    }

    // Xóa item
    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("/items/{itemId}")
    public ApiResponse<OrderDto> removeItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID itemId) {

        UUID uid = UUID.fromString(jwt.getClaimAsString("uid"));
        return ApiResponse.ok(orderService.removeCartItem(uid, itemId));
    }

}

