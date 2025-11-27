package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.config.VNPAYConfig;
import com.hoabanrestaurant.backend.dto.response.PaymentDTO;
import com.hoabanrestaurant.backend.entity.Order;
import com.hoabanrestaurant.backend.entity.Payment;
import com.hoabanrestaurant.backend.enums.OrderStatus;
import com.hoabanrestaurant.backend.enums.PaymentMethod;
import com.hoabanrestaurant.backend.enums.PaymentStatus;
import com.hoabanrestaurant.backend.repository.PaymentRepository;
import com.hoabanrestaurant.backend.service.OrderService;
import com.hoabanrestaurant.backend.service.PaymentService;
import com.hoabanrestaurant.backend.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final VNPAYConfig vnp;
    private final PaymentRepository paymentRepo;
    private final OrderService orderService;

    @Override
    public PaymentDTO.VNPayResponse createVnPayPayment(HttpServletRequest req, Order order) {

        // Lưu Payment pending
        Payment p = new Payment();
        p.setId(UUID.randomUUID());
        p.setOrder(order);
        p.setAmount(order.getTotal());
        p.setStatus(PaymentStatus.PENDING);
        p.setMethod(PaymentMethod.VNPAY);
        p.setTransactionId(newTxnRef());
        p.setCreatedAt(Instant.now());

        paymentRepo.save(p);

        // Build Params
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", vnp.getVersion());
        params.put("vnp_Command", vnp.getCommand());
        params.put("vnp_TmnCode", vnp.getTmnCode());
        params.put("vnp_Amount", VNPayUtil.toVnpAmount(order.getTotal()));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", p.getTransactionId());
        params.put("vnp_OrderInfo", "Payment order " + order.getId());
        params.put("vnp_OrderType", vnp.getOrderType());
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnp.getReturnUrl());
        params.put("vnp_IpAddr", VNPayUtil.getClientIp(req));
        params.put("vnp_CreateDate", VNPayUtil.nowString());
        params.put("vnp_ExpireDate", VNPayUtil.plusMinutesString(15));
        params.put("vnp_IpnUrl", vnp.getIpnUrl());

        // Sign
        String raw = VNPayUtil.buildQuery(params, true);
        String hash = VNPayUtil.hmacSHA512(vnp.getSecretKey().trim(), raw);
        String payUrl = vnp.getUrl() + "?" + raw + "&vnp_SecureHash=" + hash;

        return PaymentDTO.VNPayResponse.builder()
                .code("00")
                .message("OK")
                .paymentUrl(payUrl)
                .txnRef(p.getTransactionId())
                .build();
    }

    private String newTxnRef() {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(Instant.now())
                + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    // =========================
    //      RETURN URL
    // =========================
    @Override
    @Transactional
    public void handleVnPayReturn(HttpServletRequest request) {

        if (!VNPayUtil.verifyChecksum(request, vnp.getSecretKey().trim()))
            throw new IllegalArgumentException("INVALID CHECKSUM");

        String txnRef = request.getParameter("vnp_TxnRef");
        String rsp = request.getParameter("vnp_ResponseCode");
        String st = request.getParameter("vnp_TransactionStatus");

        Payment payment = paymentRepo.findByTransactionId(txnRef)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payment.setBankCode(request.getParameter("vnp_BankCode"));
        payment.setCardType(request.getParameter("vnp_CardType"));
        payment.setDescription("Return");

        if ("00".equals(rsp) && "00".equals(st)) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setPaidAt(Instant.now());

            Order order = payment.getOrder();
            if (order.getStatus() == OrderStatus.OPEN) {
                orderService.confirmPaid(order); // <<< GỌI HÀM CHUẨN
            }

        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        paymentRepo.save(payment);
    }

    @Override
    @Transactional
    public String handleVnPayIpn(HttpServletRequest request) {

        if (!VNPayUtil.verifyChecksum(request, vnp.getSecretKey().trim()))
            return "INVALID CHECKSUM";

        String txnRef = request.getParameter("vnp_TxnRef");
        String rsp = request.getParameter("vnp_ResponseCode");
        String st = request.getParameter("vnp_TransactionStatus");

        Optional<Payment> opt = paymentRepo.findByTransactionId(txnRef);
        if (opt.isEmpty()) return "ORDER NOT FOUND";

        Payment payment = opt.get();

        if ("00".equals(rsp) && "00".equals(st)) {

            if (payment.getStatus() != PaymentStatus.SUCCEEDED) {

                payment.setStatus(PaymentStatus.SUCCEEDED);
                payment.setPaidAt(Instant.now());

                Order order = payment.getOrder();
                if (order.getStatus() == OrderStatus.OPEN) {
                    orderService.confirmPaid(order);
                }

                paymentRepo.save(payment);
            }

            return "OK";

        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepo.save(payment);
            return "FAILED";
        }
    }
}
