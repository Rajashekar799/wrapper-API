package com.sabbpe.payment.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String txnId;

    private String merchantId;

    private Double amount;

    // PENDING / SUCCESS / FAILED
    private String status;

    private String easebuzzTxnId;

    // ✅ Added for callback protection
    private boolean callbackProcessed;

    private LocalDateTime updatedAt;
}
