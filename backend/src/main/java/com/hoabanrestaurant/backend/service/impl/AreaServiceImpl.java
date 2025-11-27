package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.dto.request.CreateAreaReq;
import com.hoabanrestaurant.backend.dto.response.AreaDto;
import com.hoabanrestaurant.backend.entity.Area;
import com.hoabanrestaurant.backend.enums.AreaStatus;
import com.hoabanrestaurant.backend.enums.ErrorCode;
import com.hoabanrestaurant.backend.exception.BusinessException;
import com.hoabanrestaurant.backend.mapper.AreaMapper;
import com.hoabanrestaurant.backend.repository.AreaRepository;
import com.hoabanrestaurant.backend.service.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AreaServiceImpl implements AreaService {
    private final AreaRepository repo;
    private final AreaMapper mapper;

    @Override
    public AreaDto create(CreateAreaReq req) {
        if (repo.existsByNameIgnoreCase(req.name()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Tên khu vực đã tồn tại");
        Area a = Area.builder()
                .id(UUID.randomUUID())
                .name(req.name().trim())
                .description(req.description())
                .status(req.status() != null ? req.status() : AreaStatus.ACTIVE)
                .build();
        return mapper.toDto(repo.save(a));
    }

    @Override
    public List<AreaDto> getAll() {
        return mapper.toDtoList(repo.findAll());
    }

    @Override
    public AreaDto update(UUID id, CreateAreaReq req) {
        Area a = repo.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Khu vực không tồn tại"));
        if (repo.existsByNameIgnoreCaseAndIdNot(req.name(), id))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Tên khu vực đã tồn tại");
        a.setName(req.name().trim());
        a.setDescription(req.description());
        a.setStatus(req.status() != null ? req.status() : AreaStatus.ACTIVE);
        return mapper.toDto(repo.save(a));
    }

    @Override
    public void delete(UUID id) {
        if (!repo.existsById(id))
            throw new BusinessException(ErrorCode.NOT_FOUND, "Khu vực không tồn tại");
        repo.deleteById(id);
    }
}
