package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.config.VnPayConfig;
import com.hoabanrestaurant.backend.dto.request.VNPayReq;
import com.hoabanrestaurant.backend.entity.Order;
import com.hoabanrestaurant.backend.entity.Payment;
import com.hoabanrestaurant.backend.enums.ErrorCode;
import com.hoabanrestaurant.backend.enums.OrderStatus;
import com.hoabanrestaurant.backend.enums.PaymentMethod;
import com.hoabanrestaurant.backend.enums.PaymentStatus;
import com.hoabanrestaurant.backend.exception.BusinessException;
import com.hoabanrestaurant.backend.repository.OrderRepository;
import com.hoabanrestaurant.backend.repository.PaymentRepository;
import com.hoabanrestaurant.backend.service.PaymentService;
import com.hoabanrestaurant.backend.util.VnPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VnPayServiceImpl implements PaymentService {

    private final VnPayConfig props;
    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepo;
    private final OrderServiceImpl orderService;

    // =====================================
    // 1) Tạo URL thanh toán VNPAY
    // =====================================
    @Override
    public Map<String, String> createPayment(HttpServletRequest req, VNPayReq dto) {
        Order order = orderRepo.findById(dto.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Order không tồn tại"));

        long amount = order.getTotal()
                .multiply(new BigDecimal(100))
                .longValue();

        String bankCode = dto.bankCode();
        String locale = dto.language() == null ? "vn" : dto.language();

        String vnp_TxnRef = order.getId().toString();
        String vnp_IpAddr = VnPayUtil.getIpAddress(req);

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", props.getVersion());
        params.put("vnp_Command", props.getCommand());
        params.put("vnp_TmnCode", props.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", vnp_TxnRef);
        params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", locale);
        params.put("vnp_ReturnUrl", props.getReturnUrl());
        params.put("vnp_IpAddr", vnp_IpAddr);

        if (bankCode != null && !bankCode.isEmpty()) {
            params.put("vnp_BankCode", bankCode);
        }

        // Ngày giờ
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        params.put("vnp_CreateDate", formatter.format(cld.getTime()));
        cld.add(Calendar.MINUTE, 15);
        params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        // Build hash + URL
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append("=")
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append("=")
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                query.append("&");
                hashData.append("&");
            }
        }

        String secureHash = VnPayUtil.hmacSHA512(props.getSecretKey(), hashData.substring(0, hashData.length() - 1));
        query.append("vnp_SecureHash=").append(secureHash);

        String paymentUrl = props.getVnpUrl() + "?" + query;

        Map<String, String> result = new HashMap<>();
        result.put("code", "00");
        result.put("url", paymentUrl);
        result.put("message", "success");

        return result;
    }

    // ==================================================
    // 2) Kiểm tra checksum
    // ==================================================
    public boolean validateReturn(Map<String, String> fields, String vnp_SecureHash) {
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder sb = new StringBuilder();

        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext(); ) {
            String name = itr.next();
            sb.append(name).append("=")
                    .append(URLEncoder.encode(fields.get(name), StandardCharsets.US_ASCII));
            if (itr.hasNext()) sb.append("&");
        }

        String signValue = VnPayUtil.hmacSHA512(props.getSecretKey(), sb.toString());
        return signValue.equals(vnp_SecureHash);
    }

    // ==================================================
    // 3) Xử lý returnUrl → Thanh toán thành công
    // ==================================================
    @Override
    public Map<String, Object> processReturnUrl(HttpServletRequest request) {

        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String name = params.nextElement();
            fields.put(name, request.getParameter(name));
        }

        String responseCode = request.getParameter("vnp_ResponseCode");
        String secureHash = request.getParameter("vnp_SecureHash");
        String orderIdStr = request.getParameter("vnp_TxnRef");

        boolean verified = validateReturn(fields, secureHash);

        Map<String, Object> result = new HashMap<>();
        result.put("verified", verified);
        result.put("orderId", orderIdStr);
        result.put("responseCode", responseCode);

        // ❌ THẤT BẠI
        if (!verified || !"00".equals(responseCode)) {
            result.put("success", false);
            result.put("message", "Thanh toán thất bại hoặc chữ ký không hợp lệ");
            return result;
        }

        // ======================================
        // 🔥 TỪ ĐÂY TRỞ XUỐNG LÀ THÀNH CÔNG
        // ======================================
        UUID orderId = UUID.fromString(orderIdStr);

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Order không tồn tại"));

        if (order.getStatus() == OrderStatus.PAID) {
            result.put("success", true);
            result.put("message", "Đơn đã thanh toán trước đó");
            return result;
        }

        // Tạo payment record
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .order(order)
                .method(PaymentMethod.VNPAY)
                .amount(order.getTotal())
                .status(PaymentStatus.SUCCEEDED)
                .paidAt(Instant.now())
                .build();

        paymentRepo.save(payment);

        // Xác nhận đơn đã thanh toán (giống CASH)
        orderService.confirmPaid(order);

        result.put("success", true);
        result.put("message", "Thanh toán thành công");
        return result;
    }
}
