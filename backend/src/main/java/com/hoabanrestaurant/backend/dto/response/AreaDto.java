package com.hoabanrestaurant.backend.dto.response;

import com.hoabanrestaurant.backend.enums.AreaStatus;

import java.util.UUID;

public record AreaDto(UUID id, String name, String description, AreaStatus status) {
}
