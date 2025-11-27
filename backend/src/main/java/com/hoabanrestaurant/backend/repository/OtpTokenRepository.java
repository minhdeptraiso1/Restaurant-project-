package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.OtpToken;
import com.hoabanrestaurant.backend.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {

    @Query("""
            SELECT o FROM OtpToken o
            WHERE o.email = :email AND o.purpose = :purpose
            AND o.consumedAt IS NULL AND o.expiresAt > :now
            ORDER BY o.createdAt DESC
            """)
    List<OtpToken> findActive(String email, OtpPurpose purpose, Instant now);
}

