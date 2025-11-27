package com.hoabanrestaurant.backend.dto.request;

import com.hoabanrestaurant.backend.enums.MenuStatus;
import com.hoabanrestaurant.backend.enums.Unit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateDishReq(
        @NotBlank String name,
        String description,
        @NotNull Unit unit,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
        String imageUrl,
        @NotNull MenuStatus status
) {
}
