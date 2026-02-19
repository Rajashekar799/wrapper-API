package com.sabbpe.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "easebuzz_payments")
@Getter
@Setter
public class EasebuzzPayments {

    // ===============================
    // PRIMARY KEY (AUTO INCREMENT)
    // ===============================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===============================
    // BASIC PAYMENT INFO
    // ===============================
    @Column(name = "txn_id", nullable = false, unique = true)
    private String txnId;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // ===============================
    // STATUS FIELDS
    // ===============================
    @Column(name = "gateway_status", nullable = false)
    private String gatewayStatus;   // success / failure / dropped etc

    @Column(name = "normalized_status", nullable = false)
    private String normalizedStatus; // SUCCESS / FAILED / CANCELLED

    // ===============================
    // HASH VALIDATION
    // ===============================
    @Column(name = "hash")
    private String hash;

    @Column(name = "hash_validated", nullable = false)
    private Boolean hashValidated;

    // ===============================
    // FULL CALLBACK RESPONSE
    // ===============================
    @Lob
    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    // ===============================
    // TIMESTAMPS
    // ===============================
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
