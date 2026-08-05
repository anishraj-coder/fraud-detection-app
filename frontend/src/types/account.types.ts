export type AccountStatus = 'ACTIVE' | 'PENDING_INITIAL_DEPOSIT' | 'BLOCKED';
export type AccountType = 'SAVINGS' | 'CURRENT' | 'CHECKING' | 'BUSINESS';

export interface Account {
    id: string;
    accountNumber: string;
    userId: string;
    accountHolderName: string;
    accountType: AccountType;
    accountStatus: AccountStatus;
    email: string;
    phone: string;
    accountBalance: number;
    dailyTransactionLimit: number;
    createdAt: string;
    updatedAt: string;
}

export interface OnboardingRequest {
    accountHolderName: string;
    accountType: AccountType;
    phone: string;
    initialDeposit: number;
}
