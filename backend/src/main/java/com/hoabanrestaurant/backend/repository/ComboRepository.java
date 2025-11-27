package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.Combo;
import com.hoabanrestaurant.backend.enums.MenuStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ComboRepository extends JpaRepository<Combo, UUID> {
    boolean existsByNameIgnoreCase(String name);

    List<Combo> findByStatus(MenuStatus status);


    @Query("""
                SELECT c FROM Combo c
                WHERE (:partySize IS NULL OR (c.minPeople <= :partySize AND c.maxPeople >= :partySize))
                  AND (:budget IS NULL OR c.price <= :budget)
                ORDER BY c.price ASC
            """)
    List<Combo> findByConditions(Integer partySize, BigDecimal budget);

    @Query("SELECT c FROM Combo c ORDER BY c.createdAt DESC LIMIT 10")
    List<Combo> findTop5();


}
