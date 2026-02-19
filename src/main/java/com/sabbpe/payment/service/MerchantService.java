package com.sabbpe.payment.service;

import com.sabbpe.payment.entity.Merchant;
import com.sabbpe.payment.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository repository;

    public Merchant validateMerchant(String merchantId) {

        Merchant merchant = repository.findById(merchantId)
                .orElseThrow(() ->
                        new RuntimeException("Merchant not found"));

        // ✅ Check active status
        if (!Boolean.TRUE.equals(merchant.getActive())) {
            throw new RuntimeException("Merchant inactive");
        }

        return merchant;
    }
}
