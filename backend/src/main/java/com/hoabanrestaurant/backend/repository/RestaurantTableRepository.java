package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.RestaurantTable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface
RestaurantTableRepository extends JpaRepository<RestaurantTable, UUID> {
    boolean existsByArea_IdAndCodeIgnoreCase(UUID areaId, String code);

    List<RestaurantTable> findByArea_Id(UUID areaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM RestaurantTable t WHERE t.id = :id")
    Optional<RestaurantTable> findByIdForUpdate(UUID id);

    /**
     * Tìm các bàn trống (chưa bị đặt) trong khoảng thời gian startTime -> endTime
     * Chỉ lấy bàn có status = AVAILABLE và không có reservation trùng giờ
     */
    @Query("""
            SELECT t FROM RestaurantTable t
            WHERE t.status = com.hoabanrestaurant.backend.enums.TableStatus.AVAILABLE
            AND t.id NOT IN (
                SELECT l.table.id FROM ReservationTableLink l
                WHERE l.reservation.status IN (
                    com.hoabanrestaurant.backend.enums.ReservationStatus.PENDING,
                    com.hoabanrestaurant.backend.enums.ReservationStatus.CONFIRMED
                )
                AND l.reservation.startTime < :endTime
                AND l.reservation.endTime > :startTime
            )
            ORDER BY t.area.name, t.code
            """)
    List<RestaurantTable> findAvailableTables(@Param("startTime") Instant startTime,
                                              @Param("endTime") Instant endTime);

    /**
     * Tìm các bàn trống trong một khu vực cụ thể
     */
    @Query("""
            SELECT t FROM RestaurantTable t
            WHERE t.area.id = :areaId
            AND t.status = com.hoabanrestaurant.backend.enums.TableStatus.AVAILABLE
            AND t.id NOT IN (
                SELECT l.table.id FROM ReservationTableLink l
                WHERE l.reservation.status IN (
                    com.hoabanrestaurant.backend.enums.ReservationStatus.PENDING,
                    com.hoabanrestaurant.backend.enums.ReservationStatus.CONFIRMED
                )
                AND l.reservation.startTime < :endTime
                AND l.reservation.endTime > :startTime
            )
            ORDER BY t.code
            """)
    List<RestaurantTable> findAvailableTablesByArea(@Param("areaId") UUID areaId,
                                                    @Param("startTime") Instant startTime,
                                                    @Param("endTime") Instant endTime);
}
