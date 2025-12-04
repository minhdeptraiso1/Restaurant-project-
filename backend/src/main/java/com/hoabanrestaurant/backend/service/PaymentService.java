package com.hoabanrestaurant.backend.service;

import com.hoabanrestaurant.backend.dto.request.VNPayReq;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface PaymentService {
    Map<String, String> createPayment(HttpServletRequest req, VNPayReq dto);

    Map<String, Object> processReturnUrl(HttpServletRequest request);


}

