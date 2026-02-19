package com.sabbpe.payment.repository;

import com.sabbpe.payment.entity.ClientTransactionProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientTransactionProfileRepository
        extends JpaRepository<ClientTransactionProfile, String> {

    // ✅ Native query avoids Hibernate mismatch issues
    @Query(value = """
        SELECT *
        FROM client_transaction_profile
        WHERE TRIM(client_id) = TRIM(:clientId)
        LIMIT 1
        """,
        nativeQuery = true)
    Optional<ClientTransactionProfile> findByClientId(
            @Param("clientId") String clientId);
}
