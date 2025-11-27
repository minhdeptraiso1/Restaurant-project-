package com.hoabanrestaurant.backend.repository;

import com.hoabanrestaurant.backend.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    @Query("SELECT r FROM Review r ORDER BY r.rating DESC, r.createdAt DESC")
    List<Review> findLatestTop12(Pageable pageable);

    boolean existsByOrder_Id(UUID orderId);


}
