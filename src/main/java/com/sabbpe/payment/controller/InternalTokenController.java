package com.sabbpe.payment.controller;

import com.sabbpe.payment.service.InternalTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internal-token")
@RequiredArgsConstructor
public class InternalTokenController {

    private final InternalTokenService internalTokenService;

    // =====================================================
    // ✅ GENERATE INTERNAL TOKEN
    // =====================================================
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateToken(
            @RequestBody Map<String, String> request) {

        String merchantId = request.get("merchantId");

        String token =
                internalTokenService.generateToken(merchantId);

        // ✅ wrap inside Map
        return ResponseEntity.ok(
                Map.of("token", token)
        );
    }
}
