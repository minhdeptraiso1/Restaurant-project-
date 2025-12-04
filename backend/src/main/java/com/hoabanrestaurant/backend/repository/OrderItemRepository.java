package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrder_Id(UUID orderId);

    Optional<OrderItem> findByIdAndOrder_Id(UUID itemId, UUID orderId);

    void deleteAllByOrder_Id(UUID orderId);

    // Tần suất món theo user
    @Query(value = """
                SELECT oi.item_id, COUNT(*) 
                FROM order_items oi
                JOIN orders o ON oi.order_id = o.id
                WHERE oi.item_type = 'DISH'
                  AND o.user_id = :userId
                GROUP BY oi.item_id
                ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<Object[]> findDishFrequencyByUser(UUID userId);


    // Best seller chung
    @Query(value = """
                SELECT item_id, COUNT(*) 
                FROM order_items 
                WHERE item_type = 'DISH'
                GROUP BY item_id 
                ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    List<Object[]> getBestSellers();


}
