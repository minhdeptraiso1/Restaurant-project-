package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.Order;
import com.hoabanrestaurant.backend.enums.OrderStatus;
import com.hoabanrestaurant.backend.enums.OrderTypes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByTable_IdAndStatus(UUID tableId, OrderStatus status);

    List<Order> findByUser_IdAndStatusOrderByCreatedAtDesc(UUID userId, OrderStatus status);

    boolean existsByTable_IdAndStatus(UUID tableId, OrderStatus status);

    Optional<Order> findFirstByUser_IdAndStatusAndTypes(UUID userId, OrderStatus status, OrderTypes types);

    List<Order> findAllByOrderByCreatedAtDesc();

}
