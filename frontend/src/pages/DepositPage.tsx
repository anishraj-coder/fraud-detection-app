import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { depositSchema, type DepositSchemaType } from '../lib/schemas';
import { useMyAccount } from '../hooks/useAccount';
import { useCreatePaymentOrder } from '../hooks/usePayment';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/card';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Button } from '../components/ui/button';
import { CreditCard, ShieldCheck, AlertCircle, Loader2 } from 'lucide-react';
import { useNavigate } from 'react-router';

declare global {
    interface Window {
        Razorpay: any;
    }
}

export const DepositPage: React.FC = () => {
    const { data: account, isLoading } = useMyAccount();
    const [serverError, setServerError] = useState('');

    const navigate = useNavigate();
    const createOrderMutation = useCreatePaymentOrder();

    const {
        register,
        handleSubmit,
        formState: { errors }
    } = useForm<DepositSchemaType>({
        resolver: zodResolver(depositSchema),
        defaultValues: {
            amount: 1000
        }
    });

    const onSubmit = (data: DepositSchemaType) => {
        setServerError('');

        if (!account?.accountNumber) {
            setServerError('No active account found. Please onboard an account first.');
            return;
        }

        createOrderMutation.mutate(
            {
                accountNumber: account.accountNumber,
                amount: data.amount,
                description: 'Initial Account Deposit'
            },
            {
                onSuccess: (orderData) => {
                    if (typeof window.Razorpay === 'undefined') {
                        setServerError('Razorpay SDK failed to load. Ensure Razorpay script is included.');
                        return;
                    }

                    const options = {
                        key: orderData.razorpayKeyId,
                        amount: orderData.amount * 100, // in paise
                        currency: orderData.currency || 'INR',
                        name: 'Avalon Banking Platform',
                        description: 'Account Activation Deposit',
                        order_id: orderData.razorpayOrderId,
                        handler: function (response: any) {
                            alert(`Payment Successful! Razorpay Payment ID: ${response.razorpay_payment_id}`);
                            navigate('/');
                        },
                        prefill: {
                            name: account.accountHolderName,
                            email: account.email || 'customer@avalon.com',
                            contact: account.phone || '9876543210'
                        },
                        theme: {
                            color: '#10b981'
                        }
                    };

                    const rzp = new window.Razorpay(options);
                    rzp.open();
                },
                onError: (err: any) => {
                    const serverMsg = err.response?.data?.message || err.message || 'Payment order creation failed';
                    setServerError(serverMsg);
                }
            }
        );
    };

    if (isLoading) {
        return (
            <div className="flex items-center justify-center py-12 text-xs text-muted-foreground gap-2">
                <Loader2 className="h-5 w-5 animate-spin text-primary" /> Loading account payment status...
            </div>
        );
    }

    return (
        <div className="max-w-xl mx-auto py-6 space-y-6">
            <Card className="border-primary/20 shadow-xl">
                <CardHeader className="pb-4 border-b border-border">
                    <div className="flex items-center gap-3">
                        <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary border border-primary/20">
                            <CreditCard className="h-5 w-5" />
                        </div>
                        <div>
                            <CardTitle className="text-xl font-bold">Account Initial Deposit</CardTitle>
                            <CardDescription className="text-xs">Activate your bank account via Razorpay Payment Gateway</CardDescription>
                        </div>
                    </div>
                </CardHeader>

                <CardContent className="pt-6">
                    {serverError && (
                        <div className="mb-4 flex items-center gap-2 rounded-md border border-destructive/30 bg-destructive/10 p-3 text-xs text-destructive">
                            <AlertCircle className="h-4 w-4 shrink-0" />
                            <span>{serverError}</span>
                        </div>
                    )}

                    <div className="mb-6 p-4 rounded-lg bg-muted/30 border border-border">
                        <div className="text-xs text-muted-foreground">Target Account</div>
                        <div className="text-base font-bold text-foreground font-mono">{account?.accountNumber || 'N/A'}</div>
                        <div className="text-xs text-muted-foreground mt-1">
                            Current Status: <span className="font-semibold text-amber-400">{account?.accountStatus || 'PENDING'}</span>
                        </div>
                    </div>

                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                        <div className="space-y-1.5">
                            <Label htmlFor="amount" className="text-xs font-semibold text-muted-foreground">
                                Deposit Amount (₹ INR)
                            </Label>
                            <Input
                                id="amount"
                                type="number"
                                min="100"
                                step="100"
                                className="text-xs font-mono"
                                {...register('amount', { valueAsNumber: true })}
                            />
                            {errors.amount && (
                                <p className="text-[11px] font-medium text-destructive mt-1">
                                    {errors.amount.message}
                                </p>
                            )}
                        </div>

                        <Button
                            type="submit"
                            disabled={createOrderMutation.isPending}
                            className="w-full h-10 text-xs font-semibold flex items-center justify-center gap-2 bg-emerald-500 text-black hover:bg-emerald-400 mt-6"
                        >
                            {createOrderMutation.isPending ? (
                                <>
                                    <Loader2 className="h-4 w-4 animate-spin" /> Creating Payment Order...
                                </>
                            ) : (
                                <>
                                    <ShieldCheck className="h-4 w-4" /> Pay via Razorpay
                                </>
                            )}
                        </Button>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
};
