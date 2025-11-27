package com.hoabanrestaurant.backend.service;

import com.hoabanrestaurant.backend.dto.request.ChangePasswordRequest;
import com.hoabanrestaurant.backend.dto.request.LoginRequest;
import com.hoabanrestaurant.backend.dto.request.RegisterRequest;
import com.hoabanrestaurant.backend.dto.response.AuthResponse;
import com.hoabanrestaurant.backend.dto.response.UserDto;
import jakarta.servlet.http.HttpServletRequest;

import java.time.Instant;

public interface AuthService {
    AuthResponse login(LoginRequest req, HttpServletRequest request);

    UserDto me(String email);

    UserDto register(RegisterRequest req);

    void changePassword(String email, ChangePasswordRequest req);

    void logout(String jti, Instant exp);

    AuthResponse refresh(String refreshToken);
}
