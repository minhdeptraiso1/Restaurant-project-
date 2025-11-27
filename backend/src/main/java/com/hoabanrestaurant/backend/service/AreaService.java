package com.hoabanrestaurant.backend.service;

import com.hoabanrestaurant.backend.dto.request.CreateAreaReq;
import com.hoabanrestaurant.backend.dto.response.AreaDto;

import java.util.List;
import java.util.UUID;

public interface AreaService {
    AreaDto create(CreateAreaReq req);

    List<AreaDto> getAll();

    AreaDto update(UUID id, CreateAreaReq req);

    void delete(UUID id);

}