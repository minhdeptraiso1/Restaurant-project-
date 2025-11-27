package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.Reservation;
import com.hoabanrestaurant.backend.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    @Query("""
                SELECT r FROM Reservation r
                JOIN ReservationTableLink l ON l.reservation = r
                WHERE l.table.id = :tableId
                  AND r.status = com.hoabanrestaurant.backend.enums.ReservationStatus.CONFIRMED
                  AND r.startTime <= :moment
                  AND r.endTime   >  :moment
                ORDER BY r.startTime DESC
            """)
    Optional<Reservation> findCurrentForTable(UUID tableId, Instant moment);

    // Trùng giờ trên 1 bàn
    @Query("""
              SELECT r FROM Reservation r
              JOIN com.hoabanrestaurant.backend.entity.ReservationTableLink l
                ON l.reservation = r
              WHERE l.table.id = :tableId
                AND r.status IN (com.hoabanrestaurant.backend.enums.ReservationStatus.PENDING,
                                 com.hoabanrestaurant.backend.enums.ReservationStatus.CONFIRMED)
                AND r.startTime < :endTime
                AND r.endTime   > :startTime
            """)
    List<Reservation> findOverlapsByTable(UUID tableId, Instant startTime, Instant endTime);

    // Trùng giờ trên nhiều bàn (dùng khi gán nhiều bàn cho 1 reservation)
    @Query("""
              SELECT DISTINCT r FROM Reservation r
              JOIN com.hoabanrestaurant.backend.entity.ReservationTableLink l
                ON l.reservation = r
              WHERE l.table.id IN :tableIds
                AND r.status IN (com.hoabanrestaurant.backend.enums.ReservationStatus.PENDING,
                                 com.hoabanrestaurant.backend.enums.ReservationStatus.CONFIRMED)
                AND r.startTime < :endTime
                AND r.endTime   > :startTime
            """)
    List<Reservation> findOverlapsByTables(List<UUID> tableIds, Instant startTime, Instant endTime);

    List<Reservation> findByUser_Id(UUID userId);

    List<Reservation> findByStatus(ReservationStatus status);
}
