import React from 'react';
import { useMyAccount } from '../../hooks/useAccount';
import { formatCurrency } from '../../lib/utils';
import { Card, CardHeader, CardContent } from '../ui/card';
import { Badge } from '../ui/badge';
import { Button } from '../ui/button';
import { RefreshCw, UserCheck, AlertTriangle, Lock } from 'lucide-react';
import { Link } from 'react-router';

export const AccountCard: React.FC = () => {
    const { data: account, isLoading, isError, refetch } = useMyAccount();

    if (isLoading) {
        return (
            <Card className="p-6 animate-pulse flex flex-col gap-4">
                <div className="h-6 w-32 bg-muted rounded" />
                <div className="h-10 w-48 bg-muted rounded" />
                <div className="h-4 w-40 bg-muted rounded" />
            </Card>
        );
    }

    if (isError || !account) {
        return (
            <Card className="border-amber-500/30 bg-amber-500/10 p-6 text-amber-300 flex flex-col gap-3">
                <div className="flex items-center gap-2 font-semibold text-lg">
                    <AlertTriangle className="h-5 w-5" /> No Account Onboarded
                </div>
                <p className="text-xs text-amber-200/80">
                    No active bank account was found for your authenticated profile. Please onboard an account first.
                </p>
                <Link to="/onboard">
                    <Button size="sm" className="bg-amber-500 text-black hover:bg-amber-400 text-xs font-semibold">
                        Onboard Account Now
                    </Button>
                </Link>
            </Card>
        );
    }

    const getStatusBadge = () => {
        switch (account.accountStatus) {
            case 'ACTIVE':
                return (
                    <Badge variant="success" className="gap-1.5 py-0.5">
                        <UserCheck className="h-3.5 w-3.5" /> ACTIVE
                    </Badge>
                );
            case 'PENDING_INITIAL_DEPOSIT':
                return (
                    <Badge variant="warning" className="gap-1.5 py-0.5">
                        <AlertTriangle className="h-3.5 w-3.5" /> PENDING DEPOSIT
                    </Badge>
                );
            case 'BLOCKED':
                return (
                    <Badge variant="destructive" className="gap-1.5 py-0.5">
                        <Lock className="h-3.5 w-3.5" /> ACCOUNT BLOCKED
                    </Badge>
                );
            default:
                return null;
        }
    };

    return (
        <Card className="relative overflow-hidden border-primary/20 bg-gradient-to-br from-card via-card/90 to-primary/5 shadow-xl">
            <CardHeader className="pb-2">
                <div className="flex items-start justify-between gap-4">
                    <div>
                        <span className="text-xs font-semibold text-muted-foreground uppercase tracking-wider block mb-1">
                            {account.accountType} ACCOUNT
                        </span>
                        <h2 className="text-xl font-bold tracking-tight text-foreground">
                            {account.accountHolderName}
                        </h2>
                        <p className="text-xs font-mono text-muted-foreground mt-0.5">
                            Acc No: <span className="text-foreground font-semibold">{account.accountNumber}</span>
                        </p>
                    </div>
                    <div className="flex flex-col items-end gap-2">
                        {getStatusBadge()}
                        <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => refetch()}
                            className="h-7 text-xs text-muted-foreground hover:text-foreground gap-1 px-2"
                        >
                            <RefreshCw className="h-3 w-3" /> Sync
                        </Button>
                    </div>
                </div>
            </CardHeader>

            <CardContent>
                <div className="my-2 p-4 rounded-lg bg-background/60 border border-border/50 backdrop-blur-sm">
                    <span className="text-xs text-muted-foreground font-medium block mb-1">
                        Available Account Balance
                    </span>
                    <div className="text-3xl sm:text-4xl font-extrabold tracking-tight text-primary">
                        {formatCurrency(account.accountBalance)}
                    </div>
                </div>

                {account.accountStatus === 'PENDING_INITIAL_DEPOSIT' && (
                    <div className="mt-4 pt-4 border-t border-border flex items-center justify-between gap-3">
                        <span className="text-xs text-amber-300">Initial deposit required to activate transactions.</span>
                        <Link to="/deposit">
                            <Button size="sm" className="bg-amber-500 text-black hover:bg-amber-400 text-xs font-semibold">
                                Pay Initial Deposit
                            </Button>
                        </Link>
                    </div>
                )}
            </CardContent>
        </Card>
    );
};
