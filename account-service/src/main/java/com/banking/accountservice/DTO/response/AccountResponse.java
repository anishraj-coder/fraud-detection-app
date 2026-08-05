package com.banking.accountservice.DTO.response;

import com.banking.accountservice.entity.enums.AccountStatus;
import com.banking.accountservice.entity.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter@Getter
@Schema(description = "Account details schema with all fields")
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
