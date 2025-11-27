package com.hoabanrestaurant.backend.dto.request;

import com.hoabanrestaurant.backend.enums.MenuStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateComboReq(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        String imageUrl,
        MenuStatus status,
        @NotNull @Size(min = 1, message = "Combo cần ít nhất 1 món")
        List<ItemReq> items
) {
    public record ItemReq(@NotNull UUID dishId, @Min(1) int quantity) {
    }
}
