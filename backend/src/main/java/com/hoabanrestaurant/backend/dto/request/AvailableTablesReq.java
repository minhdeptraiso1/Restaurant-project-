package com.hoabanrestaurant.backend.dto.request;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * Request để tìm bàn trống trong khoảng thời gian
 * Optional: có thể filter theo areaId
 */
public record AvailableTablesReq(
        @NotNull(message = "Thời gian bắt đầu không được để trống")
        Instant startTime,

        @NotNull(message = "Thời gian kết thúc không được để trống")
        Instant endTime,

        UUID areaId,  // Optional: nếu muốn filter theo khu vực

        Integer minSeats  // Optional: số ghế tối thiểu cần
) {
}
