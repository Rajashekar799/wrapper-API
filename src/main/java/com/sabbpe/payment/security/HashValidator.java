package com.sabbpe.payment.security;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;

@Component
public class HashValidator {

    public boolean verifyHash(
            String payload,
            String receivedHash,
            String salt) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest((payload + salt).getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            String generatedHash = sb.toString();

            return generatedHash.equals(receivedHash);

        } catch (Exception e) {
            throw new RuntimeException("Hash validation failed");
        }
    }
}
