package com.hoabanrestaurant.backend.service;

import com.hoabanrestaurant.backend.dto.response.PaymentDTO;
import com.hoabanrestaurant.backend.entity.Order;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    PaymentDTO.VNPayResponse createVnPayPayment(HttpServletRequest req, Order order);

    void handleVnPayReturn(HttpServletRequest request);  // /v1/payments/vnpay-return

    String handleVnPayIpn(HttpServletRequest request);   // /v1/payments/vnpay-ipn


}
