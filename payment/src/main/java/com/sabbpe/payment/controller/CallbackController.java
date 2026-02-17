package com.sabbpe.payment.controller;

import com.sabbpe.payment.entity.Payment;
import com.sabbpe.payment.repository.PaymentRepository;
import com.sabbpe.payment.security.EasebuzzCallbackHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/callback")
public class CallbackController {

    private final PaymentRepository paymentRepo;
    private final EasebuzzCallbackHashUtil hashUtil;

    @Value("${easebuzz.key}")
    private String key;

    @Value("${easebuzz.salt}")
    private String salt;

    @PostMapping
    public ResponseEntity<String> handleCallback(
            @RequestParam String status,
            @RequestParam String txnid,
            @RequestParam String amount,
            @RequestParam String firstname,
            @RequestParam String email,
            @RequestParam String productinfo,
            @RequestParam String hash,
            @RequestParam(required = false) String easepayid) {

        Payment payment = paymentRepo.findByTxnId(txnid);

        if (payment == null) {
            return ResponseEntity.ok("INVALID_TXN");
        }

        // ✅ Build reverse hash string
        String hashString =
                salt + "|" + status + "|||||||||||" +
                email + "|" + firstname + "|" +
                productinfo + "|" + amount + "|" +
                txnid + "|" + key;

        String calculatedHash =
                hashUtil.generateHash(hashString);

        // ✅ HASH VERIFICATION
        if (!calculatedHash.equals(hash)) {
            return ResponseEntity.ok("HASH_MISMATCH");
        }

        // ✅ Idempotency check
        if (payment.isCallbackProcessed()) {
            return ResponseEntity.ok("ALREADY_PROCESSED");
        }

        // ✅ Update status
        if ("success".equalsIgnoreCase(status)) {
            payment.setStatus("SUCCESS");
        } else {
            payment.setStatus("FAILED");
        }

        payment.setEasebuzzTxnId(easepayid);
        payment.setCallbackProcessed(true);
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepo.save(payment);

        return ResponseEntity.ok("OK");
    }
}
