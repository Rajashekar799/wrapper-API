package com.sabbpe.payment.entity;

import com.sabbpe.payment.enums.AccountType;
import com.sabbpe.payment.enums.BusinessType;
import com.sabbpe.payment.enums.KycStatus;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "client_profile")
@Getter
@Setter
public class ClientProfile {

    @Id
    @Column(name = "client_id", length = 36, nullable = false)
    private String clientId;

    @Column(name = "client_onboarded_by", length = 36)
    private String clientOnboardedBy;

    // ENUM('merchant','distributor','customer')
    @Enumerated(EnumType.STRING)
    @Column(name = "client_account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "client_mobile", nullable = false, length = 20)
    private String clientMobile;

    @Column(name = "client_email", nullable = false)
    private String clientEmail;

    @Column(name = "client_password", nullable = false)
    private String clientPassword;

    @Column(name = "json_web_token", columnDefinition = "LONGTEXT")
    private String jsonWebToken;

    @Column(name = "client_account_status", nullable = false)
    private String accountStatus;

    @Column(name = "onboarding_completed", nullable = false)
    private Boolean onboardingCompleted;

    @Column(name = "client_business_name")
    private String businessName;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_business_type")
    private BusinessType businessType;

    @Column(name = "client_business_registration_number")
    private String businessRegistrationNumber;

    @Column(name = "client_tax_id")
    private String taxId;

    @Column(name = "client_business_industry")
    private String businessIndustry;

    @Column(name = "client_business_website")
    private String businessWebsite;

    @Column(name = "client_kyc_status")
    @Enumerated(EnumType.STRING)
    private KycStatus kycStatus;

    @Column(name = "client_payment_provider")
    private String paymentProvider;

    // ===== Transaction Credentials =====

    @Column(name = "transaction_userid", length = 36)
    private String transactionUserId;

    @Column(name = "transaction_password", length = 36)
    private String transactionPassword;

    @Column(name = "transaction_merchantid", length = 36)
    private String transactionMerchantId;

    @Column(name = "transaction_aes_key", length = 256)
    private String aesKey;

    @Column(name = "transaction_iv", length = 128)
    private String iv;

    // ===== NTT Credentials =====

    @Column(name = "ntt_userid", length = 36)
    private String nttUserId;

    @Column(name = "ntt_password", length = 36)
    private String nttPassword;

    @Column(name = "ntt_merchantid", length = 36)
    private String nttMerchantId;

    // ===== Audit timestamps =====

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
