package com.sabbpe.payment.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String txnId;
    private Double refundAmount;

    // INITIATED / SUCCESS / FAILED
    private String refundStatus;
}
