package com.sabbpe.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefundResponse {

    private String txnId;
    private String refundStatus;
    private String message;
}
