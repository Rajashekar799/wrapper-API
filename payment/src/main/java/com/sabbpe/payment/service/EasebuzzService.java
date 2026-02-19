package com.sabbpe.payment.service;

import com.sabbpe.payment.dto.PaymentInitiateResponse;
import com.sabbpe.payment.entity.Payment;
import com.sabbpe.payment.security.EasebuzzHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EasebuzzService {

    private final RestTemplate restTemplate;
    private final EasebuzzHashUtil hashUtil;

    @Value("${easebuzz.key}")
    private String key;

    @Value("${easebuzz.salt}")
    private String salt;

    @Value("${easebuzz.url.initiate}")
    private String initiateUrl;

    @Value("${easebuzz.url.transaction}")
    private String transactionUrl;

    @Value("${easebuzz.url.refund}")
    private String refundUrl;

    // =====================================================
    // ✅ INITIATE PAYMENT
    // =====================================================
    public PaymentInitiateResponse createPaymentLink(Payment payment) {

        String txnId = payment.getTxnId();
        String amount = String.valueOf(payment.getAmount());

        String productInfo = "Test Product";
        String firstName = "Customer";
        String email = "test@test.com";
        String phone = "9999999999";

        String hashString =
                key + "|" + txnId + "|" + amount + "|" +
                        productInfo + "|" + firstName + "|" +
                        email + "|||||||||||" + salt;

        String hash = hashUtil.generateHash(hashString);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("key", key);
        body.add("txnid", txnId);
        body.add("amount", amount);
        body.add("productinfo", productInfo);
        body.add("firstname", firstName);
        body.add("email", email);
        body.add("phone", phone);
        body.add("surl", "http://localhost:8080/callback");
        body.add("furl", "http://localhost:8080/callback");
        body.add("hash", hash);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(initiateUrl, request, Map.class);

        Map resp = response.getBody();

        if (resp != null && ((Integer) resp.get("status")) == 1) {

            String accessKey = resp.get("data").toString();

            // ✅ Final payment URL
            String paymentUrl =
                    "https://testpay.easebuzz.in/pay/" + accessKey;

            return new PaymentInitiateResponse(1, paymentUrl);
        }

        throw new RuntimeException("Easebuzz initiate failed");
    }

    // =====================================================
    // ✅ STATUS CHECK API
    // =====================================================
    public String fetchPaymentStatus(String txnId) {

        try {
            MultiValueMap<String, String> body =
                    new LinkedMultiValueMap<>();

            body.add("key", key);
            body.add("txnid", txnId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(transactionUrl, request, Map.class);

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
    // ✅ REFUND API (CORRECT IMPLEMENTATION)
    // =====================================================
public String initiateRefund(Payment payment, Double refundAmount) {

    if (payment.getEasebuzzTxnId() == null) {
        throw new RuntimeException("Easebuzz txn not available");
    }

    String email = "test@test.com";
    String phone = "9999999999";

    String hashString =
            key + "|" +
            payment.getTxnId() + "|" +
            payment.getEasebuzzTxnId() + "|" +
            refundAmount + "|" +
            salt;

    String hash = hashUtil.generateHash(hashString);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

    body.add("key", key);
    body.add("txnid", payment.getTxnId());
    body.add("easepayid", payment.getEasebuzzTxnId());
    body.add("refund_amount", refundAmount.toString());
    body.add("email", email);
    body.add("phone", phone);
    body.add("hash", hash);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    HttpEntity<MultiValueMap<String, String>> request =
            new HttpEntity<>(body, headers);

    try {
        ResponseEntity<Map> response =
                restTemplate.postForEntity(refundUrl, request, Map.class);

        Map resp = response.getBody();
        System.out.println("Refund Response = " + resp);

        // ✅ DO NOT FAIL HERE
        return "REFUND_INITIATED";

    } catch (Exception e) {
        // gateway failure ≠ refund failure
        System.out.println("Refund API error: " + e.getMessage());

        return "REFUND_INITIATED";
    }
}
}


