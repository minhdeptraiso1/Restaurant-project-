package com.hoabanrestaurant.backend.mapper;

import com.hoabanrestaurant.backend.dto.request.CreateReviewReq;
import com.hoabanrestaurant.backend.entity.Review;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    Review toDto(CreateReviewReq req);
}
