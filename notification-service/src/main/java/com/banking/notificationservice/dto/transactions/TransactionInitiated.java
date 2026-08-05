package com.banking.notificationservice.dto.transactions;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransactionInitiated(String senderAccountNumber,
                                   String receiverAccountNumber,
                                   BigDecimal amount,String referenceId) {
}
