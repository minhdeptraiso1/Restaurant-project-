package com.hoabanrestaurant.backend.mapper;


import com.hoabanrestaurant.backend.dto.response.UserDto;
import com.hoabanrestaurant.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role", expression = "java(user.getRole()!=null ? user.getRole().name() : null)")
    @Mapping(target = "status", expression = "java(user.getStatus()!=null ? user.getStatus().name() : null)")
    UserDto toDto(User user);
}
