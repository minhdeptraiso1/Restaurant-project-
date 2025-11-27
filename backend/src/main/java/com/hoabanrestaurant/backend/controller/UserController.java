package com.hoabanrestaurant.backend.controller;

import com.hoabanrestaurant.backend.dto.request.CreateUserReq;
import com.hoabanrestaurant.backend.dto.request.UpdateUserRoleReq;
import com.hoabanrestaurant.backend.dto.response.UserDto;
import com.hoabanrestaurant.backend.exception.ApiResponse;
import com.hoabanrestaurant.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping
    public ApiResponse<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.ok(service.findAllUsers(pageable, search, role, status)
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserDto> create(@Valid @RequestBody CreateUserReq req) {
        return ApiResponse.ok(service.create(req));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUserById(@PathVariable UUID id) {
        return ApiResponse.ok(service.findById(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteUser(@PathVariable UUID id) {
        service.delete(id);
        return ApiResponse.ok("");
    }

    @PatchMapping("/{id}/role")
    public ApiResponse<UserDto> updateRole(
            @PathVariable UUID id,
            @RequestBody UpdateUserRoleReq req
    ) {
        return ApiResponse.ok(service.updateRole(id, req.role()));
    }
}
