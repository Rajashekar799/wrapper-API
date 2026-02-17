package com.sabbpe.payment.service;

import com.sabbpe.payment.entity.InternalToken;
import com.sabbpe.payment.repository.InternalTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final InternalTokenRepository repo;

    // Generate token after merchant auth
    public String generateToken(String merchantId) {

        String tokenValue = UUID.randomUUID().toString();

        InternalToken token = new InternalToken();
        token.setToken(tokenValue);
        token.setMerchantId(merchantId);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(30));

        repo.save(token);

        return tokenValue;
    }

    // Validate token
    public void validateToken(String tokenValue) {

        InternalToken token = repo.findByToken(tokenValue)
                .orElseThrow(() ->
                        new RuntimeException("Invalid token"));

        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }
    }
}
