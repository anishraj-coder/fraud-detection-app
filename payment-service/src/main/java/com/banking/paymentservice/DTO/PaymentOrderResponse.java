package com.banking.paymentservice.DTO;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter@Setter
@AllArgsConstructor@NoArgsConstructor
public class PaymentOrderResponse {

    private String accountNumber;
    private BigDecimal amount;
    private String status;
    private String currency;
    private String razorpayOrderId;
    private String razorpayKeyId;
}
