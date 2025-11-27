package com.hoabanrestaurant.backend.service;

import com.hoabanrestaurant.backend.dto.request.CreateCategoryReq;
import com.hoabanrestaurant.backend.dto.response.CategoryDto;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryDto create(CreateCategoryReq req);

    List<CategoryDto> listAll();

    CategoryDto update(UUID id, CreateCategoryReq req);

}
