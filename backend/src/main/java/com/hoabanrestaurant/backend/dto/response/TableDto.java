package com.hoabanrestaurant.backend.dto.response;

import com.hoabanrestaurant.backend.enums.TableStatus;

import java.util.UUID;

public record TableDto(UUID id, UUID areaId, String areaName, String code, int seats, TableStatus status) {
}
