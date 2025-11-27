package com.hoabanrestaurant.backend.entity;

import com.hoabanrestaurant.backend.enums.PaymentMethod;
import com.hoabanrestaurant.backend.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// entity/Payment.java
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;          // CASH / CARD / QR (VNPay)

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;          // PENDING / SUCCEEDED / FAILED / (EXPIRED)

    private Instant paidAt;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    // ===== VNPay fields (mới thêm) =====
    @Column(name = "transaction_id", unique = true, length = 64)   // vnp_TxnRef
    private String transactionId;

    @Column(name = "bank_code", length = 32)                        // vnp_BankCode (VNPAYQR)
    private String bankCode;

    @Column(name = "card_type", length = 32)                        // vnp_CardType
    private String cardType;

    @Column(name = "description", length = 255)                     // vnp_OrderInfo
    private String description;

    @Column(name = "extra_data", length = 1024)                     // log thêm (BankTranNo,...)
    private String extraData;

    @Column(name = "expired_at")
    private Instant expiredAt;                                      // hạn QR


}


