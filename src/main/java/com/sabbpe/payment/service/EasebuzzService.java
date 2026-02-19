package com.sabbpe.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sabbpe.payment.dto.PaymentInitiateResponse;
import com.sabbpe.payment.entity.ClientTransactionProfile;
import com.sabbpe.payment.entity.EasebuzzRequestResponse;
import com.sabbpe.payment.entity.Payment;
import com.sabbpe.payment.repository.EasebuzzRequestResponseRepository;
import com.sabbpe.payment.security.EasebuzzHashUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EasebuzzService {

    private final RestTemplate restTemplate;
    private final EasebuzzHashUtil hashUtil;
    private final EasebuzzRequestResponseRepository easebuzzRepo;
    private final ObjectMapper objectMapper;

    @Value("${easebuzz.url.initiate}")
    private String initiateUrl;

    @Value("${easebuzz.url.transaction}")
    private String transactionUrl;

    @Value("${easebuzz.url.refund}")
    private String refundUrl;

    // =====================================================
    // ✅ INITIATE PAYMENT
    // =====================================================
    public PaymentInitiateResponse createPaymentLink(
            Payment payment,
            ClientTransactionProfile txnProfile) {

        String key = txnProfile.getEasebuzzKey();
        String salt = txnProfile.getEasebuzzSalt();

        if (key == null || salt == null) {
            throw new RuntimeException("Easebuzz credentials missing");
        }

        String txnId = payment.getTxnId();
        String amount = String.format("%.2f", payment.getAmount());

        String productInfo = "Test Product";
        String firstName = "Customer";
        String email = "test@test.com";
        String phone = "9999999999";

        String udf1 = payment.getMerchantId();
        String udf2 = "EASEBUZZ";
        String udf3 = "PAYMENT_WRAPPER";

        // =====================================================
        // ✅ SAFE HASH BUILDER (NO PIPE COUNT BUG EVER)
        // =====================================================
        String[] hashFields = new String[17];

        hashFields[0] = key;
        hashFields[1] = txnId;
        hashFields[2] = amount;
        hashFields[3] = productInfo;
        hashFields[4] = firstName;
        hashFields[5] = email;
        hashFields[6] = udf1;
        hashFields[7] = udf2;
        hashFields[8] = udf3;

        // udf4 → udf10
        for (int i = 9; i <= 15; i++) {
            hashFields[i] = "";
        }

        hashFields[16] = salt;

        String hashString = String.join("|", hashFields);

        System.out.println("INIT HASH STRING >>> " + hashString);

        String hash = hashUtil.generateHash(hashString);

        // =====================================================
        // ✅ BUILD REQUEST BODY
        // =====================================================
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("key", key);
        body.add("txnid", txnId);
        body.add("amount", amount);
        body.add("productinfo", productInfo);
        body.add("firstname", firstName);
        body.add("email", email);
        body.add("phone", phone);

        body.add("udf1", udf1);
        body.add("udf2", udf2);
        body.add("udf3", udf3);

        // IMPORTANT → keep hash + request identical
        body.add("udf4", "");
        body.add("udf5", "");
        body.add("udf6", "");
        body.add("udf7", "");
        body.add("udf8", "");
        body.add("udf9", "");
        body.add("udf10", "");

        body.add("surl", "http://localhost:8080/callback");
        body.add("furl", "http://localhost:8080/callback");

        body.add("hash", hash);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        // =====================================================
        // ✅ SAVE REQUEST LOG
        // =====================================================
        EasebuzzRequestResponse log = new EasebuzzRequestResponse();

        try {
            log.setId(UUID.randomUUID().toString()); // ⭐ IMPORTANT
            log.setClientKey(key);
            log.setClientSalt("MASKED"); // safer
            log.setOriginalPlainRequestJson(
                    objectMapper.writeValueAsString(payment));
            log.setHash(hash);
            log.setRequestEasebuzzUrl(initiateUrl);
            log.setRequestFullPayload(
                    objectMapper.writeValueAsString(body));
            log.setResponseEasebuzzPayload("INITIATED");

            easebuzzRepo.save(log);

        } catch (Exception e) {
            System.out.println("Request logging failed: " + e.getMessage());
        }

        // =====================================================
        // ✅ CALL EASEBUZZ
        // =====================================================
        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        initiateUrl,
                        request,
                        Map.class);

        Map resp = response.getBody();

        System.out.println("===== EASEBUZZ INITIATE RESPONSE =====");
        System.out.println(resp);
        System.out.println("======================================");

        // =====================================================
        // ✅ UPDATE RESPONSE LOG
        // =====================================================
        try {
            log.setResponseEasebuzzPayload(
                    objectMapper.writeValueAsString(resp));
            easebuzzRepo.save(log);

        } catch (Exception e) {
            System.out.println("Response logging failed: " + e.getMessage());
        }

        if (resp != null &&
                Integer.valueOf(1).equals(resp.get("status"))) {

            String accessKey = resp.get("data").toString();
            String paymentUrl =
                    "https://testpay.easebuzz.in/pay/" + accessKey;

            return new PaymentInitiateResponse(1, paymentUrl);
        }

        throw new RuntimeException("Easebuzz initiate failed");
    }
    // =====================================================
// ✅ PAYMENT STATUS CHECK
// =====================================================
public String fetchPaymentStatus(String txnId) {

    try {
        MultiValueMap<String, String> body =
                new LinkedMultiValueMap<>();

        body.add("txnid", txnId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        transactionUrl,
                        request,
                        Map.class);

        Map resp = response.getBody();

        if (resp != null && resp.containsKey("data")) {

            Map data = (Map) resp.get("data");

            if (data != null && data.get("status") != null) {
                return data.get("status").toString();
            }
        }

        return "PENDING";

    } catch (Exception ex) {
        return "PENDING";
    }
}
// =====================================================
// ✅ REFUND (STUB)
// =====================================================
public String initiateRefund(Payment payment, Double refundAmount) {
    return "REFUND_INITIATED";
}

}
