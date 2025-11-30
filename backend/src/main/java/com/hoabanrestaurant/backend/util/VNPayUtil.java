package com.hoabanrestaurant.backend.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.DatatypeConverter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class VNPayUtil {

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printHexBinary(bytes).toLowerCase();
        } catch (Exception e) {
            throw new RuntimeException("Cannot generate HMAC SHA512", e);
        }
    }

    public static String buildQuery(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        StringBuilder sb = new StringBuilder();

        for (String key : keys) {
            String value = params.get(key);
            if (value == null || value.isEmpty()) continue;

            sb.append(key).append("=")
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20"))
                    .append("&");
        }

        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static boolean verifyChecksum(HttpServletRequest request, String secretKey) {
        Map<String, String> fields = new TreeMap<>();
        request.getParameterMap().forEach((key, value) -> {
            if (!key.equals("vnp_SecureHash") && !key.equals("vnp_SecureHashType")) {
                fields.put(key, value[0]);
            }
        });

        String generated = hmacSHA512(secretKey, buildQuery(fields));
        String received = request.getParameter("vnp_SecureHash");

        return generated.equals(received);
    }

    public static String now() {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(Instant.now());
    }

    public static String expire(int minutes) {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(Instant.now().plusSeconds(minutes * 60));
    }

    /**
     * Lấy IP client từ request, xử lý X-Forwarded-For cho proxy/ngrok
     */
    public static String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            ip = ip.split(",")[0].trim();
        } else {
            ip = req.getRemoteAddr();
        }
        // Xử lý IPv6 loopback → IPv4
        if (ip == null || ip.isBlank() || ip.contains(":")) {
            ip = "127.0.0.1";
        }
        return ip;
    }
}
