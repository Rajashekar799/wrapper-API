package com.sabbpe.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Easebuzz_Request_Response")
@Getter
@Setter
public class EasebuzzRequestResponse {

   @Id
   @Column(name = "id", length = 36, nullable = false, updatable = false)
   private String id;

    @Column(name = "client_key", length = 200)
    private String clientKey;

    @Column(name = "client_salt", length = 200)
    private String clientSalt;

    @Lob
    @Column(name = "original_plain_request_json",
            columnDefinition = "TEXT")
    private String originalPlainRequestJson;

    @Column(name = "hash", length = 256, nullable = false)
    private String hash;

    @Column(name = "request_easebuzz_url", length = 500, nullable = false)
    private String requestEasebuzzUrl;

    @Lob
    @Column(name = "request_full_payload", columnDefinition = "TEXT")
    private String requestFullPayload;

    @Lob
    @Column(name = "response_easebuzz_payload", columnDefinition = "TEXT")
    private String responseEasebuzzPayload;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}

