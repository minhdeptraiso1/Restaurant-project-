package com.hoabanrestaurant.backend.exception;

import com.hoabanrestaurant.backend.enums.ErrorCode;

public class BusinessException extends RuntimeException {
    private final ErrorCode error;

    public BusinessException(ErrorCode error) {
        super(error.defaultMessage);
        this.error = error;
    }

    public BusinessException(ErrorCode error, String message) {
        super(message);
        this.error = error;
    }

    public ErrorCode getError() {
        return error;
    }
}
