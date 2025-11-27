package com.hoabanrestaurant.backend.controller;


import com.hoabanrestaurant.backend.dto.request.CreateReviewReq;
import com.hoabanrestaurant.backend.dto.response.ReviewRes;
import com.hoabanrestaurant.backend.entity.Review;
import com.hoabanrestaurant.backend.exception.ApiResponse;
import com.hoabanrestaurant.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/reviews")
public class ReviewController {
    private final ReviewService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ApiResponse<Review> createReview(@Valid @RequestBody CreateReviewReq req, @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getClaim("uid"));
        return ApiResponse.ok(service.createReview(req, uid));
    }

    @GetMapping("/latest")
    public ApiResponse<List<ReviewRes>> getLatestReview() {
        return ApiResponse.ok(service.getTop12Reviews());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<List<ReviewRes>> getAllReviews() {
        return ApiResponse.ok(service.getAllReviews());
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<String> deleteAllReviews(UUID id) {
        service.deleteReviewById(id);
        return ApiResponse.ok("All reviews have been deleted successfully.");
    }
}
