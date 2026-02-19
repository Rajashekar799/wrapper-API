package com.sabbpe.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sabbpe.payment.entity.PaymentCallbackAudit;
import com.sabbpe.payment.repository.PaymentCallbackAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CallbackAuditService {

    private final PaymentCallbackAuditRepository repository;
    private final ObjectMapper objectMapper;

    public void storeCallback(Map<String,String> payload) {

        try {
            PaymentCallbackAudit audit = new PaymentCallbackAudit();

            audit.setTxnId(payload.get("txnid"));
            audit.setMerchantId(payload.get("udf1"));
            audit.setPayload(objectMapper.writeValueAsString(payload));
            audit.setReceivedAt(LocalDateTime.now());

            repository.save(audit);

        } catch (Exception e) {
            // never fail payment flow because of audit logging
            System.out.println("Audit save failed: " + e.getMessage());
        }
    }
}
