package com.hoabanrestaurant.backend.service;

import com.hoabanrestaurant.backend.dto.request.VNPayReq;
import com.hoabanrestaurant.backend.dto.response.VNPayResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    VNPayResponse createPayment(HttpServletRequest request, VNPayReq reqBody);

    String handleIPN(HttpServletRequest request);

    void handleReturn(HttpServletRequest request);
}

