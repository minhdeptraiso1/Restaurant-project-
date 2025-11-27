package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.Category;
import com.hoabanrestaurant.backend.enums.MenuStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByNameIgnoreCase(String name);

    List<Category> findByStatus(MenuStatus status);
}
