package com.hoabanrestaurant.backend.dto.response;

import com.hoabanrestaurant.backend.enums.MenuStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ComboDto(
        UUID id, String name, String description,
        BigDecimal price, String imageUrl, MenuStatus status,
        List<Item> items, BigDecimal suggestedSum  // tổng giá gợi ý = sum(dish.price * qty)
) {
    public record Item(UUID dishId, String dishName, int quantity) {
    }
}
