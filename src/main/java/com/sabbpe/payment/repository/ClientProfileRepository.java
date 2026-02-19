package com.sabbpe.payment.repository;

import com.sabbpe.payment.entity.ClientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientProfileRepository
        extends JpaRepository<ClientProfile, String> {

    Optional<ClientProfile> findByClientId(String clientId);

    // ✅ UAT SAFE AUTH QUERY (NATIVE SQL)
    @Query(value = """
        SELECT *
        FROM client_profile
        WHERE TRIM(transaction_userid) = TRIM(:user)
          AND TRIM(transaction_password) = TRIM(:pass)
        LIMIT 1
        """,
        nativeQuery = true)
    Optional<ClientProfile> authenticate(
            @Param("user") String user,
            @Param("pass") String pass);
}
