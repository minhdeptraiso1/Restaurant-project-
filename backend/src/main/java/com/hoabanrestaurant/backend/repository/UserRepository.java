package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Page<User> findByFullNameContainingIgnoreCase(String search, Pageable pageable);
}
