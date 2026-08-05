import React, { useState } from 'react';
import { useTransactionHistory } from '../../hooks/useTransactions';
import { useMyAccount } from '../../hooks/useAccount';
import { StatusBadge } from './StatusBadge';
import { formatCurrency, formatDate } from '../../lib/utils';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../ui/card';
import { Table, TableHeader, TableBody, TableHead, TableRow, TableCell } from '../ui/table';
import { Input } from '../ui/input';
import { Button } from '../ui/button';
import { ArrowUpRight, ArrowDownLeft, Search, RefreshCw, Loader2 } from 'lucide-react';

export const HistoryTable: React.FC = () => {
    const { data: transactions, isLoading, isError, refetch } = useTransactionHistory();
    const { data: account } = useMyAccount();
    const [searchTerm, setSearchTerm] = useState('');

    const filtered = Array.isArray(transactions)
        ? transactions.filter((tx) => {
            const term = searchTerm.toLowerCase();
            return (
                tx.referenceNumber.toLowerCase().includes(term) ||
                tx.senderAccountNumber.includes(term) ||
                tx.receiverAccountNumber.includes(term) ||
                tx.status.toLowerCase().includes(term)
            );
        })
        : [];

    return (
        <Card className="border-border shadow-lg">
            <CardHeader className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 pb-4">
                <div>
                    <CardTitle className="text-base font-semibold">Transaction Ledger</CardTitle>
                    <CardDescription className="text-xs">Real-time audit log of credits and debits</CardDescription>
                </div>
                <div className="flex items-center gap-3">
                    <div className="relative">
                        <Search className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                        <Input
                            type="text"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            placeholder="Search reference or account..."
                            className="h-9 w-48 sm:w-64 pl-9 pr-3 text-xs"
                        />
                    </div>
                    <Button
                        variant="outline"
                        size="icon"
                        onClick={() => refetch()}
                        className="h-9 w-9"
                        title="Refresh History"
                    >
                        <RefreshCw className="h-4 w-4 text-muted-foreground" />
                    </Button>
                </div>
            </CardHeader>

            <CardContent>
                {isLoading && (
                    <div className="flex items-center justify-center py-12 text-xs text-muted-foreground gap-2">
                        <Loader2 className="h-5 w-5 animate-spin text-primary" />
                        Loading ledger entries...
                    </div>
                )}

                {isError && (
                    <div className="p-4 text-center text-xs text-destructive bg-destructive/10 border border-destructive/20 rounded-md">
                        Failed to fetch transaction history ledger. Ensure JWT token is valid.
                    </div>
                )}

                {!isLoading && !isError && filtered.length === 0 && (
                    <div className="py-12 text-center text-xs text-muted-foreground border border-dashed border-border/50 rounded-lg">
                        No transactions found in ledger matching your criteria.
                    </div>
                )}

                {!isLoading && !isError && filtered.length > 0 && (
                    <Table>
                        <TableHeader>
                            <TableRow className="bg-muted/20">
                                <TableHead>Type</TableHead>
                                <TableHead>Reference No</TableHead>
                                <TableHead>Sender / Receiver</TableHead>
                                <TableHead>Amount (₹)</TableHead>
                                <TableHead>Status</TableHead>
                                <TableHead className="text-right">Created At</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {filtered.map((tx) => {
                                const isDebit = account?.accountNumber
                                    ? tx.senderAccountNumber === account.accountNumber
                                    : true;

                                return (
                                    <TableRow key={tx.id || tx.referenceNumber}>
                                        <TableCell>
                                            <div className="flex items-center gap-2">
                                                <div
                                                    className={`flex h-7 w-7 items-center justify-center rounded-full ${
                                                        isDebit
                                                            ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                                                            : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                                                    }`}
                                                >
                                                    {isDebit ? (
                                                        <ArrowUpRight className="h-4 w-4" />
                                                    ) : (
                                                        <ArrowDownLeft className="h-4 w-4" />
                                                    )}
                                                </div>
                                                <span className="font-semibold text-foreground">
                                                    {isDebit ? 'DEBIT' : 'CREDIT'}
                                                </span>
                                            </div>
                                        </TableCell>
                                        <TableCell className="font-mono text-muted-foreground">{tx.referenceNumber}</TableCell>
                                        <TableCell className="font-mono text-muted-foreground">
                                            <div>From: {tx.senderAccountNumber}</div>
                                            <div>To: {tx.receiverAccountNumber}</div>
                                        </TableCell>
                                        <TableCell className="font-bold text-sm">
                                            <span className={isDebit ? 'text-rose-400' : 'text-emerald-400'}>
                                                {isDebit ? '-' : '+'}{formatCurrency(tx.amount)}
                                            </span>
                                        </TableCell>
                                        <TableCell>
                                            <StatusBadge status={tx.status} />
                                        </TableCell>
                                        <TableCell className="text-right font-mono text-muted-foreground">
                                            {formatDate(tx.createdAt)}
                                        </TableCell>
                                    </TableRow>
                                );
                            })}
                        </TableBody>
                    </Table>
                )}
            </CardContent>
        </Card>
    );
};
