package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.Combo;
import com.hoabanrestaurant.backend.enums.MenuStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ComboRepository extends JpaRepository<Combo, UUID> {
    boolean existsByNameIgnoreCase(String name);

    List<Combo> findByStatus(MenuStatus status);


    @Query("""
            SELECT c FROM Combo c
            WHERE c.status = 'ACTIVE'
            ORDER BY RANDOM()
            """)
    List<Combo> findRandomCombos(Pageable pageable);


}
