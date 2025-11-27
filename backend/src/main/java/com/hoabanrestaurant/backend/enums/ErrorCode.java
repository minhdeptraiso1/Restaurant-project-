package com.hoabanrestaurant.backend.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    BAD_REQUEST("E400", "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("E401", "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("E403", "Không có quyền", HttpStatus.FORBIDDEN),
    NOT_FOUND("E404", "Không tìm thấy", HttpStatus.NOT_FOUND),

    USER_NOT_FOUND("U404", "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    EMAIL_EXISTS("U409", "Email đã tồn tại", HttpStatus.CONFLICT),
    BAD_CREDENTIALS("U401", "Email hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED),

    TOKEN_INVALID("T401", "Token không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED),
    RATE_LIMITED("S429", "Tạm bị khoá, thử lại sau", HttpStatus.TOO_MANY_REQUESTS),

    VOUCHER_EXPIRED("V400", "Voucher đã hết hạn", HttpStatus.BAD_REQUEST),
    VOUCHER_INACTIVE("V400", "Voucher không khả dụng", HttpStatus.BAD_REQUEST),
    VOUCHER_NOT_APPLICABLE("V400", "Voucher không áp dụng cho đơn hàng", HttpStatus.BAD_REQUEST),
    VOUCHER_NOT_FOUND("V404", "Không tìm thấy voucher", HttpStatus.NOT_FOUND),
    VOUCHER_CODE_EXISTS("V409", "Mã voucher đã tồn tại", HttpStatus.CONFLICT),

    INTERNAL_ERROR("E500", "Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR);

    public final String code;
    public final String defaultMessage;
    public final HttpStatus status;

    ErrorCode(String code, String msg, HttpStatus st) {
        this.code = code;
        this.defaultMessage = msg;
        this.status = st;
    }
}
