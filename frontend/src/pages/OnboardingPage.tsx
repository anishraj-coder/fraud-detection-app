import React, { useState } from 'react';
import { useNavigate } from 'react-router';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { onboardingSchema, type OnboardingSchemaType } from '../lib/schemas';
import { useOnboardAccount } from '../hooks/useAccount';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/card';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Button } from '../components/ui/button';
import { UserPlus, CheckCircle, AlertCircle, Loader2 } from 'lucide-react';

export const OnboardingPage: React.FC = () => {
    const [serverError, setServerError] = useState('');
    const navigate = useNavigate();
    const onboardMutation = useOnboardAccount();

    const {
        register,
        handleSubmit,
        formState: { errors }
    } = useForm<OnboardingSchemaType>({
        resolver: zodResolver(onboardingSchema),
        defaultValues: {
            accountHolderName: '',
            accountType: 'SAVINGS',
            phone: '',
            initialDeposit: 1000
        }
    });

    const onSubmit = (data: OnboardingSchemaType) => {
        setServerError('');
        onboardMutation.mutate(
            {
                accountHolderName: data.accountHolderName.trim(),
                accountType: data.accountType,
                phone: data.phone.trim(),
                initialDeposit: data.initialDeposit
            },
            {
                onSuccess: () => {
                    navigate('/deposit');
                },
                onError: (err: any) => {
                    const serverMsg = err.response?.data?.message || err.message || 'Onboarding failed';
                    setServerError(serverMsg);
                }
            }
        );
    };

    return (
        <div className="max-w-xl mx-auto py-6 space-y-6">
            <Card className="border-primary/20 shadow-xl">
                <CardHeader className="pb-4 border-b border-border">
                    <div className="flex items-center gap-3">
                        <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary border border-primary/20">
                            <UserPlus className="h-5 w-5" />
                        </div>
                        <div>
                            <CardTitle className="text-xl font-bold">Customer Account Onboarding</CardTitle>
                            <CardDescription className="text-xs">Register your new bank account in ACCOUNT-SERVICE</CardDescription>
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

                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                        <div className="space-y-1.5">
                            <Label htmlFor="accountHolderName" className="text-xs font-semibold text-muted-foreground">
                                Full Legal Name
                            </Label>
                            <Input
                                id="accountHolderName"
                                type="text"
                                placeholder="John Doe"
                                className="text-xs"
                                {...register('accountHolderName')}
                            />
                            {errors.accountHolderName && (
                                <p className="text-[11px] font-medium text-destructive mt-1">
                                    {errors.accountHolderName.message}
                                </p>
                            )}
                        </div>

                        <div className="space-y-1.5">
                            <Label htmlFor="accountType" className="text-xs font-semibold text-muted-foreground">
                                Account Type
                            </Label>
                            <select
                                id="accountType"
                                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-xs ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                                {...register('accountType')}
                            >
                                <option value="SAVINGS">SAVINGS ACCOUNT</option>
                                <option value="CURRENT">CURRENT ACCOUNT</option>
                                <option value="CHECKING">CHECKING ACCOUNT</option>
                                <option value="BUSINESS">BUSINESS ACCOUNT</option>
                            </select>
                            {errors.accountType && (
                                <p className="text-[11px] font-medium text-destructive mt-1">
                                    {errors.accountType.message}
                                </p>
                            )}
                        </div>

                        <div className="space-y-1.5">
                            <Label htmlFor="phone" className="text-xs font-semibold text-muted-foreground">
                                Phone Number
                            </Label>
                            <Input
                                id="phone"
                                type="tel"
                                placeholder="+91 9876543210"
                                className="text-xs font-mono"
                                {...register('phone')}
                            />
                            {errors.phone && (
                                <p className="text-[11px] font-medium text-destructive mt-1">
                                    {errors.phone.message}
                                </p>
                            )}
                        </div>

                        <div className="space-y-1.5">
                            <Label htmlFor="initialDeposit" className="text-xs font-semibold text-muted-foreground">
                                Initial Deposit Amount (₹ INR)
                            </Label>
                            <Input
                                id="initialDeposit"
                                type="number"
                                min="100"
                                step="100"
                                className="text-xs font-mono"
                                {...register('initialDeposit', { valueAsNumber: true })}
                            />
                            {errors.initialDeposit && (
                                <p className="text-[11px] font-medium text-destructive mt-1">
                                    {errors.initialDeposit.message}
                                </p>
                            )}
                        </div>

                        <Button
                            type="submit"
                            disabled={onboardMutation.isPending}
                            className="w-full h-10 text-xs font-semibold flex items-center justify-center gap-2 mt-6"
                        >
                            {onboardMutation.isPending ? (
                                <>
                                    <Loader2 className="h-4 w-4 animate-spin" />
                                    Processing Onboarding...
                                </>
                            ) : (
                                <>
                                    <CheckCircle className="h-4 w-4" />
                                    Complete Onboarding
                                </>
                            )}
                        </Button>
                    </form>
                </CardContent>
            </Card>
        </div>
    );
};
