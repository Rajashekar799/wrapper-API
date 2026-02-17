package com.sabbpe.payment.service;

import com.sabbpe.payment.dto.InitiatePaymentRequest;
import com.sabbpe.payment.dto.PaymentInitiateResponse;
import com.sabbpe.payment.dto.PaymentResponse;
import com.sabbpe.payment.dto.PaymentStatusResponse;
import com.sabbpe.payment.dto.RefundRequest;
import com.sabbpe.payment.dto.RefundResponse;
import com.sabbpe.payment.entity.Payment;
import com.sabbpe.payment.entity.Refund;
import com.sabbpe.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.sabbpe.payment.repository.RefundRepository;


import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final MerchantService merchantService;
    private final PaymentRepository paymentRepo;
    private final EasebuzzService easebuzzService;
    private final TokenService tokenService;
    private final RefundRepository refundRepo;



   public PaymentInitiateResponse initiatePayment(
        InitiatePaymentRequest req) {

    merchantService.validateMerchant(req.getMerchantId());

    Payment payment = new Payment();
    payment.setTxnId(UUID.randomUUID().toString());
    payment.setMerchantId(req.getMerchantId());
    payment.setAmount(req.getAmount());
    payment.setStatus("PENDING");

    paymentRepo.save(payment);

    return easebuzzService.createPaymentLink(payment);
}
public PaymentStatusResponse getPaymentStatus(String txnId) {

    Payment payment = paymentRepo.findByTxnId(txnId);

    if (payment == null) {
        throw new RuntimeException("Transaction not found");
    }

    // ✅ If already final, return DB status
    if (!"PENDING".equalsIgnoreCase(payment.getStatus())) {
        return new PaymentStatusResponse(
                payment.getTxnId(),
                payment.getStatus(),
                payment.getAmount());
    }

    // ✅ Verify with Easebuzz
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
public RefundResponse refundPayment(
        RefundRequest request) {

    Payment payment =
            paymentRepo.findByTxnId(request.getTxnId());

    if (payment == null) {
        throw new RuntimeException("Transaction not found");
    }

    // ✅ Allow refund only for successful payments
    if (!"SUCCESS".equalsIgnoreCase(payment.getStatus())) {
        throw new RuntimeException(
                "Refund allowed only for successful payments");
    }

    // ✅ Call Easebuzz
    String refundStatus =
            easebuzzService.initiateRefund(
                    payment,
                    request.getRefundAmount());

    // Save refund record
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
