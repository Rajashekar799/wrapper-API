package com.sabbpe.payment.service;

import com.sabbpe.payment.dto.*;
import com.sabbpe.payment.entity.ClientProfile;
import com.sabbpe.payment.entity.ClientTransactionProfile;
import com.sabbpe.payment.entity.Payment;
import com.sabbpe.payment.entity.Refund;
import com.sabbpe.payment.repository.PaymentRepository;
import com.sabbpe.payment.repository.RefundRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    // ✅ AUTHENTICATION (Bearer USER:PASS)
    private final MerchantAuthService merchantAuthService;

    // ✅ BUSINESS VALIDATION
    private final MerchantValidationService merchantValidationService;

    private final PaymentRepository paymentRepo;
    private final EasebuzzService easebuzzService;
    private final RefundRepository refundRepo;

    // =====================================================
    // ✅ INITIATE PAYMENT
    // =====================================================
    public PaymentInitiateResponse initiatePayment(
            String authorization,
            InitiatePaymentRequest req) {

        // -------------------------------------------------
        // STEP 1 — AUTHENTICATE USING HEADER ONLY
        // -------------------------------------------------
        ClientProfile merchant =
                merchantAuthService.authenticate(authorization);

        // -------------------------------------------------
        // STEP 2 — VALIDATE MERCHANT (ACTIVE + KYC)
        // -------------------------------------------------
        merchant = merchantValidationService
                .validateMerchant(merchant.getClientId());

        // -------------------------------------------------
        // STEP 3 — FETCH GATEWAY PROFILE
        // -------------------------------------------------
        ClientTransactionProfile txnProfile =
                merchantValidationService
                        .getTransactionProfile(merchant.getClientId());

        // -------------------------------------------------
        // STEP 4 — CREATE PAYMENT RECORD
        // -------------------------------------------------
        Payment payment = new Payment();
        payment.setTxnId(UUID.randomUUID().toString());
        payment.setMerchantId(merchant.getClientId());
        payment.setAmount(req.getAmount());
        payment.setStatus("PENDING");

        paymentRepo.save(payment);

        // -------------------------------------------------
        // STEP 5 — PROVIDER CHECK
        // -------------------------------------------------
        String provider = merchant.getPaymentProvider();

        if (provider == null ||
                !provider.equalsIgnoreCase("EASEBUZZ")) {
            throw new RuntimeException(
                    "Only EASEBUZZ provider supported currently");
        }

        // -------------------------------------------------
        // STEP 6 — CALL EASEBUZZ
        // -------------------------------------------------
        return easebuzzService
                .createPaymentLink(payment, txnProfile);
    }

    // =====================================================
    // ✅ PAYMENT STATUS
    // =====================================================
    public PaymentStatusResponse getPaymentStatus(String txnId) {

        Payment payment = paymentRepo.findByTxnId(txnId);

        if (payment == null) {
            throw new RuntimeException("Transaction not found");
        }

        // already completed
        if (!"PENDING".equalsIgnoreCase(payment.getStatus())) {
            return new PaymentStatusResponse(
                    payment.getTxnId(),
                    payment.getStatus(),
                    payment.getAmount());
        }

        String gatewayStatus =
                easebuzzService.fetchPaymentStatus(txnId);

        if ("success".equalsIgnoreCase(gatewayStatus)) {
            payment.setStatus("SUCCESS");
        } else if ("failure".equalsIgnoreCase(gatewayStatus)) {
            payment.setStatus("FAILED");
        }

        paymentRepo.save(payment);

        return new PaymentStatusResponse(
                payment.getTxnId(),
                payment.getStatus(),
                payment.getAmount());
    }

    // =====================================================
    // ✅ REFUND PAYMENT
    // =====================================================
    public RefundResponse refundPayment(RefundRequest request) {

        Payment payment =
                paymentRepo.findByTxnId(request.getTxnId());

        if (payment == null) {
            throw new RuntimeException("Transaction not found");
        }

        if (!"SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            throw new RuntimeException(
                    "Refund allowed only for successful payments");
        }

        String refundStatus =
                easebuzzService.initiateRefund(
                        payment,
                        request.getRefundAmount());

        Refund refund = new Refund();
        refund.setTxnId(payment.getTxnId());
        refund.setRefundAmount(request.getRefundAmount());
        refund.setRefundStatus(refundStatus);

        refundRepo.save(refund);

        return new RefundResponse(
                payment.getTxnId(),
                refundStatus,
                "Refund processed");
    }
}
