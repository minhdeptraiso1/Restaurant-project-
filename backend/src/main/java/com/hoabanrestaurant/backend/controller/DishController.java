// controller/DishController.java
package com.hoabanrestaurant.backend.controller;

import com.hoabanrestaurant.backend.dto.request.CreateDishReq;
import com.hoabanrestaurant.backend.dto.request.UpdateDishReq;
import com.hoabanrestaurant.backend.dto.response.DishDto;
import com.hoabanrestaurant.backend.exception.ApiResponse;
import com.hoabanrestaurant.backend.service.DishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/dishes")
@RequiredArgsConstructor
public class DishController {
    private final DishService service;

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PostMapping
    public ApiResponse<DishDto> create(@Valid @RequestBody CreateDishReq req) {
        return ApiResponse.ok(service.create(req));
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PutMapping("/{id}")
    public ApiResponse<DishDto> update(@PathVariable UUID id, @Valid @RequestBody UpdateDishReq req) {
        return ApiResponse.ok(service.update(id, req));
    }

    @GetMapping
    public ApiResponse<Page<DishDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ApiResponse.ok(service.listAll(page, size, sortBy, direction));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<Page<DishDto>> listAll(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(defaultValue = "name") String sortBy,
                                              @RequestParam(defaultValue = "asc") String direction
    ) {
        return ApiResponse.ok(service.getAll(page, size, sortBy, direction));
    }


    @GetMapping("/by-category/{categoryId}")
    public ApiResponse<List<DishDto>> byCategory(@PathVariable UUID categoryId) {
        return ApiResponse.ok(service.listByCategory(categoryId));
    }

    @GetMapping("/{id}")
    public ApiResponse<DishDto> getById(@PathVariable UUID id) {
        return ApiResponse.ok(service.getById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<String> delete(@PathVariable UUID id) {
        service.delete(id);
        return ApiResponse.ok("Xóa món thành công");
    }
}
