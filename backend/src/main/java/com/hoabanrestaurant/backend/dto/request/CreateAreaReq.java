package com.hoabanrestaurant.backend.dto.request;

import com.hoabanrestaurant.backend.enums.AreaStatus;
import jakarta.validation.constraints.NotBlank;

public record CreateAreaReq(
        @NotBlank String name,
        String description,
        AreaStatus status
) {
}
