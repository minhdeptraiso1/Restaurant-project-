package com.hoabanrestaurant.backend.entity;

import com.hoabanrestaurant.backend.enums.MenuStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "combos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Combo {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;     // giá bán combo

    private String imageUrl;

    @Column(name = "min_people")
    @Builder.Default
    private Integer minPeople = 2;    // ví dụ 2

    @Column(name = "max_people")
    @Builder.Default
    private Integer maxPeople = 4;    // ví dụ 4

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MenuStatus status = MenuStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
