// exception/GlobalExceptionHandler.java
package com.hoabanrestaurant.backend.exception;

import com.hoabanrestaurant.backend.enums.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusiness(BusinessException ex) {
        ErrorCode ec = ex.getError();
        return ResponseEntity.status(ec.status).body(ApiResponse.fail(ec, ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<?>> badCred(BadCredentialsException ex) {
        return ResponseEntity.status(ErrorCode.BAD_CREDENTIALS.status)
                .body(ApiResponse.fail(ErrorCode.BAD_CREDENTIALS, null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> denied(AccessDeniedException ex) {
        return ResponseEntity.status(ErrorCode.FORBIDDEN.status)
                .body(ApiResponse.fail(ErrorCode.FORBIDDEN, null));
    }

    @ExceptionHandler({JwtException.class, OAuth2AuthenticationException.class})
    public ResponseEntity<ApiResponse<?>> jwt(Exception ex) {
        return ResponseEntity.status(ErrorCode.TOKEN_INVALID.status)
                .body(ApiResponse.fail(ErrorCode.TOKEN_INVALID, null));
    }

    // @Valid trên @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> beanValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst().map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse(ErrorCode.BAD_REQUEST.defaultMessage);
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.status)
                .body(ApiResponse.fail(ErrorCode.BAD_REQUEST, msg));
    }

    // @Validated trên @RequestParam/@PathVariable
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> constraint(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .findFirst().map(v -> v.getMessage())
                .orElse(ErrorCode.BAD_REQUEST.defaultMessage);
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.status)
                .body(ApiResponse.fail(ErrorCode.BAD_REQUEST, msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> unhandled(Exception ex) {
        log.error("Unhandled error", ex);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.status)
                .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR, null));
    }
}
