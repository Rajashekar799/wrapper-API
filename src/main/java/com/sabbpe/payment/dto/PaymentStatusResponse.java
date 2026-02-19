package com.sabbpe.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentStatusResponse {

    private String txnId;
    private String status;
    private Double amount;
}
