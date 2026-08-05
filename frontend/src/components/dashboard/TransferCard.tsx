import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { transferSchema, type TransferSchemaType } from '../../lib/schemas';
import { useTransferMoney } from '../../hooks/useTransactions';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../ui/card';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Button } from '../ui/button';
import { Send, AlertCircle, Loader2 } from 'lucide-react';

export const TransferCard: React.FC = () => {
    const [serverError, setServerError] = useState('');
    const transferMutation = useTransferMoney();

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors }
    } = useForm<TransferSchemaType>({
        resolver: zodResolver(transferSchema),
        defaultValues: {
            receiverAccountNumber: '',
            amount: undefined,
            description: ''
        }
    });

    const onSubmit = (data: TransferSchemaType) => {
        setServerError('');
        transferMutation.mutate(
            {
                receiverAccountNumber: data.receiverAccountNumber.trim(),
                amount: data.amount,
                description: data.description?.trim() || undefined
            },
            {
                onSuccess: () => {
                    reset();
                },
                onError: (err: any) => {
                    const serverMsg = err.response?.data?.message || err.message || 'Transfer failed';
                    setServerError(serverMsg);
                }
            }
        );
    };

    return (
        <Card className="border-border shadow-lg">
            <CardHeader className="pb-4">
                <div className="flex items-center gap-2">
                    <div className="flex h-8 w-8 items-center justify-center rounded-md bg-primary/10 text-primary border border-primary/20">
                        <Send className="h-4 w-4" />
                    </div>
                    <div>
                        <CardTitle className="text-base font-semibold">Money Transfer</CardTitle>
                        <CardDescription className="text-xs">Initiate SAGA transfer via Gateway</CardDescription>
                    </div>
                </div>
            </CardHeader>

            <CardContent>
                {serverError && (
                    <div className="mb-4 flex items-center gap-2 rounded-md border border-destructive/30 bg-destructive/10 p-3 text-xs text-destructive">
                        <AlertCircle className="h-4 w-4 shrink-0" />
                        <span>{serverError}</span>
                    </div>
                )}

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    <div className="space-y-1.5">
                        <Label htmlFor="receiverAccountNumber" className="text-xs font-semibold text-muted-foreground">
                            Receiver Account Number
                        </Label>
                        <Input
                            id="receiverAccountNumber"
                            type="text"
                            placeholder="e.g. 50100012345672"
                            className="font-mono text-xs"
                            {...register('receiverAccountNumber')}
                        />
                        {errors.receiverAccountNumber && (
                            <p className="text-[11px] font-medium text-destructive mt-1">
                                {errors.receiverAccountNumber.message}
                            </p>
                        )}
                    </div>

                    <div className="space-y-1.5">
                        <Label htmlFor="amount" className="text-xs font-semibold text-muted-foreground">
                            Amount (₹ INR)
                        </Label>
                        <Input
                            id="amount"
                            type="number"
                            step="0.01"
                            placeholder="100.00"
                            className="font-mono text-xs"
                            {...register('amount', { valueAsNumber: true })}
                        />
                        {errors.amount && (
                            <p className="text-[11px] font-medium text-destructive mt-1">
                                {errors.amount.message}
                            </p>
                        )}
                    </div>

                    <div className="space-y-1.5">
                        <Label htmlFor="description" className="text-xs font-semibold text-muted-foreground">
                            Description / Remark (Optional)
                        </Label>
                        <Input
                            id="description"
                            type="text"
                            placeholder="e.g. Splitting lunch bill"
                            className="text-xs"
                            {...register('description')}
                        />
                    </div>

                    <Button
                        type="submit"
                        disabled={transferMutation.isPending}
                        className="w-full h-10 text-xs font-semibold flex items-center justify-center gap-2 mt-6"
                    >
                        {transferMutation.isPending ? (
                            <>
                                <Loader2 className="h-4 w-4 animate-spin" />
                                Dispatching SAGA Transfer...
                            </>
                        ) : (
                            <>
                                <Send className="h-4 w-4" />
                                Send Funds Now
                            </>
                        )}
                    </Button>
                </form>
            </CardContent>
        </Card>
    );
};
