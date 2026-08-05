package com.banking.transaction_service.entity.enums;

public enum TransactionStatus {
    PENDING,COMPLETED,PROCESSING,PENDING_VERIFICATION,
    FLAGGED, FAILED_REFUNDED, COMPENSATING, REQUIRES_MANUAL_INTERVENTION, DEBITED, FAILED
}
