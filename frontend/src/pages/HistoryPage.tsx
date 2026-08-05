import React from 'react';
import { HistoryTable } from '../components/ledger/HistoryTable';

export const HistoryPage: React.FC = () => {
    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold tracking-tight text-foreground">Transaction Audit Ledger</h1>
                <p className="text-xs text-muted-foreground">Comprehensive history of credits, debits, and SAGA state transactions</p>
            </div>
            <HistoryTable />
        </div>
    );
};
