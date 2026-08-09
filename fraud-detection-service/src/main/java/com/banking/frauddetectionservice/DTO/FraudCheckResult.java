package com.banking.frauddetectionservice.DTO;

import lombok.Builder;

@Builder
public record FraudCheckResult(boolean result, String reason, String referenceId) {
}
