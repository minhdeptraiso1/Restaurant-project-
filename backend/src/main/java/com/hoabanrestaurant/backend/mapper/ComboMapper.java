package com.hoabanrestaurant.backend.mapper;

import com.hoabanrestaurant.backend.dto.response.ComboDto;
import com.hoabanrestaurant.backend.entity.Combo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ComboMapper {
    // items & suggestedSum sẽ lắp từ service
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "suggestedSum", ignore = true)
    ComboDto toDto(Combo c);
}
