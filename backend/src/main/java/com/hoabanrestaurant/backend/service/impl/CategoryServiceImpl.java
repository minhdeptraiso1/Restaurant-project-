// service/impl/CategoryServiceImpl.java
package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.dto.request.CreateCategoryReq;
import com.hoabanrestaurant.backend.dto.response.CategoryDto;
import com.hoabanrestaurant.backend.entity.Category;
import com.hoabanrestaurant.backend.enums.ErrorCode;
import com.hoabanrestaurant.backend.enums.MenuStatus;
import com.hoabanrestaurant.backend.exception.BusinessException;
import com.hoabanrestaurant.backend.mapper.CategoryMapper;
import com.hoabanrestaurant.backend.repository.CategoryRepository;
import com.hoabanrestaurant.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repo;
    private final CategoryMapper mapper;

    @Override
    public CategoryDto create(CreateCategoryReq req) {
        if (repo.existsByNameIgnoreCase(req.name()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Tên danh mục đã tồn tại");
        Category c = Category.builder()
                .id(UUID.randomUUID())
                .name(req.name().trim())
                .description(req.description())
                .status(req.status() != null ? req.status() : MenuStatus.ACTIVE)
                .build();
        return mapper.toDto(repo.save(c));
    }

    @Override
    public List<CategoryDto> listAll() {
        return mapper.toDtoList(repo.findAll());
    }

    @Override
    public CategoryDto update(UUID categoryId, CreateCategoryReq req) {
        Category c = repo.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Danh mục không tồn tại"));
        c.setName(req.name().trim());
        c.setDescription(req.description());
        if (req.status() != null) {
            c.setStatus(req.status());
        }
        return mapper.toDto(repo.save(c));
    }
}
