package com.banking.transaction_service.DTO;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransactionRefunded(String senderAccountNumber,
                                  String receiverAccountNumber,
                                  BigDecimal amount, String referenceId) {
}
