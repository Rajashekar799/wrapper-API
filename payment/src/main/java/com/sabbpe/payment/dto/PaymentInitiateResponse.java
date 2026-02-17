package com.sabbpe.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentInitiateResponse {

    private Integer status;
    private String paymentUrl;
}
