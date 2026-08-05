package com.banking.transaction_service.DTO;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountResponse {

    private String id;
    private String accountNumber;
    private String accountHolderName;
    private String accountType;
    private String accountStatus;
    private String email;
    private String phone;
    private BigDecimal accountBalance;
    private BigDecimal dailyTransactionLimit;
    private LocalDateTime createdAt;
}