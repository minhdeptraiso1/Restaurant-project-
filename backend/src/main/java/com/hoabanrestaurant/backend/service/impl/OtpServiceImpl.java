package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.entity.OtpToken;
import com.hoabanrestaurant.backend.enums.ErrorCode;
import com.hoabanrestaurant.backend.enums.OtpPurpose;
import com.hoabanrestaurant.backend.exception.BusinessException;
import com.hoabanrestaurant.backend.repository.OtpTokenRepository;
import com.hoabanrestaurant.backend.repository.UserRepository;
import com.hoabanrestaurant.backend.service.EmailService;
import com.hoabanrestaurant.backend.service.OtpService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpTokenRepository otpRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    @Value("${app.otp.ttl-minutes:5}")
    private long ttlMinutes;

    @Override
    @Transactional
    public void requestResetPassword(String email) {
        var user = userRepo.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Email không tồn tại"));

        var actives = otpRepo.findActive(user.getEmail(), OtpPurpose.RESET_PASSWORD, Instant.now());
        OtpToken token;
        if (!actives.isEmpty()) {
            token = actives.get(0);
        } else {
            String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1000000));
            token = OtpToken.builder()
                    .id(UUID.randomUUID())
                    .email(user.getEmail())
                    .purpose(OtpPurpose.RESET_PASSWORD)
                    .codeHash(encoder.encode(code))
                    .expiresAt(Instant.now().plus(Duration.ofMinutes(ttlMinutes)))
                    .build();
            otpRepo.save(token);

            // gửi email
            Map<String, Object> model = Map.of("code", code, "ttlMinutes", ttlMinutes);
            emailService.sendTemplate(user.getEmail(), "Mã OTP đổi mật khẩu", "email/otp.html", model);
        }
    }

    @Override
    @Transactional
    public void verifyResetAndChangePassword(String email, String code, String newPassword) {
        var user = userRepo.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Email không tồn tại"));

        var list = otpRepo.findActive(user.getEmail(), OtpPurpose.RESET_PASSWORD, Instant.now());
        if (list.isEmpty()) throw new BusinessException(ErrorCode.BAD_REQUEST, "OTP không hợp lệ hoặc đã hết hạn");

        OtpToken tok = list.get(0);
        if (!encoder.matches(code, tok.getCodeHash())) {
            tok.setAttempts(tok.getAttempts() + 1);
            otpRepo.save(tok);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Mã OTP không đúng");
        }

        // ok
        tok.setConsumedAt(Instant.now());
        otpRepo.save(tok);

        user.setPasswordHash(encoder.encode(newPassword));
        userRepo.save(user);
    }
}

