package com.sabbpe.payment.service;

import com.sabbpe.payment.entity.ClientProfile;
import com.sabbpe.payment.entity.ClientTransactionProfile;
import com.sabbpe.payment.enums.AccountType;
import com.sabbpe.payment.enums.KycStatus;
import com.sabbpe.payment.repository.ClientProfileRepository;
import com.sabbpe.payment.repository.ClientTransactionProfileRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantValidationService {

    private final ClientProfileRepository repository;
    private final ClientTransactionProfileRepository txnRepo;

    // =====================================================
    // ✅ VALIDATE MERCHANT FROM UDF1 (client_id)
    // =====================================================
    public ClientProfile validateMerchant(String clientId) {

        ClientProfile client = repository.findByClientId(clientId)
                .orElseThrow(() ->
                        new RuntimeException("Merchant not found"));

        // ✅ ACCOUNT TYPE CHECK (ENUM SAFE)
        if (client.getAccountType() != AccountType.merchant) {
            throw new RuntimeException("Invalid account type");
        }

        // ✅ ACCOUNT STATUS CHECK
        if (!"active".equalsIgnoreCase(client.getAccountStatus())) {
            throw new RuntimeException("Merchant inactive");
        }

        // ✅ KYC CHECK (ENUM SAFE)
        if (client.getKycStatus() != KycStatus.verified) {
            throw new RuntimeException("KYC not approved");
        }

        return client;
    }

    // =====================================================
    // ✅ FETCH CLIENT TRANSACTION PROFILE
    // =====================================================
    public ClientTransactionProfile getTransactionProfile(String clientId) {

        return txnRepo.findByClientId(clientId)
                .orElseThrow(() ->
                        new RuntimeException("Transaction profile not found"));
    }
}
