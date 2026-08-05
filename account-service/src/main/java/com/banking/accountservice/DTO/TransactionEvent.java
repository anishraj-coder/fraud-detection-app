package com.banking.accountservice.DTO;

import java.math.BigDecimal;

public record TransactionEvent(String accountNumber, BigDecimal amount){
}
