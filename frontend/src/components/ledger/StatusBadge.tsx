import React from 'react';
import type { TransactionStatus } from '../../types/transaction.types';
import { Badge } from '../ui/badge';

interface Props {
    status: TransactionStatus;
}

export const StatusBadge: React.FC<Props> = ({ status }) => {
    switch (status) {
        case 'COMPLETED':
            return <Badge variant="success">COMPLETED</Badge>;
        case 'PENDING_VERIFICATION':
            return <Badge variant="warning" className="animate-pulse">PENDING OTP</Badge>;
        case 'FLAGGED':
            return <Badge variant="destructive">FLAGGED</Badge>;
        case 'DEBITED':
        case 'PENDING':
            return <Badge variant="info">PROCESSING</Badge>;
        case 'FAILED_REFUNDED':
            return <Badge variant="destructive">REFUNDED</Badge>;
        default:
            return <Badge variant="outline">{status}</Badge>;
    }
};
