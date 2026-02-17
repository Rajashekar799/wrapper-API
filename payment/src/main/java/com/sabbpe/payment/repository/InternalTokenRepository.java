package com.sabbpe.payment.repository;

import com.sabbpe.payment.entity.InternalToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InternalTokenRepository
        extends JpaRepository<InternalToken, Long> {

    Optional<InternalToken> findByToken(String token);
}
