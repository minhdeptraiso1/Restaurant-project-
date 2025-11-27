package com.hoabanrestaurant.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateReservationReq(
        @NotNull Instant startTime,
        @NotNull Instant endTime,
        @Min(1) int partySize,
        String note
) {
}
