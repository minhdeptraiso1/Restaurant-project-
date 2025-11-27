package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.Area;
import com.hoabanrestaurant.backend.enums.AreaStatus;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AreaRepository extends JpaRepository<Area, UUID> {
    boolean existsByNameIgnoreCase(String name);

    List<Area> findByStatus(AreaStatus status);


    boolean existsByNameIgnoreCaseAndIdNot(@NotBlank String name, UUID id);
}
