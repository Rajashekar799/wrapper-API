package com.sabbpe.payment.repository;

import com.sabbpe.payment.entity.EasebuzzRequestResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EasebuzzRequestResponseRepository
        extends JpaRepository<EasebuzzRequestResponse, String> {

    @Query(value =
            "SELECT * FROM Easebuzz_Request_Response " +
            "WHERE request_full_payload LIKE %:txnid% " +
            "ORDER BY created_at DESC LIMIT 1",
            nativeQuery = true)
    Optional<EasebuzzRequestResponse> findLatestByTxnId(
            @Param("txnid") String txnid);
}
