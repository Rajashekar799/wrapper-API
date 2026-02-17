package com.sabbpe.payment.repository;

import com.sabbpe.payment.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository
        extends JpaRepository<Refund, Long> {
}
