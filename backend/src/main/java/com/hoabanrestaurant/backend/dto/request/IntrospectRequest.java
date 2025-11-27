package com.hoabanrestaurant.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record IntrospectRequest(@NotBlank String token) {
}