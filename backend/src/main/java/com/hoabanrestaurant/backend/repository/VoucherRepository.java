package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface VoucherRepository extends JpaRepository<Voucher, UUID> {
    Optional<Voucher> findByCodeIgnoreCase(String code);

    @Query("""
            SELECT v FROM Voucher v
            WHERE v.status = com.hoabanrestaurant.backend.enums.VoucherStatus.ACTIVE
              AND (v.startAt IS NULL OR v.startAt <= :now)
              AND (v.endAt   IS NULL OR v.endAt   >= :now)
              AND v.pointCost > 0
              AND v.pointCost <= :userPoints
            """)
    Page<Voucher> findRedeemable(long userPoints, Instant now, Pageable pageable);
}