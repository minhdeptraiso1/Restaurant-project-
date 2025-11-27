package com.hoabanrestaurant.backend.dto.response;

import com.hoabanrestaurant.backend.enums.MenuStatus;

import java.util.UUID;

public record CategoryDto(UUID id, String name, String description, MenuStatus status) {
}