package com.hoabanrestaurant.backend.service;

import com.hoabanrestaurant.backend.dto.request.CreateDishReq;
import com.hoabanrestaurant.backend.dto.request.UpdateDishReq;
import com.hoabanrestaurant.backend.dto.response.DishDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DishService {
    DishDto create(CreateDishReq req);

    DishDto update(UUID dishId, UpdateDishReq req);

    Page<DishDto> listAll(int page, int size, String sortBy, String direction);

    Page<DishDto> getAll(int page, int size, String sortBy, String direction);

    List<Map<String, Object>> getAll();

    DishDto getById(UUID dishId);

    List<DishDto> listByCategory(UUID categoryId);

    void delete(UUID dishId);
}
