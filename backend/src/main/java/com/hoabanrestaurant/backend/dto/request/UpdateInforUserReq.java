package com.hoabanrestaurant.backend.dto.request;

public record UpdateInforUserReq(
        String avatarUrl,
        String fullName,
        String phone,
        String address) {
}
