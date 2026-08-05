export type TransactionStatus =
    | 'PENDING'
    | 'DEBITED'
    | 'PENDING_VERIFICATION'
    | 'COMPLETED'
    | 'FAILED'
    | 'FLAGGED'
    | 'FAILED_REFUNDED'
    | 'REQUIRES_MANUAL_INTERVENTION';

export type TransactionType = 'TRANSFER' | 'INITIAL_DEPOSIT';

export interface Transaction {
    id: string;
    senderAccountNumber: string;
    receiverAccountNumber: string;
    referenceNumber: string;
    amount: number;
    status: TransactionStatus;
    type: TransactionType;
    description?: string;
    failureReason?: string;
    createdAt: string;
    completedAt?: string;
}

export interface CustomerTransferRequest {
    receiverAccountNumber: string;
    amount: number;
    description?: string;
}

export interface OtpVerificationPayload {
    referenceId: string;
    amount: number;
    otp: string;
}
