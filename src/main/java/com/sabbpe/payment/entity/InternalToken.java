package com.sabbpe.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "InternalToken")
@Getter
@Setter
public class InternalToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token")
    private String token;

    @Column(name = "merchantId")
    private String merchantId;

    @Column(name = "expiryTime")
    private LocalDateTime expiryTime;
}
