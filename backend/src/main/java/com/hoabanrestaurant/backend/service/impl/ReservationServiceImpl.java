package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.dto.request.CreateReservationReq;
import com.hoabanrestaurant.backend.dto.response.ReservationDto;
import com.hoabanrestaurant.backend.entity.Reservation;
import com.hoabanrestaurant.backend.entity.ReservationTableLink;
import com.hoabanrestaurant.backend.entity.RestaurantTable;
import com.hoabanrestaurant.backend.entity.User;
import com.hoabanrestaurant.backend.enums.ErrorCode;
import com.hoabanrestaurant.backend.enums.ReservationStatus;
import com.hoabanrestaurant.backend.exception.BusinessException;
import com.hoabanrestaurant.backend.mapper.ReservationMapper;
import com.hoabanrestaurant.backend.repository.ReservationRepository;
import com.hoabanrestaurant.backend.repository.ReservationTableLinkRepository;
import com.hoabanrestaurant.backend.repository.RestaurantTableRepository;
import com.hoabanrestaurant.backend.repository.UserRepository;
import com.hoabanrestaurant.backend.service.EmailService;
import com.hoabanrestaurant.backend.service.ReservationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final UserRepository userRepo;
    private final RestaurantTableRepository tableRepo;
    private final ReservationRepository resRepo;
    private final ReservationTableLinkRepository linkRepo;
    private final ReservationMapper mapper;
    private final EmailService emailService;

    @Override
    public ReservationDto create(UUID userId, CreateReservationReq req) {
        if (!req.startTime().isBefore(req.endTime()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Thời gian đặt bàn không hợp lệ");

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Reservation r = Reservation.builder()
                .id(UUID.randomUUID())
                .user(user)
                .startTime(req.startTime())
                .endTime(req.endTime())
                .partySize(req.partySize())
                .note(req.note())
                .status(ReservationStatus.PENDING)
                .build();

        r = resRepo.save(r);
        return toDtoWithTables(r, List.of());
    }

    @Override
    @Transactional
    public ReservationDto assignTables(UUID reservationId, List<UUID> tableIds, String staffEmail) {
        Reservation r = resRepo.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Đơn đặt không tồn tại"));

        if (r.getStatus() == ReservationStatus.CANCELLED || r.getStatus() == ReservationStatus.COMPLETED)
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Trạng thái đơn không cho phép gán bàn");

        // Kiểm tra trùng giờ trên các bàn được gán
        List<Reservation> overlaps = linkRepo.findOverlapsForTables(tableIds, r.getStartTime(), r.getEndTime());
        // loại chính nó (nếu có) trong danh sách overlaps
        boolean hasOtherOverlap = overlaps.stream().anyMatch(x -> !x.getId().equals(reservationId));
        if (hasOtherOverlap)
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Một hoặc nhiều bàn đã được đặt trong khung giờ này");

        // Load thực thể bàn + validate sức chứa
        List<RestaurantTable> tables = tableRepo.findAllById(tableIds);
        if (tables.size() != tableIds.size())
            throw new BusinessException(ErrorCode.NOT_FOUND, "Có bàn không tồn tại");

        int totalSeats = tables.stream().mapToInt(RestaurantTable::getSeats).sum();
        if (totalSeats < r.getPartySize())
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Tổng chỗ ngồi của bàn gán không đủ cho số khách");

        // Xoá liên kết cũ (nếu muốn cho phép re-assign) rồi gán mới
        linkRepo.deleteAllByReservation(reservationId);
        for (RestaurantTable t : tables) {
            ReservationTableLink link = ReservationTableLink.builder()
                    .id(new ReservationTableLink.Id(reservationId, t.getId()))
                    .reservation(r)
                    .table(t)
                    .build();
            linkRepo.save(link);
        }

        // Có thể set CONFIRMED ngay khi gán bàn
        r.setStatus(ReservationStatus.CONFIRMED);
        resRepo.save(r);
//        emailService.sendTemplate(
//                r.getUser().getEmail(),
//                "Xác nhận đặt bàn – Hoa Ban",
//                "email/reservation_confirm.html",
//                Map.of("fullName", r.getUser().getFullName(),
//                        "start", r.getStartTime(),
//                        "end", r.getEndTime(),
//                        "partySize", r.getPartySize(),
//                        "reservationId", r.getId().toString())
//        );

        return toDtoWithTables(r, tables);
    }

    @Override
    @Transactional
    public void cancelByUser(UUID reservationId, UUID userId, String reason) {
        Reservation r = resRepo.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Đơn đặt không tồn tại"));
        if (!r.getUser().getId().equals(userId))
            throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn chỉ có thể huỷ đơn của chính mình");

        if (r.getStatus() == ReservationStatus.CANCELLED)
            return;

        r.setStatus(ReservationStatus.CANCELLED);
        r.setCancelReason(reason);
        r.setCanceledBy("USER:" + r.getUser().getEmail());
        resRepo.save(r);
    }

    @Override
    @Transactional
    public void cancelByStaff(UUID reservationId, String staffEmail, String reason) {
        Reservation r = resRepo.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Đơn đặt không tồn tại"));
        if (r.getStatus() == ReservationStatus.CANCELLED)
            return;

        r.setStatus(ReservationStatus.CANCELLED);
        r.setCancelReason(reason);
        r.setCanceledBy((staffEmail != null ? (staffEmail.startsWith("ADMIN") ? "ADMIN:" : "STAFF:") : "STAFF:") + staffEmail);
        resRepo.save(r);
    }

    @Override
    public List<ReservationDto> myReservations(UUID userId) {
        return resRepo.findByUser_Id(userId).stream()
                .map(r -> toDtoWithTables(r, linkRepo.findTablesByReservation(r.getId())))
                .toList();
    }

    @Override
    public List<ReservationDto> allReservations() {
        return resRepo.findAll().stream()
                .map(r -> toDtoWithTables(r, linkRepo.findTablesByReservation(r.getId())))
                .toList();
    }

    private ReservationDto toDtoWithTables(Reservation r, List<RestaurantTable> tables) {
        List<ReservationDto.TableBrief> tb = tables.stream()
                .map(t -> new ReservationDto.TableBrief(t.getId(), t.getCode()))
                .toList();

        ReservationDto base = mapper.toDto(r);
        // Vì ReservationDto là record (immutable), trả bản mới với đủ fields:
        return new ReservationDto(
                base.id(), base.userId(), base.userEmail(),
                tb,
                base.startTime(), base.endTime(),
                base.partySize(), base.status(), base.note(),
                base.cancelReason(), base.canceledBy()
        );
    }


    @Override
    public List<Map<String, Object>> getAvailableSlots() {

        final LocalTime START_TIME = LocalTime.of(10, 0);
        final LocalTime END_TIME = LocalTime.of(22, 0);
        final int SLOT_DURATION_MINUTES = 60;
        final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

        List<Map<String, Object>> slots = new ArrayList<>();
        LocalTime currentSlotStart = START_TIME;
        long fakeId = 1;

        while (currentSlotStart.isBefore(END_TIME)) {
            LocalTime currentSlotEnd = currentSlotStart.plusMinutes(SLOT_DURATION_MINUTES);

            Map<String, Object> slotMap = new HashMap<>();
            slotMap.put("id", fakeId++);
            slotMap.put("start", currentSlotStart.format(FORMATTER));
            slotMap.put("end", currentSlotEnd.format(FORMATTER));
            slotMap.put("status", "AVAILABLE"); // optional but useful for AI

            slots.add(slotMap);

            currentSlotStart = currentSlotEnd;
        }

        return slots;
    }

    @Override
    public Map<String, Long> getReservationStatsToday() {

        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);

        Instant start = today.atStartOfDay(zone).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(zone).toInstant();

        List<Object[]> stats = resRepo.countByStatusBetween(start, end);

        Map<String, Long> result = new HashMap<>();

        for (Object[] row : stats) {
            ReservationStatus status = (ReservationStatus) row[0];
            Long count = (Long) row[1];
            result.put(status.name(), count);
        }

        return result;
    }

}