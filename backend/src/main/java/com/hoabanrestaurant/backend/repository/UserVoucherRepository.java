package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, UUID> {


    // UserVoucherRepository.java
    boolean existsByUser_IdAndVoucher_IdAndRedeemedFalse(UUID userId, UUID voucherId);

    Optional<UserVoucher> findByIdAndUser_IdAndRedeemedFalse(UUID id, UUID userId);

    List<UserVoucher> findByUser_IdAndRedeemedFalse(UUID userId);

}
