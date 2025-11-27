package com.hoabanrestaurant.backend.dto.response;

public record IntrospectResponse(boolean active, String subject, String message) {
    public static IntrospectResponse ok(String sub) {
        return new IntrospectResponse(true, sub, "OK");
    }

    public static IntrospectResponse fail(String msg) {
        return new IntrospectResponse(false, null, msg);
    }
}