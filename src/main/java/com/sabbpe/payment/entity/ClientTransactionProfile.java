package com.sabbpe.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_transaction_profile")
@Getter
@Setter
public class ClientTransactionProfile {

    // ===============================
    // PRIMARY KEY
    // ===============================
    @Id
    @Column(name = "merchant_processor_id", length = 36)
    private String merchantProcessorId;

    // ===============================
    // FOREIGN KEY → client_profile
    // ===============================
    @Column(name = "client_id", length = 36)
    private String clientId;

    /*
     OPTIONAL (Senior-level mapping — recommended later)

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "client_id", insertable = false, updatable = false)
     private ClientProfile clientProfile;
    */

    // ===============================
    // NTT DATA CREDENTIALS
    // ===============================
    @Column(name = "ntt_userid", length = 36)
    private String nttUserId;

    @Column(name = "ntt_password", length = 36)
    private String nttPassword;

    @Column(name = "ntt_merchantid", length = 36)
    private String nttMerchantId;

    // ===============================
    // EASEBUZZ CREDENTIALS
    // ===============================
    @Column(name = "easebuzz_key", length = 64)
    private String easebuzzKey;

    @Column(name = "easebuzz_salt", length = 64)
    private String easebuzzSalt;

    // ===============================
    // AUDIT FIELDS
    // ===============================
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
