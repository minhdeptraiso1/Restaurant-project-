package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.config.VNPAYConfig;
import com.hoabanrestaurant.backend.dto.request.VNPayReq;
import com.hoabanrestaurant.backend.dto.response.VNPayResponse;
import com.hoabanrestaurant.backend.entity.Order;
import com.hoabanrestaurant.backend.repository.OrderRepository;
import com.hoabanrestaurant.backend.repository.PaymentRepository;
import com.hoabanrestaurant.backend.service.OrderService;
import com.hoabanrestaurant.backend.service.PaymentService;
import com.hoabanrestaurant.backend.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements PaymentService {

    private final VNPAYConfig vnp;
    private final PaymentRepository paymentRepo;
    private final OrderService orderService;
    private final OrderRepository orderRepo;

    @Override
    public VNPayResponse createPayment(HttpServletRequest req, VNPayReq reqBody) {


        Order order = orderRepo.findById(reqBody.orderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String txnRef = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        long amount = order.getTotal()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", vnp.getVersion());
        params.put("vnp_Command", vnp.getCommand());
        params.put("vnp_TmnCode", vnp.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Order payment " + order.getId());
        params.put("vnp_OrderType", vnp.getOrderType());
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnp.getReturnUrl());
        // KHÔNG gửi vnp_IpnUrl trong request (phải cấu hình trên VNPay Portal)
        params.put("vnp_IpAddr", VNPayUtil.getClientIp(req));
        params.put("vnp_CreateDate", VNPayUtil.now());
        params.put("vnp_ExpireDate", VNPayUtil.expire(15));
        // KHÔNG thêm vnp_SecureHashType vào params để ký

        String raw = VNPayUtil.buildQuery(params);
        String hash = VNPayUtil.hmacSHA512(vnp.getSecretKey(), raw);

        String payUrl = vnp.getVnpUrl() + "?" + raw + "&vnp_SecureHash=" + hash;

        return new VNPayResponse(
                "00",
                "success",
                payUrl
        );
    }

    @Override
    @Transactional
    public void handleReturn(HttpServletRequest req) {
        if (!VNPayUtil.verifyChecksum(req, vnp.getSecretKey()))
            throw new RuntimeException("INVALID CHECKSUM");
    }

    @Override
    @Transactional
    public String handleIPN(HttpServletRequest req) {
        if (!VNPayUtil.verifyChecksum(req, vnp.getSecretKey()))
            return "INVALID";

        String rsp = req.getParameter("vnp_ResponseCode");
        String st = req.getParameter("vnp_TransactionStatus");

        if ("00".equals(rsp) && "00".equals(st)) {
            return "OK";
        }

        return "FAILED";
    }
}
