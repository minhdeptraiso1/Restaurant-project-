package com.hoabanrestaurant.backend.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {
    public static AuthResponse of(String at, String rt) {
        return new AuthResponse(at, rt, "Bearer");
    }
}