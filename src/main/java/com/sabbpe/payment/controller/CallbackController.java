package com.sabbpe.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sabbpe.payment.entity.ClientProfile;
import com.sabbpe.payment.entity.ClientTransactionProfile;
import com.sabbpe.payment.entity.EasebuzzPayments;
import com.sabbpe.payment.entity.EasebuzzRequestResponse;
import com.sabbpe.payment.entity.Payment;
import com.sabbpe.payment.repository.EasebuzzPaymentsRepository;
import com.sabbpe.payment.repository.EasebuzzRequestResponseRepository;
import com.sabbpe.payment.repository.PaymentRepository;
import com.sabbpe.payment.security.EasebuzzCallbackHashUtil;
import com.sabbpe.payment.service.CallbackAuditService;
import com.sabbpe.payment.service.MerchantValidationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/callback")
public class CallbackController {

    private final PaymentRepository paymentRepo;
    private final EasebuzzCallbackHashUtil hashUtil;
    private final CallbackAuditService auditService;
    private final MerchantValidationService merchantValidationService;
    private final EasebuzzRequestResponseRepository easebuzzRepo;
    private final EasebuzzPaymentsRepository easebuzzPaymentsRepo;
    private final ObjectMapper objectMapper;

    // =====================================================
    // ✅ HANDLE EASEBUZZ CALLBACK
    // =====================================================
    @PostMapping
    public ResponseEntity<?> handleCallback(
            @RequestParam Map<String, String> payload) {

        // =====================================================
        // STORE RAW CALLBACK (AUDIT)
        // =====================================================
        auditService.storeCallback(payload);

        System.out.println("\n===== EASEBUZZ CALLBACK RECEIVED =====");
        payload.forEach((k, v) -> System.out.println(k + " : " + v));
        System.out.println("======================================\n");

        // =====================================================
        // EXTRACT REQUIRED FIELDS
        // =====================================================
        String status = payload.get("status");
        String txnid = payload.get("txnid");
        String amount = payload.get("amount");
        String firstname = payload.get("firstname");
        String email = payload.get("email");
        String productinfo = payload.get("productinfo");
        String hash = payload.get("hash");
        String easepayid = payload.get("easepayid");

        String udf1 = payload.get("udf1"); // merchantId
        String udf2 = payload.get("udf2");
        String udf3 = payload.get("udf3");

        // =====================================================
        // FIND PAYMENT
        // =====================================================
        Payment payment = paymentRepo.findByTxnId(txnid);

        if (payment == null) {
            return ResponseEntity.ok("INVALID_TXN");
        }

        // =====================================================
        // MERCHANT VALIDATION
        // =====================================================
        if (udf1 == null || !payment.getMerchantId().equals(udf1)) {
            return ResponseEntity.ok("MERCHANT_MISMATCH");
        }

        ClientProfile merchant =
                merchantValidationService.validateMerchant(udf1);

        // =====================================================
        // FETCH GATEWAY CREDENTIALS
        // =====================================================
        ClientTransactionProfile txnProfile =
                merchantValidationService.getTransactionProfile(udf1);

        String key = txnProfile.getEasebuzzKey();
        String salt = txnProfile.getEasebuzzSalt();

        // =====================================================
        // REVERSE HASH VALIDATION
        // =====================================================
        String reverseHashString = String.join("|",
                salt,
                status,
                "", "", "", "", "", "", "",
                udf3 == null ? "" : udf3,
                udf2 == null ? "" : udf2,
                udf1 == null ? "" : udf1,
                email == null ? "" : email,
                firstname == null ? "" : firstname,
                productinfo == null ? "" : productinfo,
                amount == null ? "" : amount,
                txnid == null ? "" : txnid,
                key
        );

        String calculatedHash =
                hashUtil.generateHash(reverseHashString);

        if (!calculatedHash.equalsIgnoreCase(hash)) {
            System.out.println("❌ HASH MISMATCH");
            return ResponseEntity.ok("HASH_MISMATCH");
        }

        // =====================================================
        // ✅ UPDATE EASEBUZZ REQUEST RESPONSE TABLE
        // =====================================================
        try {
            Optional<EasebuzzRequestResponse> optionalLog =
                    easebuzzRepo.findLatestByTxnId(txnid);

            if (optionalLog.isPresent()) {

                EasebuzzRequestResponse log = optionalLog.get();

                String callbackJson =
                        objectMapper.writeValueAsString(payload);

                log.setResponseEasebuzzPayload(callbackJson);

                easebuzzRepo.save(log);
            }

        } catch (Exception ex) {
            System.out.println("Easebuzz callback logging failed: "
                    + ex.getMessage());
        }

        // =====================================================
        // ✅ INSERT / UPDATE easebuzz_payments TABLE
        // =====================================================
        try {

            String normalizedStatus =
                    "success".equalsIgnoreCase(status)
                            ? "PAID"
                            : "FAILED";

            EasebuzzPayments record =
                    easebuzzPaymentsRepo
                            .findByTxnId(txnid)
                            .orElse(new EasebuzzPayments());

            record.setTxnId(txnid);
            record.setMerchantId(udf1);
            record.setAmount(new BigDecimal(amount));
            record.setGatewayStatus(status);
            record.setNormalizedStatus(normalizedStatus);
            record.setHash(hash);
            record.setHashValidated(true);
            record.setRawResponse(
                    objectMapper.writeValueAsString(payload));
            record.setUpdatedAt(LocalDateTime.now());

            if (record.getCreatedAt() == null) {
                record.setCreatedAt(LocalDateTime.now());
            }

            easebuzzPaymentsRepo.save(record);

        } catch (Exception ex) {
            System.out.println("easebuzz_payments save failed: "
                    + ex.getMessage());
        }

        // =====================================================
        // IDEMPOTENCY CHECK
        // =====================================================
        if (payment.isCallbackProcessed()) {
            return ResponseEntity.ok("ALREADY_PROCESSED");
        }

        // =====================================================
        // UPDATE PAYMENT STATUS
        // =====================================================
        if ("success".equalsIgnoreCase(status)) {
            payment.setStatus("SUCCESS");
        } else {
            payment.setStatus("FAILED");
        }

        payment.setEasebuzzTxnId(easepayid);
        payment.setCallbackProcessed(true);
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepo.save(payment);

        // =====================================================
        // REDIRECT USER TO MERCHANT WEBSITE
        // =====================================================
        String redirectUrl = merchant.getBusinessWebsite();

        if (redirectUrl != null && !redirectUrl.isBlank()) {

            String finalRedirect =
                    redirectUrl +
                    "?txnid=" + URLEncoder.encode(txnid, StandardCharsets.UTF_8) +
                    "&status=" + URLEncoder.encode(payment.getStatus(), StandardCharsets.UTF_8) +
                    "&easepayid=" + URLEncoder.encode(easepayid, StandardCharsets.UTF_8) +
                    "&amount=" + URLEncoder.encode(amount, StandardCharsets.UTF_8);

            System.out.println("✅ Redirecting user → " + finalRedirect);

            return ResponseEntity.status(302)
                    .header("Location", finalRedirect)
                    .build();
        }

        // =====================================================
        // FALLBACK RESPONSE
        // =====================================================
        return ResponseEntity.ok("PAYMENT_PROCESSED");
    }
}
