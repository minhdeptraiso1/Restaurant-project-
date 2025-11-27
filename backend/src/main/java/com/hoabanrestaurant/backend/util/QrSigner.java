package com.hoabanrestaurant.backend.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class QrSigner {
    @Value("${qr.secret}")
    private String secret;

    public String issue(UUID tableId, Duration ttl) {
        Instant iat = Instant.now(), exp = iat.plus(ttl);
        String payload = tableId + "|" + iat.toEpochMilli() + "|" + exp.toEpochMilli();
        String sig = hmac(payload, secret);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((payload + "|" + sig).getBytes(StandardCharsets.UTF_8));
    }

    public Decoded verify(String code) {
        String raw = new String(Base64.getUrlDecoder().decode(code), StandardCharsets.UTF_8);
        String[] parts = raw.split("\\|");
        if (parts.length != 4) throw new IllegalArgumentException("QR không hợp lệ");
        var tableId = UUID.fromString(parts[0]);
        var exp = Instant.ofEpochMilli(Long.parseLong(parts[2]));
        var sig = parts[3];
        String payload = parts[0] + "|" + parts[1] + "|" + parts[2];
        if (!hmac(payload, secret).equals(sig)) throw new IllegalArgumentException("QR sai chữ ký");
        return new Decoded(tableId, exp);
    }

    private String hmac(String data, String key) {
        try {
            var mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record Decoded(UUID tableId, Instant exp) {
    }
}
