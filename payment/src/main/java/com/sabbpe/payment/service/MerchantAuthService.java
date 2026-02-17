package com.sabbpe.payment.service;

import com.sabbpe.payment.entity.Merchant;
import com.sabbpe.payment.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantAuthService {

    private final MerchantRepository merchantRepo;

    public Merchant authenticate(String merchantId,
                                 String authorizationHeader) {

        // ✅ Check header exists
        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing Authorization header");
        }

        // Remove "Bearer "
        String token = authorizationHeader.substring(7);

        // Expected format → apiKey:apiSecret
        String[] parts = token.split(":");

        if (parts.length != 2) {
            throw new RuntimeException("Invalid authorization format");
        }

        String apiKey = parts[0];
        String apiSecret = parts[1];

        // ✅ Fetch merchant
        Merchant merchant = merchantRepo
        .findByMerchantId(merchantId)
        .orElseThrow(() ->
                new RuntimeException("Merchant not found"));

        if (merchant == null)
            throw new RuntimeException("Merchant not found");

        if (!merchant.getApiKey().equals(apiKey))
            throw new RuntimeException("Invalid API Key");

        if (!merchant.getApiSecret().equals(apiSecret))
            throw new RuntimeException("Invalid API Secret");

        if (!Boolean.TRUE.equals(merchant.getActive()))
            throw new RuntimeException("Merchant inactive");

        return merchant;
    }
}
