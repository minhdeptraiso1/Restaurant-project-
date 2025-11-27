package com.hoabanrestaurant.backend.util;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class VNPayUtil {

    public static String urlEncode(String s) {
        // VNPay yêu cầu UTF-8 và space là %20 (không phải +)
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * Build query để KÝ: sort A→Z, encode key & value, bỏ null/rỗng và KHÔNG gồm vnp_SecureHash, vnp_SecureHashType
     */
    public static String buildQuery(Map<String, String> params, boolean encodeKey) {
        return params.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .filter(e -> !e.getKey().equals("vnp_SecureHash") && !e.getKey().equals("vnp_SecureHashType"))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> (encodeKey ? urlEncode(e.getKey()) : e.getKey()) + "=" + urlEncode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    public static String hmacSHA512(String secret, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            hmac.init(new SecretKeySpec(secret.trim().getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] raw = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String toVnpAmount(BigDecimal vnd) {
        return vnd.multiply(BigDecimal.valueOf(100)).setScale(0).toPlainString(); // ×100, không dấu
    }

    public static String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            ip = ip.split(",")[0].trim();
        } else {
            ip = req.getRemoteAddr();
        }
        // ép IPv6 loopback về 127.0.0.1
        if (ip == null || ip.isBlank() || ip.contains(":")) {
            ip = "127.0.0.1";
        }
        return ip;
    }


    private static final ZoneId HCM = ZoneId.of("Asia/Ho_Chi_Minh");

    public static String nowString() {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(HCM).format(Instant.now());
    }

    public static String plusMinutesString(int minutes) {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(HCM).format(Instant.now().plusSeconds(minutes * 60L));
    }

    /**
     * Verify checksum cho return/ipn: DỰNG raw giống hệt lúc ký và so sánh không phân biệt hoa/thường
     */
    public static boolean verifyChecksum(HttpServletRequest req, String secret) {
        Map<String, String> params = new HashMap<>();
        req.getParameterMap().forEach((k, v) -> params.put(k, v != null && v.length > 0 ? v[0] : null));
        String secureHash = params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        String raw = buildQuery(params, true);
        String calc = hmacSHA512(secret, raw);
        return secureHash != null && secureHash.equalsIgnoreCase(calc);
    }
}
