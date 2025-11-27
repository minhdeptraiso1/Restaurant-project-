package com.hoabanrestaurant.backend.dto.request;

import com.hoabanrestaurant.backend.enums.TableStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTableReq(
        @NotNull UUID areaId,
        @NotBlank String code,
        @Min(1) int seats,
        TableStatus status
) {
}
