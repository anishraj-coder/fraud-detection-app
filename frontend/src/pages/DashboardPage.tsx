import React from 'react';
import { AccountCard } from '../components/dashboard/AccountCard';
import { TransferCard } from '../components/dashboard/TransferCard';
import { SagaVisualizer } from '../components/dashboard/SagaVisualizer';
import { HistoryTable } from '../components/ledger/HistoryTable';

export const DashboardPage: React.FC = () => {
    return (
        <div className="space-y-8">
            {/* Top Grid: Account Profile & Money Transfer */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <AccountCard />
                <TransferCard />
            </div>

            {/* Middle Section: SAGA Pipeline Visualizer */}
            <SagaVisualizer />

            {/* Bottom Section: Transaction Ledger Table */}
            <HistoryTable />
        </div>
    );
};
