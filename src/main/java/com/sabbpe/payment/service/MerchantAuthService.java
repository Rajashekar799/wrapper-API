package com.sabbpe.payment.service;

import com.sabbpe.payment.entity.ClientProfile;
import com.sabbpe.payment.repository.ClientProfileRepository; // ✅ FIXED IMPORT

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.sabbpe.payment.enums.AccountType;
import com.sabbpe.payment.enums.KycStatus;


@Service
@RequiredArgsConstructor
public class MerchantAuthService {

    private final ClientProfileRepository clientRepo;

    // =====================================================
    // ✅ AUTHENTICATE MERCHANT USING API KEY + SECRET
    // =====================================================
    public ClientProfile authenticate(String authorizationHeader) {

    if (authorizationHeader == null || authorizationHeader.isBlank()) {
        throw new RuntimeException("Missing Authorization header");
    }

    // ✅ Remove Bearer safely (case insensitive)
    String token = authorizationHeader.replaceFirst("(?i)^Bearer\\s+", "").trim();

    // Expected → USER:PASS
    String[] parts = token.split(":");

    if (parts.length != 2) {
        throw new RuntimeException("Invalid authorization format");
    }

    String apiKey = parts[0].trim();
    String apiSecret = parts[1].trim();

    System.out.println("AUTH USER -> [" + apiKey + "]");
    System.out.println("AUTH PASS -> [" + apiSecret + "]");

    ClientProfile client = clientRepo
            .authenticate(apiKey, apiSecret)
            .orElseThrow(() ->
                    new RuntimeException("Invalid merchant credentials"));

    // ✅ ENUM check
    if (client.getAccountType() != AccountType.merchant) {
        throw new RuntimeException("Invalid account type");
    }

    if (!"active".equalsIgnoreCase(client.getAccountStatus())) {
        throw new RuntimeException("Merchant not active");
    }

    return client;
}

}
