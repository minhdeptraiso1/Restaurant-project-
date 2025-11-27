package com.hoabanrestaurant.backend.mapper;

import com.hoabanrestaurant.backend.dto.response.ReservationDto;
import com.hoabanrestaurant.backend.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    ReservationDto toDto(Reservation r);
}
