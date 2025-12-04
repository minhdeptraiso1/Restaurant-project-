package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.Dish;
import com.hoabanrestaurant.backend.enums.MenuStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface DishRepository extends JpaRepository<Dish, UUID> {

    boolean existsByCategory_IdAndNameIgnoreCase(UUID categoryId, String name);

    List<Dish> findByStatus(MenuStatus status);

    List<Dish> findByCategory_IdAndStatus(UUID categoryId, MenuStatus status);

    Page<Dish> findByStatus(MenuStatus status, Pageable pageable);

    @Query("""
            SELECT d FROM Dish d
            WHERE d.status = 'ACTIVE'
            ORDER BY RANDOM()
            """)
    List<Dish> findRandomActiveDishes(Pageable pageable);

    List<Dish> findTop4BySignatureTrueAndStatus(MenuStatus status);


}
