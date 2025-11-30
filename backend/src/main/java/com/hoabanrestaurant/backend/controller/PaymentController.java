package com.hoabanrestaurant.backend.controller;

import com.hoabanrestaurant.backend.dto.request.VNPayReq;
import com.hoabanrestaurant.backend.dto.response.VNPayResponse;
import com.hoabanrestaurant.backend.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public VNPayResponse create(HttpServletRequest request, @RequestBody VNPayReq order) {
        return paymentService.createPayment(request, order);
    }

    @GetMapping("/return")
    public ResponseEntity<String> vnpReturn(HttpServletRequest req) {
        paymentService.handleReturn(req);
        return ResponseEntity.ok("Payment success!");
    }

    @GetMapping("/ipn")
    public ResponseEntity<String> ipn(HttpServletRequest req) {
        return ResponseEntity.ok(paymentService.handleIPN(req));
    }
}
