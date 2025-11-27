package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, UUID> {
}