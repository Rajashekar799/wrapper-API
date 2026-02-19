package com.sabbpe.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_callback_audit")
@Getter
@Setter
public class PaymentCallbackAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String txnId;

    private String merchantId;

    @Column(columnDefinition = "LONGTEXT")
    private String payload;   // full Easebuzz payload

    private LocalDateTime receivedAt;
}
