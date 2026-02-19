package com.sabbpe.payment.repository;

import com.sabbpe.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Payment findByTxnId(String txnId);
}
