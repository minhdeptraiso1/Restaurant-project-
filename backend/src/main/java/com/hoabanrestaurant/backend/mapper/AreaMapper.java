package com.hoabanrestaurant.backend.mapper;

import com.hoabanrestaurant.backend.dto.response.AreaDto;
import com.hoabanrestaurant.backend.entity.Area;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AreaMapper {
    AreaDto toDto(Area a);

    List<AreaDto> toDtoList(List<Area> a);
}
