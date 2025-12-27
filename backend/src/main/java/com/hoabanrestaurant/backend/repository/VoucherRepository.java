package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.Voucher;
import com.hoabanrestaurant.backend.enums.VoucherStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface VoucherRepository extends JpaRepository<Voucher, UUID> {
    Optional<Voucher> findByCodeIgnoreCase(String code);

    @Query("""
                SELECT v FROM Voucher v
                WHERE v.status = :status
                  AND (v.startAt IS NULL OR v.startAt <= :now)
                  AND (v.endAt   IS NULL OR v.endAt   >= :now)
                  AND v.pointCost > 0
                  AND v.pointCost <= :userPoints
            """)
    Page<Voucher> findRedeemable(
            @Param("status") VoucherStatus status,
            @Param("userPoints") long userPoints,
            @Param("now") Instant now,
            Pageable pageable
    );

}