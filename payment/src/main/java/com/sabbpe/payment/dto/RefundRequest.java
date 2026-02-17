package com.sabbpe.payment.dto;

import lombok.Data;

@Data
public class RefundRequest {

    private String merchantId;
    private String txnId;
    private Double refundAmount;
}
