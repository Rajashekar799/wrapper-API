package com.sabbpe.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Merchant {

    @Id
    @Column(unique = true)
    private String merchantId;

    @Column(unique = true)
    private String apiKey;
    private String apiSecret;

    // ACTIVE / BLOCKED
    private String status;
}
