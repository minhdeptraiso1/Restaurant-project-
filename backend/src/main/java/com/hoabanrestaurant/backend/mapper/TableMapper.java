package com.hoabanrestaurant.backend.mapper;

import com.hoabanrestaurant.backend.dto.response.TableDto;
import com.hoabanrestaurant.backend.entity.RestaurantTable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TableMapper {
    @Mapping(target = "areaId", source = "area.id")
    @Mapping(target = "areaName", source = "area.name")
    TableDto toDto(RestaurantTable t);

    List<TableDto> toDtoList(List<RestaurantTable> t);
}
