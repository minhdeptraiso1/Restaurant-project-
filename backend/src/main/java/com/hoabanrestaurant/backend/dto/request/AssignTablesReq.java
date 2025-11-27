package com.hoabanrestaurant.backend.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record AssignTablesReq(
        @NotNull List<UUID> tableIds
) {
}
