package com.hoabanrestaurant.backend.dto.request;

import com.hoabanrestaurant.backend.enums.MenuStatus;
import com.hoabanrestaurant.backend.enums.Unit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateDishReq(
        @NotNull UUID categoryId,
        @NotBlank String name,
        String description,
        @NotNull Unit unit,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
        String imageUrl,
        MenuStatus status
) {
}
