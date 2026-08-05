package com.banking.accountservice.DTO;

import java.math.BigDecimal;

public record FraudDetected (String accountNumber, BigDecimal amount){
}
