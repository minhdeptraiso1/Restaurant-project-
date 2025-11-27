package com.hoabanrestaurant.backend.dto.response;

import com.hoabanrestaurant.backend.enums.ReservationStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationDto(
        UUID id, UUID userId, String userEmail,
        List<TableBrief> tables,           // <-- danh sách bàn
        Instant startTime, Instant endTime,
        int partySize, ReservationStatus status, String note,
        String cancelReason, String canceledBy
) {
    public record TableBrief(UUID id, String code) {
    }
}
