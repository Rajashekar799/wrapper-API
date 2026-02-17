package com.sabbpe.payment.dto;

import lombok.Data;

@Data
public class InitiatePaymentRequest {

private String merchantId;
    private Double amount;
    private String customerName;
    private String email;
    private String phone;

}
