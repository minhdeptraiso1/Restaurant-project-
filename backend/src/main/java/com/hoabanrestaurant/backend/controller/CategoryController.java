// controller/CategoryController.java
package com.hoabanrestaurant.backend.controller;

import com.hoabanrestaurant.backend.dto.request.CreateCategoryReq;
import com.hoabanrestaurant.backend.dto.response.CategoryDto;
import com.hoabanrestaurant.backend.exception.ApiResponse;
import com.hoabanrestaurant.backend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PostMapping
    public ApiResponse<CategoryDto> create(@Valid @RequestBody CreateCategoryReq req) {
        return ApiResponse.ok(service.create(req));
    }

    @GetMapping
    public ApiResponse<List<CategoryDto>> list() {
        return ApiResponse.ok(service.listAll());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<CategoryDto> update(@PathVariable UUID id, @Valid @RequestBody CreateCategoryReq req) {
        return ApiResponse.ok(service.update(id, req));
    }
}
