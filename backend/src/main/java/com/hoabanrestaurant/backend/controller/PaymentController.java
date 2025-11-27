package com.hoabanrestaurant.backend.controller;

import com.hoabanrestaurant.backend.dto.response.PaymentDTO;
import com.hoabanrestaurant.backend.entity.Order;
import com.hoabanrestaurant.backend.exception.ApiResponse;
import com.hoabanrestaurant.backend.repository.OrderRepository;
import com.hoabanrestaurant.backend.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final OrderRepository orderRepo;

    // Tạo URL thanh toán VNPay
    @PostMapping("/vnpay/create/{orderId}")
    public ApiResponse<PaymentDTO.VNPayResponse> createVnpay(@PathVariable UUID orderId,
                                                             HttpServletRequest request) {
        Order order = orderRepo.findById(orderId).orElseThrow();
        return ApiResponse.ok(paymentService.createVnPayPayment(request, order));
    }

    // Return URL (user browser)
    @GetMapping("/vnpay/return")
    public ResponseEntity<String> vnpReturn(HttpServletRequest request) {
        paymentService.handleVnPayReturn(request);
        // Có thể redirect FE: return ResponseEntity.status(302).location(URI.create("http://fe/success")).build();
        return ResponseEntity.ok("Return received");
    }

    // IPN URL (VNPay server-to-server)
    @GetMapping("/vnpay/ipn")
    public ResponseEntity<String> vnpIpn(HttpServletRequest request) {
        String res = paymentService.handleVnPayIpn(request);
        return ResponseEntity.ok(res);
    }
}
