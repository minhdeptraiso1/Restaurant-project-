package com.hoabanrestaurant.backend.mapper;

import com.hoabanrestaurant.backend.dto.response.CategoryDto;
import com.hoabanrestaurant.backend.entity.Category;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryDto toDto(Category c);

    List<CategoryDto> toDtoList(List<Category> c);
}
