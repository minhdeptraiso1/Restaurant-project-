package com.hoabanrestaurant.backend.dto.request;

import com.hoabanrestaurant.backend.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusReq(@NotNull OrderStatus status, String reason) {
}
