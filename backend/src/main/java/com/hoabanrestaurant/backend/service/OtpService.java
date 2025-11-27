package com.hoabanrestaurant.backend.service;

public interface OtpService {
    void requestResetPassword(String email);                         // gửi OTP

    void verifyResetAndChangePassword(String email, String code, String newPassword);
}
