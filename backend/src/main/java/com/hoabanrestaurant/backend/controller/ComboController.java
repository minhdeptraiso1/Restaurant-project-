// controller/ComboController.java
package com.hoabanrestaurant.backend.controller;

import com.hoabanrestaurant.backend.dto.request.CreateComboReq;
import com.hoabanrestaurant.backend.dto.response.ComboDto;
import com.hoabanrestaurant.backend.dto.response.SuggestedMenuDto;
import com.hoabanrestaurant.backend.exception.ApiResponse;
import com.hoabanrestaurant.backend.service.ComboService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/v1/combos")
@RequiredArgsConstructor
public class ComboController {
    private final ComboService service;

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PostMapping
    public ApiResponse<ComboDto> create(@Valid @RequestBody CreateComboReq req) {
        return ApiResponse.ok(service.create(req));
    }

    @GetMapping
    public ApiResponse<List<ComboDto>> list() {
        return ApiResponse.ok(service.listAll());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<List<ComboDto>> listAllForAdmin() {
        return ApiResponse.ok(service.getAllbyAdmin());
    }

    @GetMapping("/{id}")
    public ApiResponse<ComboDto> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<ComboDto> update(@PathVariable UUID id, @Valid @RequestBody CreateComboReq req) {
        return ApiResponse.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<String> delete(@PathVariable UUID id) {
        service.delete(id);
        return ApiResponse.ok("Xóa combo thành công");
    }

    //recoment system goi y mon theo lich su an uong
    @GetMapping("/suggested")
    public ApiResponse<SuggestedMenuDto> getSuggestedMenu(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(service.getSuggestedMenu(jwt));
    }

}
