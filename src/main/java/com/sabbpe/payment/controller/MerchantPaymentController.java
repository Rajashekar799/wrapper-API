package com.sabbpe.payment.controller;

import com.sabbpe.payment.dto.InitiatePaymentRequest;
import com.sabbpe.payment.dto.RefundRequest;
import com.sabbpe.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/merchant/payment")
@RequiredArgsConstructor
public class MerchantPaymentController {

    private final PaymentService paymentService;

@PostMapping("/initiate")
public ResponseEntity<?> initiate(
        @RequestHeader("Authorization") String authorization,
        @RequestBody InitiatePaymentRequest request) {

    return ResponseEntity.ok(
            paymentService.initiatePayment(authorization, request));
}



@GetMapping("/status/{txnId}")
public ResponseEntity<?> status(
        @PathVariable String txnId) {

    return ResponseEntity.ok(
            paymentService.getPaymentStatus(txnId));
}
@PostMapping("/refund")
public ResponseEntity<?> refund(
        @RequestBody RefundRequest request) {

    return ResponseEntity.ok(
            paymentService.refundPayment(request));
}

}
