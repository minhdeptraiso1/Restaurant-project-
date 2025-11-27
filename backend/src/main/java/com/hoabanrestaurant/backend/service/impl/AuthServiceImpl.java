package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.dto.request.ChangePasswordRequest;
import com.hoabanrestaurant.backend.dto.request.LoginRequest;
import com.hoabanrestaurant.backend.dto.request.RegisterRequest;
import com.hoabanrestaurant.backend.dto.response.AuthResponse;
import com.hoabanrestaurant.backend.dto.response.UserDto;
import com.hoabanrestaurant.backend.entity.User;
import com.hoabanrestaurant.backend.enums.ErrorCode;
import com.hoabanrestaurant.backend.enums.UserRole;
import com.hoabanrestaurant.backend.enums.UserStatus;
import com.hoabanrestaurant.backend.exception.BusinessException;
import com.hoabanrestaurant.backend.mapper.UserMapper;
import com.hoabanrestaurant.backend.repository.UserRepository;
import com.hoabanrestaurant.backend.security.ClientInfoResolver;
import com.hoabanrestaurant.backend.security.LoginRateLimiter;
import com.hoabanrestaurant.backend.security.TokenBlacklistService;
import com.hoabanrestaurant.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepo;
    private final UserMapper mapper;
    private final TokenBlacklistService blacklistService;
    private final PasswordEncoder passwordEncoder;
    private final LoginRateLimiter loginLimiter;
    private final ClientInfoResolver clientInfoResolver;

    @Value("${app.security.jwt.issuer}")
    private String issuer;
    @Value("${app.security.jwt.access-token-minutes}")
    private long accessMinutes;
    @Value("${app.security.jwt.refresh-token-days}")
    private long refreshDays;

    @Override
    public AuthResponse login(LoginRequest req, HttpServletRequest http) {
        // 1) chuẩn hoá input + tạo rate key
        String email = req.email().trim().toLowerCase();
        String clientIp = clientInfoResolver.resolveClientIp(http);
        String rateKey = email + "|" + clientIp;

        // 2) rate limit
        loginLimiter.ensureAllowed(rateKey);

        try {
            // 3) xác thực
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, req.password())
            );

            // 4) phát token nếu ok
            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            String access = generateAccessToken(user);
            String refresh = generateRefreshToken(email);

            loginLimiter.onSuccess(rateKey);
            return new AuthResponse(access, refresh, "Bearer");
        } catch (BadCredentialsException e) {
            loginLimiter.onFailure(rateKey);
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS); // để GlobalExceptionHandler map ra 401
        } catch (Exception e) {
            // các lỗi khác trong quá trình authenticate
            loginLimiter.onFailure(rateKey);
            throw e;
        }
    }

    @Override
    public void logout(String jti, Instant exp) {
        blacklistService.revoke(jti, exp);
    }

    @Override
    @Transactional
    public UserDto register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepo.findByEmail(email).isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }

        User u = new User();
        u.setId(UUID.randomUUID());
        u.setFullName(req.fullName().trim());
        u.setEmail(email);
        u.setPhone(req.phone());
        u.setRole(UserRole.CUSTOMER);
        u.setStatus(UserStatus.ACTIVE);
        u.setPasswordHash(passwordEncoder.encode(req.password()));

        return mapper.toDto(userRepo.save(u));
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest req) {
        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(req.oldPassword(), u.getPasswordHash())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Mật khẩu cũ không đúng");
        }
        u.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepo.save(u);
    }

    @Override
    public AuthResponse refresh(String oldRefreshToken) {
        // 1) decode & validate refresh (nếu revoked/hết hạn sẽ ném exception trong decoder/validator)
        Jwt jwt = jwtDecoder.decode(oldRefreshToken);
        String email = jwt.getSubject();
        String oldRefreshJti = jwt.getId();

        // 2) lấy user
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 3) phát token mới (rotation)
        String newAccess = generateAccessToken(user);
        String newRefresh = generateRefreshToken(email);

        // 4) revoke refresh cũ đến hết hạn
        blacklistService.revoke(oldRefreshJti, jwt.getExpiresAt());

        return new AuthResponse(newAccess, newRefresh, "Bearer");
    }

    // ===== Helpers =====

    private String generateAccessToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())                 // jti
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(accessMinutes)))
                .subject(user.getEmail())
                .claim("uid", user.getId().toString())
                .claim("roles", List.of(user.getRole().name()))   // khớp JwtAuthenticationConverter
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS512).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private String generateRefreshToken(String email) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())                 // jti refresh
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofDays(refreshDays)))
                .subject(email)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS512).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Override
    public UserDto me(String email) {
        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return mapper.toDto(u);
    }
}
