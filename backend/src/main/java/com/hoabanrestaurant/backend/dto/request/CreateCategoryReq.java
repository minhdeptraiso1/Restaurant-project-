package com.hoabanrestaurant.backend.dto.request;

import com.hoabanrestaurant.backend.enums.MenuStatus;
import jakarta.validation.constraints.NotBlank;

public record CreateCategoryReq(
        @NotBlank String name,
        String description,
        MenuStatus status
) {
}
