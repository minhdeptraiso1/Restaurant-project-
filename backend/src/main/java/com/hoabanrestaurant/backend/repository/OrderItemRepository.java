package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrder_Id(UUID orderId);

    Optional<OrderItem> findByIdAndOrder_Id(UUID itemId, UUID orderId);

    void deleteByOrder_Id(UUID orderId);

    void deleteAllByOrder_Id(UUID orderId);

}
