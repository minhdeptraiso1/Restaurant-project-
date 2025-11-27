package com.hoabanrestaurant.backend.controller;

import com.hoabanrestaurant.backend.service.ComboService;
import com.hoabanrestaurant.backend.service.DishService;
import com.hoabanrestaurant.backend.service.ReservationService;
import com.hoabanrestaurant.backend.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/ai")
@RequiredArgsConstructor
public class AIInternalController {

    private final DishService dishService;
    private final ComboService comboService;
    private final TableService tableService;
    private final ReservationService reservationService;

    // 1) Lấy danh sách món
    @GetMapping("/dishes")
    public List<Map<String, Object>> getDishes() {
        return dishService.getAll();
    }

    // 2) Lấy danh sách combo
    @GetMapping("/combos")
    public List<Map<String, Object>> getCombos() {
        return comboService.getAll();
    }

    // 3) Lấy danh sách bàn + trạng thái bàn
    @GetMapping("/tables/status")
    public List<Map<String, Object>> getTablesStatus() {
        return tableService.getTablesStatus();
    }

    // 4) Lấy danh sách khung giờ trống cho đặt bàn
    @GetMapping("/reservation/slots")
    public List<Map<String, Object>> getSlots() {
        // Controller chỉ việc gọi
        return reservationService.getAvailableSlots();
    }
}
