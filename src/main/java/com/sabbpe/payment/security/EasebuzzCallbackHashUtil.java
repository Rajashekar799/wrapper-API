package com.sabbpe.payment.security;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;

@Component
public class EasebuzzCallbackHashUtil {

    public String generateHash(String input) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(input.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("Callback hash generation failed");
        }
    }
}
