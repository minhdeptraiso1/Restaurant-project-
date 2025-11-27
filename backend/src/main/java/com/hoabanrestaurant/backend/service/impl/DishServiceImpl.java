package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.dto.request.CreateDishReq;
import com.hoabanrestaurant.backend.dto.request.UpdateDishReq;
import com.hoabanrestaurant.backend.dto.response.DishDto;
import com.hoabanrestaurant.backend.entity.Category;
import com.hoabanrestaurant.backend.entity.Dish;
import com.hoabanrestaurant.backend.enums.ErrorCode;
import com.hoabanrestaurant.backend.enums.MenuStatus;
import com.hoabanrestaurant.backend.exception.BusinessException;
import com.hoabanrestaurant.backend.mapper.DishMapper;
import com.hoabanrestaurant.backend.repository.CategoryRepository;
import com.hoabanrestaurant.backend.repository.DishRepository;
import com.hoabanrestaurant.backend.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {
    private final CategoryRepository categoryRepo;
    private final DishRepository dishRepo;
    private final DishMapper mapper;

    @Override
    public DishDto create(CreateDishReq req) {
        Category cat = categoryRepo.findById(req.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Danh mục không tồn tại"));
        if (dishRepo.existsByCategory_IdAndNameIgnoreCase(cat.getId(), req.name()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Tên món đã tồn tại trong danh mục");

        Dish d = Dish.builder()
                .id(UUID.randomUUID())
                .category(cat)
                .name(req.name().trim())
                .description(req.description())
                .unit(req.unit())
                .price(req.price())
                .imageUrl(req.imageUrl())
                .status(req.status() != null ? req.status() : MenuStatus.ACTIVE)
                .build();

        return mapper.toDto(dishRepo.save(d));
    }

    @Override
    public DishDto update(UUID dishId, UpdateDishReq req) {
        Dish d = dishRepo.findById(dishId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Món không tồn tại"));
        d.setName(req.name().trim());
        d.setDescription(req.description());
        d.setUnit(req.unit());
        d.setPrice(req.price());
        d.setImageUrl(req.imageUrl());
        d.setStatus(req.status());
        return mapper.toDto(dishRepo.save(d));
    }

    @Override
    public Page<DishDto> listAll(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return dishRepo.findByStatus(MenuStatus.ACTIVE, pageable).map(mapper::toDto);
    }

    @Override
    public Page<DishDto> getAll(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return dishRepo.findAll(pageable).map(mapper::toDto);
    }


    @Override
    public List<DishDto> listByCategory(UUID categoryId) {
        return mapper.toDtoList(dishRepo.findByCategory_IdAndStatus(categoryId, MenuStatus.ACTIVE));
    }

    @Override
    public DishDto getById(UUID dishId) {
        Dish d = dishRepo.findById(dishId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Món không tồn tại"));
        return mapper.toDto(d);
    }

    @Override
    public void delete(UUID dishId) {
        Dish d = dishRepo.findById(dishId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Món không tồn tại"));
        dishRepo.delete(d);
    }

    @Override
    public List<Map<String, Object>> getAll() {
        return dishRepo.findByStatus(MenuStatus.ACTIVE).stream()
                .map(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getId() != null ? c.getId().toString() : "");
                    m.put("name", c.getName() != null ? c.getName() : "");
                    m.put("price", c.getPrice() != null ? c.getPrice() : "");
                    return m;
                })
                .toList();
    }

}
