package com.sabbpe.payment.repository;

import com.sabbpe.payment.entity.EasebuzzPayments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EasebuzzPaymentsRepository
        extends JpaRepository<EasebuzzPayments, Long> {

    Optional<EasebuzzPayments> findByTxnId(String txnId);
}
