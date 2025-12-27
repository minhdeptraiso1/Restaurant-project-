package com.hoabanrestaurant.backend.service;


import com.hoabanrestaurant.backend.dto.request.AddOrderItemReq;
import com.hoabanrestaurant.backend.dto.request.ApplyVoucherReq;
import com.hoabanrestaurant.backend.dto.request.CreateOrderReq;
import com.hoabanrestaurant.backend.dto.request.PayOrderReq;
import com.hoabanrestaurant.backend.dto.request.UpdateCartItemReq;
import com.hoabanrestaurant.backend.dto.request.UpdateOrderStatusReq;
import com.hoabanrestaurant.backend.dto.response.OrderDto;
import com.hoabanrestaurant.backend.entity.Order;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public interface OrderService {
    // đã có:
    OrderDto create(UUID userIdOrNull, CreateOrderReq req);

    OrderDto addItem(UUID orderId, AddOrderItemReq req);

    OrderDto applyVoucher(UUID orderId, ApplyVoucherReq req);

    OrderDto pay(UUID orderId, PayOrderReq req);

    void confirmPaid(Order order);


    // THÊM MỚI cho Cart (Customer)
    OrderDto getOrCreateCart(UUID userId);

    OrderDto addItemToCart(UUID userId, AddOrderItemReq req);

    OrderDto updateCartItem(UUID userId, UUID itemId, UpdateCartItemReq req);

    OrderDto removeCartItem(UUID userId, UUID itemId);

    OrderDto applyUserVoucher(UUID orderId, UUID userId, UUID userVoucherId);

    List<OrderDto> getUserOrders(UUID userId);

    List<OrderDto> getAllOrders();

    OrderDto getOrderById(UUID orderId);

    OrderDto updateStatus(UUID orderId, UpdateOrderStatusReq req, Jwt jwt);


    int cleanEmptyOrders();

    OrderDto openByQr(UUID userIdOrNull, String qrCode, String note);

    Map<String, Long> getOrderStatsToday();

    Map<String, BigDecimal> getRevenueLast7Days();

}