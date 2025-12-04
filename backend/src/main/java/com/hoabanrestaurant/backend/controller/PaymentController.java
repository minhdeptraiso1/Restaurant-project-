package com.hoabanrestaurant.backend.controller;

import com.hoabanrestaurant.backend.dto.request.VNPayReq;
import com.hoabanrestaurant.backend.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService vnPayService;

    @PostMapping("/create_payment")
    public ResponseEntity<?> createPayment(HttpServletRequest req, @RequestBody VNPayReq dto) throws Exception {
        return ResponseEntity.ok(vnPayService.createPayment(req, dto));
    }

    @GetMapping("/vnpay_return")
    public ResponseEntity<?> returnUrl(HttpServletRequest request) {
        Map<String, Object> result = vnPayService.processReturnUrl(request);

        boolean success = (boolean) result.get("success");

        if (success) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

}