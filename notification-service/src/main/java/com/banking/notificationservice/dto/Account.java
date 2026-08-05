package com.banking.notificationservice.dto;

import com.banking.notificationservice.dto.enums.AccountStatus;
import com.banking.notificationservice.dto.enums.AccountType;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Account {

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

    private LocalDateTime updatedAt;


}