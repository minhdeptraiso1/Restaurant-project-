package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.Order;
import com.hoabanrestaurant.backend.enums.OrderStatus;
import com.hoabanrestaurant.backend.enums.OrderTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByTable_IdAndStatus(UUID tableId, OrderStatus status);

    List<Order> findByUser_IdAndStatusOrderByCreatedAtDesc(UUID userId, OrderStatus status);

    boolean existsByTable_IdAndStatus(UUID tableId, OrderStatus status);

    Optional<Order> findFirstByUser_IdAndStatusAndTypes(UUID userId, OrderStatus status, OrderTypes types);

    List<Order> findAllByOrderByCreatedAtDesc();

    @Query("""
                SELECT o.status, COUNT(o)
                FROM Order o
                WHERE o.createdAt >= :start AND o.createdAt < :end
                GROUP BY o.status
            """)
    List<Object[]> countByStatusBetween(
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query("""
                SELECT o.createdAt, SUM(o.total)
                FROM Order o
                WHERE o.createdAt BETWEEN :from AND :to
                  AND o.status = :status
                GROUP BY o.createdAt
            """)
    List<Object[]> sumRevenueByCreatedAtBetween(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("status") OrderStatus status
    );


}
