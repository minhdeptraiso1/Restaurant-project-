package com.hoabanrestaurant.backend.mapper;

import com.hoabanrestaurant.backend.dto.response.DishDto;
import com.hoabanrestaurant.backend.entity.Dish;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DishMapper {
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "signature", source = "signature")
    DishDto toDto(Dish d);

    List<DishDto> toDtoList(List<Dish> d);
}
