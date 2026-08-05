package com.banking.transaction_service.DTO;

import lombok.Builder;

@Builder
public record FraudCheckResult(boolean result,String reason,String referenceId) {
}
