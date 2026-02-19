package com.sabbpe.payment.service;

import com.sabbpe.payment.entity.InternalToken;
import com.sabbpe.payment.repository.InternalTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalTokenService {

    private final InternalTokenRepository repo;

    @Value("${internal.token.expiry.minutes}")
    private int expiryMinutes;

    // ✅ Generate token
    public String generateToken(String merchantId) {

        String token = UUID.randomUUID().toString();

        InternalToken t = new InternalToken();
        t.setToken(token);
        t.setMerchantId(merchantId);
        t.setExpiryTime(
                LocalDateTime.now().plusMinutes(expiryMinutes));

        repo.save(t);

        return token;
    }
//validate token
public String validateToken(String authorization, String merchantId) {

    if (authorization == null || authorization.isBlank()) {
        throw new RuntimeException("Missing Authorization header");
    }

    // ⭐ REMOVE "Bearer "
    String token = authorization.replace("Bearer ", "").trim();

    InternalToken t = repo.findByToken(token)
            .orElseThrow(() ->
                    new RuntimeException("INVALID_TOKEN"));

    // expiry check
    if (t.getExpiryTime().isBefore(LocalDateTime.now())) {
        throw new RuntimeException("TOKEN_EXPIRED");
    }

    // merchant validation
    if (!t.getMerchantId().equals(merchantId)) {
        throw new RuntimeException("MERCHANT_TOKEN_MISMATCH");
    }

    return t.getMerchantId();
}


}
