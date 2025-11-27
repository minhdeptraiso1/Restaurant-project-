package com.hoabanrestaurant.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemReq(

        @NotNull(message = "Số lượng không được bỏ trống")
        @Min(value = 0, message = "Số lượng phải >= 0")
        Integer quantity

) {
}
