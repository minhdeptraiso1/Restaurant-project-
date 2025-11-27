package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.dto.request.CreateTableReq;
import com.hoabanrestaurant.backend.dto.response.TableDto;
import com.hoabanrestaurant.backend.entity.Area;
import com.hoabanrestaurant.backend.entity.RestaurantTable;
import com.hoabanrestaurant.backend.enums.ErrorCode;
import com.hoabanrestaurant.backend.exception.BusinessException;
import com.hoabanrestaurant.backend.mapper.TableMapper;
import com.hoabanrestaurant.backend.repository.AreaRepository;
import com.hoabanrestaurant.backend.repository.RestaurantTableRepository;
import com.hoabanrestaurant.backend.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TableServiceImpl implements TableService {
    private final AreaRepository areaRepo;
    private final RestaurantTableRepository tableRepo;
    private final TableMapper mapper;

    @Override
    public TableDto create(CreateTableReq req) {
        Area area = areaRepo.findById(req.areaId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Khu vực không tồn tại"));
        if (tableRepo.existsByArea_IdAndCodeIgnoreCase(area.getId(), req.code()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Mã bàn đã tồn tại trong khu vực");

        RestaurantTable t = RestaurantTable.builder()
                .id(UUID.randomUUID())
                .area(area)
                .code(req.code().trim())
                .seats(req.seats())
                .status(req.status() != null ? req.status() : area.getStatus().name().equals("ACTIVE")
                        ? com.hoabanrestaurant.backend.enums.TableStatus.AVAILABLE
                        : com.hoabanrestaurant.backend.enums.TableStatus.UNAVAILABLE)
                .build();

        return mapper.toDto(tableRepo.save(t));
    }

    @Override
    public List<TableDto> listByArea(UUID areaId) {
        return mapper.toDtoList(tableRepo.findByArea_Id(areaId));
    }

    @Override
    public List<TableDto> getAll() {
        return mapper.toDtoList(tableRepo.findAll());
    }

    @Override
    public List<Map<String, Object>> getTablesStatus() {
        return tableRepo.findAll().stream()
                .map(t -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", t.getId() != null ? t.getId().toString() : "");
                    m.put("name", t.getCode() != null ? t.getCode() : "");
                    m.put("status", t.getStatus() != null ? t.getStatus().name() : "UNKNOWN");
                    return m;
                })
                .toList();
    }


    @Override
    public TableDto updateTable(UUID id, CreateTableReq req) {
        Area area = areaRepo.findById(req.areaId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Khu vực không tồn tại"));
        if (tableRepo.existsByArea_IdAndCodeIgnoreCase(area.getId(), req.code()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Mã bàn đã tồn tại trong khu vực");
        RestaurantTable table = tableRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Bàn không tồn tại"));
        table = RestaurantTable.builder()
                .id(UUID.randomUUID())
                .area(area)
                .code(req.code().trim())
                .seats(req.seats())
                .status(req.status() != null ? req.status() : area.getStatus().name().equals("ACTIVE")
                        ? com.hoabanrestaurant.backend.enums.TableStatus.AVAILABLE
                        : com.hoabanrestaurant.backend.enums.TableStatus.UNAVAILABLE)
                .build();
        return mapper.toDto(tableRepo.save(table));
    }

    @Override
    public void deleteTable(UUID id) {
        if (!tableRepo.existsById(id)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Bàn không tồn tại");
        }
        tableRepo.deleteById(id);
    }

    @Override
    public List<TableDto> getAvailableTables(Instant startTime, Instant endTime) {
        if (startTime.isAfter(endTime)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Thời gian bắt đầu phải trước thời gian kết thúc");
        }
        return mapper.toDtoList(tableRepo.findAvailableTables(startTime, endTime));
    }

    @Override
    public List<TableDto> getAvailableTablesByArea(UUID areaId, Instant startTime, Instant endTime) {
        if (startTime.isAfter(endTime)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Thời gian bắt đầu phải trước thời gian kết thúc");
        }
        if (!areaRepo.existsById(areaId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Khu vực không tồn tại");
        }
        return mapper.toDtoList(tableRepo.findAvailableTablesByArea(areaId, startTime, endTime));
    }
}
