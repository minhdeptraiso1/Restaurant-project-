package com.hoabanrestaurant.backend.service;

import com.hoabanrestaurant.backend.dto.request.CreateReviewReq;
import com.hoabanrestaurant.backend.dto.response.ReviewRes;
import com.hoabanrestaurant.backend.entity.Review;

import java.util.List;
import java.util.UUID;

public interface ReviewService {

    Review createReview(CreateReviewReq req, UUID userId);

    List<ReviewRes> getTop12Reviews();

    List<ReviewRes> getAllReviews();

    void deleteReviewById(UUID id);
}
