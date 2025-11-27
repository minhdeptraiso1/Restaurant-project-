package com.hoabanrestaurant.backend.service;

import com.hoabanrestaurant.backend.dto.request.CreateTableReq;
import com.hoabanrestaurant.backend.dto.response.TableDto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TableService {
    TableDto create(CreateTableReq req);

    List<TableDto> listByArea(UUID areaId);

    List<TableDto> getAll();

    TableDto updateTable(UUID id, CreateTableReq req);

    void deleteTable(UUID id);

    List<Map<String, Object>> getTablesStatus();

    List<TableDto> getAvailableTables(Instant startTime, Instant endTime);


    List<TableDto> getAvailableTablesByArea(UUID areaId, Instant startTime, Instant endTime);
}
