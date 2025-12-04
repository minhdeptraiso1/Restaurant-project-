package com.hoabanrestaurant.backend.dto.response;

import com.hoabanrestaurant.backend.enums.MenuStatus;
import com.hoabanrestaurant.backend.enums.Unit;

import java.math.BigDecimal;
import java.util.UUID;

public record DishDto(
        UUID id, UUID categoryId, String categoryName,
        String name, String description, Unit unit,
        BigDecimal price, String imageUrl, Boolean signature, MenuStatus status
) {
}
