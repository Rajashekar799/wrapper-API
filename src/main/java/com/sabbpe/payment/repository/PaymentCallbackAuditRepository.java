package com.sabbpe.payment.repository;

import com.sabbpe.payment.entity.PaymentCallbackAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCallbackAuditRepository
        extends JpaRepository<PaymentCallbackAudit, Long> {
}
