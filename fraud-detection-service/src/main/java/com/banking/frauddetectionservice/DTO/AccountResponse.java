package com.banking.frauddetectionservice.DTO;


import com.banking.frauddetectionservice.DTO.enums.AccountStatus;
import com.banking.frauddetectionservice.DTO.enums.AccountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class AccountResponse {

    private String id;

    private String accountNumber;

    private String accountHolderName;

    private AccountType accountType;

    private AccountStatus accountStatus;

    private String email;

    private String phone;

    private BigDecimal accountBalance;

    private BigDecimal dailyTransactionLimit;


    private LocalDateTime createdAt;

}
