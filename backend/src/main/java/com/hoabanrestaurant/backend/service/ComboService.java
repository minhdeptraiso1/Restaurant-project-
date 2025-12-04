package com.hoabanrestaurant.backend.service;

import com.hoabanrestaurant.backend.dto.request.CreateComboReq;
import com.hoabanrestaurant.backend.dto.response.ComboDto;
import com.hoabanrestaurant.backend.dto.response.DishDto;
import com.hoabanrestaurant.backend.dto.response.SuggestedMenuDto;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ComboService {
    ComboDto create(CreateComboReq req);

    ComboDto get(UUID comboId);

    List<ComboDto> listAll();

    List<ComboDto> getAllbyAdmin();

    ComboDto update(UUID comboId, CreateComboReq req);

    void delete(UUID comboId);

    List<Map<String, Object>> getAll();

    List<DishDto> getSuggestedDishesForUser(UUID userId);

    List<ComboDto> getSuggestedCombos();

    SuggestedMenuDto getSuggestedMenu(Jwt jwt);

}
