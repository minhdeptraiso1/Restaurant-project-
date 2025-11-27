package com.hoabanrestaurant.backend.service;

import com.hoabanrestaurant.backend.dto.request.CreateReservationReq;
import com.hoabanrestaurant.backend.dto.response.ReservationDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ReservationService {
    ReservationDto create(UUID userId, CreateReservationReq req);      // KH tạo đơn, chưa gán bàn

    ReservationDto assignTables(UUID reservationId, List<UUID> tableIds, String staffEmail); // NV gán bàn

    void cancelByUser(UUID reservationId, UUID userId, String reason);

    void cancelByStaff(UUID reservationId, String staffEmail, String reason);

    List<ReservationDto> myReservations(UUID userId);

    List<ReservationDto> allReservations();

    public List<Map<String, Object>> getAvailableSlots();

}
