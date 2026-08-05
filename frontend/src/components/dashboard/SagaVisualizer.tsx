import React from 'react';
import { useSagaStore } from '../../store/useSagaStore';
import { useSagaStatus } from '../../hooks/useTransactions';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../ui/card';
import { Badge } from '../ui/badge';
import { Button } from '../ui/button';
import { CheckCircle2, Clock, ShieldAlert, XCircle, Loader2 } from 'lucide-react';

export const SagaVisualizer: React.FC = () => {
    const { activeSagaRef, clearSaga } = useSagaStore();
    const { data: tx, isLoading } = useSagaStatus(activeSagaRef);

    if (!activeSagaRef) {
        return (
            <Card className="border-dashed border-border/60 bg-card/30 p-6 text-center text-muted-foreground flex flex-col items-center justify-center gap-2">
                <Clock className="h-8 w-8 text-muted-foreground/50" />
                <span className="text-xs font-medium">No Active SAGA Transaction Pipeline</span>
                <span className="text-[11px] text-muted-foreground/70">
                    Initiate a money transfer above to view real-time distributed transaction status.
                </span>
            </Card>
        );
    }

    const status = tx?.status || 'PENDING';

    const getStepStatus = (stepIndex: number) => {
        if (status === 'COMPLETED') return 'completed';
        if (status === 'FAILED' || status === 'FAILED_REFUNDED') {
            if (stepIndex === 3) return 'failed';
        }

        switch (stepIndex) {
            case 0:
                return status !== 'PENDING' ? 'completed' : 'active';
            case 1:
                if (status === 'DEBITED' || status === 'PENDING_VERIFICATION') return 'active';
                return 'pending';
            case 2:
                if (status === 'PENDING_VERIFICATION') return 'warning';
                return 'pending';
            case 3:
                return 'pending';
            default:
                return 'pending';
        }
    };

    const steps = [
        { label: 'Sender Debit', desc: 'ACCOUNT-SERVICE' },
        { label: 'Fraud Engine', desc: 'FRAUD-DETECTION' },
        { label: 'OTP Verification', desc: 'NOTIFICATION' },
        { label: 'Settlement', desc: 'TRANSACTION-SERVICE' }
    ];

    return (
        <Card className="border-primary/30 shadow-lg">
            <CardHeader className="flex flex-row items-center justify-between pb-3 border-b border-border">
                <div>
                    <CardTitle className="text-xs font-bold text-primary uppercase tracking-wider">
                        Real-time SAGA Orchestrator
                    </CardTitle>
                    <CardDescription className="text-xs font-mono">
                        Ref: <span className="text-foreground font-semibold">{activeSagaRef}</span>
                    </CardDescription>
                </div>
                <Button variant="ghost" size="sm" onClick={clearSaga} className="text-xs text-muted-foreground">
                    Dismiss
                </Button>
            </CardHeader>

            <CardContent className="pt-4">
                {isLoading && (
                    <div className="flex items-center gap-2 text-xs text-muted-foreground py-4">
                        <Loader2 className="h-4 w-4 animate-spin text-primary" />
                        Fetching live status from Gateway...
                    </div>
                )}

                <div className="grid grid-cols-1 sm:grid-cols-4 gap-3 my-2">
                    {steps.map((step, idx) => {
                        const stepState = getStepStatus(idx);
                        return (
                            <div
                                key={idx}
                                className={`flex flex-col p-3 rounded-lg border text-xs transition-all ${
                                    stepState === 'completed'
                                        ? 'border-emerald-500/40 bg-emerald-500/10 text-emerald-300'
                                        : stepState === 'warning'
                                        ? 'border-amber-500/40 bg-amber-500/10 text-amber-300 animate-pulse'
                                        : stepState === 'active'
                                        ? 'border-primary/40 bg-primary/10 text-primary'
                                        : stepState === 'failed'
                                        ? 'border-destructive/40 bg-destructive/10 text-destructive'
                                        : 'border-border/40 bg-muted/20 text-muted-foreground'
                                }`}
                            >
                                <div className="flex items-center justify-between mb-1">
                                    <span className="font-bold text-[11px] uppercase tracking-wider">Step {idx + 1}</span>
                                    {stepState === 'completed' && <CheckCircle2 className="h-4 w-4 text-emerald-400" />}
                                    {stepState === 'warning' && <ShieldAlert className="h-4 w-4 text-amber-400 animate-bounce" />}
                                    {stepState === 'active' && <Loader2 className="h-4 w-4 animate-spin text-primary" />}
                                    {stepState === 'failed' && <XCircle className="h-4 w-4 text-destructive" />}
                                </div>
                                <span className="font-semibold text-foreground text-xs leading-tight">{step.label}</span>
                                <span className="text-[10px] opacity-70 font-mono mt-0.5">{step.desc}</span>
                            </div>
                        );
                    })}
                </div>

                <div className="mt-4 pt-3 border-t border-border flex items-center justify-between text-xs">
                    <span className="text-muted-foreground font-mono">
                        Current Status: <strong className="text-foreground uppercase">{status}</strong>
                    </span>
                    {status === 'COMPLETED' && (
                        <Badge variant="success" className="gap-1">
                            <CheckCircle2 className="h-3.5 w-3.5" /> Settlement Finalized
                        </Badge>
                    )}
                    {status === 'FAILED_REFUNDED' && (
                        <Badge variant="destructive" className="gap-1">
                            <ShieldAlert className="h-3.5 w-3.5" /> Transfer Failed & Refunded
                        </Badge>
                    )}
                </div>
            </CardContent>
        </Card>
    );
};
