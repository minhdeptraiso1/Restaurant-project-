package com.hoabanrestaurant.backend.dto.response;

import java.util.UUID;

public record UserDto(
        UUID id,
        String fullName,
        String email,
        String phone,
        String role,
        String address,
        String status,
        String avatarUrl
) {
}
