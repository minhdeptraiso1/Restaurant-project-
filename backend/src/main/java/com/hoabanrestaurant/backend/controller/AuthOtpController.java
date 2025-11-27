package com.hoabanrestaurant.backend.controller;

import com.hoabanrestaurant.backend.exception.ApiResponse;
import com.hoabanrestaurant.backend.service.OtpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthOtpController {

    private final OtpService otpService;

    public record RequestOtpReq(@Email @NotBlank String email) {
    }

    public record VerifyOtpReq(@Email @NotBlank String email,
                               @NotBlank String code, @NotBlank @Size(min = 6) String newPassword) {
    }

    @PostMapping("/password/request-otp")
    public ApiResponse<String> requestOtp(@Valid @RequestBody RequestOtpReq req) {
        otpService.requestResetPassword(req.email());
        return ApiResponse.ok("Đã gửi OTP qua email của bạn");
    }

    @PostMapping("/password/verify-otp")
    public ApiResponse<String> verifyOtp(@Valid @RequestBody VerifyOtpReq req) {
        otpService.verifyResetAndChangePassword(req.email(), req.code(), req.newPassword());
        return ApiResponse.ok("Đổi mật khẩu thành công");
    }
}

