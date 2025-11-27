// repository/ReservationTableLinkRepository.java
package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.Reservation;
import com.hoabanrestaurant.backend.entity.ReservationTableLink;
import com.hoabanrestaurant.backend.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationTableLinkRepository extends JpaRepository<ReservationTableLink, ReservationTableLink.Id> {

    @Query("""
              SELECT l.table FROM ReservationTableLink l
                WHERE l.reservation.id = :reservationId
            """)
    List<RestaurantTable> findTablesByReservation(UUID reservationId);


    @Modifying
    @Query("DELETE FROM ReservationTableLink l WHERE l.reservation.id = :reservationId")
    void deleteAllByReservation(UUID reservationId);

    @Query("""
              SELECT r FROM Reservation r
              JOIN ReservationTableLink l ON l.reservation = r
              WHERE l.table.id IN :tableIds
                AND r.status IN (com.hoabanrestaurant.backend.enums.ReservationStatus.PENDING,
                                 com.hoabanrestaurant.backend.enums.ReservationStatus.CONFIRMED)
                AND r.startTime < :endTime
                AND r.endTime   > :startTime
            """)
    List<Reservation> findOverlapsForTables(List<UUID> tableIds, java.time.Instant startTime, java.time.Instant endTime);
}
