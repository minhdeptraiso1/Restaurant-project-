package com.hoabanrestaurant.backend.exception;


import com.hoabanrestaurant.backend.enums.ErrorCode;
import lombok.Builder;

@Builder
public record ApiResponse<T>(
        boolean success,
        String message,
        String code,
        T data) {
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("OK")
                .code(null)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> fail(ErrorCode ec, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .code(ec.code)
                .data(null)
                .build();
    }
}
