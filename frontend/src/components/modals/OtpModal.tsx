import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { otpSchema, type OtpSchemaType } from '../../lib/schemas';
import { useSagaStore } from '../../store/useSagaStore';
import { useVerifyOtp } from '../../hooks/useTransactions';
import { Dialog, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '../ui/dialog';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Button } from '../ui/button';
import { ShieldAlert, KeyRound, Loader2 } from 'lucide-react';

export const OtpModal: React.FC = () => {
    const { isOtpModalOpen, setIsOtpModalOpen, activeSagaRef } = useSagaStore();
    const [serverError, setServerError] = useState('');
    const verifyOtpMutation = useVerifyOtp();

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors }
    } = useForm<OtpSchemaType>({
        resolver: zodResolver(otpSchema),
        defaultValues: {
            otp: ''
        }
    });

    if (!isOtpModalOpen || !activeSagaRef) return null;

    const onSubmit = (data: OtpSchemaType) => {
        setServerError('');
        verifyOtpMutation.mutate(
            { refId: activeSagaRef, otp: data.otp.trim() },
            {
                onSuccess: () => {
                    reset();
                },
                onError: (err: any) => {
                    const serverMsg = err.response?.data?.message || err.message || 'OTP verification failed';
                    setServerError(serverMsg);
                }
            }
        );
    };

    return (
        <Dialog open={isOtpModalOpen} onOpenChange={setIsOtpModalOpen}>
            <DialogHeader className="mb-4">
                <div className="flex items-center gap-3">
                    <div className="flex h-10 w-10 items-center justify-center rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/30">
                        <ShieldAlert className="h-5 w-5" />
                    </div>
                    <div>
                        <DialogTitle className="text-base font-bold">Verification Required</DialogTitle>
                        <DialogDescription className="text-xs">High amount transfer flagged by Fraud Engine</DialogDescription>
                    </div>
                </div>
            </DialogHeader>

            <p className="text-xs text-muted-foreground mb-4">
                A 6-digit OTP code has been generated for transaction reference{' '}
                <span className="font-mono text-foreground font-semibold">{activeSagaRef}</span>. Check notification service logs.
            </p>

            {serverError && (
                <div className="mb-4 p-3 rounded-md bg-destructive/10 border border-destructive/20 text-xs text-destructive">
                    {serverError}
                </div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <div className="space-y-1.5">
                    <Label htmlFor="otp" className="text-xs font-semibold text-muted-foreground">
                        6-Digit Verification OTP
                    </Label>
                    <div className="relative">
                        <KeyRound className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
                        <Input
                            id="otp"
                            type="text"
                            maxLength={6}
                            placeholder="123456"
                            className="pl-9 text-center text-lg font-mono tracking-widest"
                            {...register('otp')}
                        />
                    </div>
                    {errors.otp && (
                        <p className="text-[11px] font-medium text-destructive mt-1">
                            {errors.otp.message}
                        </p>
                    )}
                </div>

                <DialogFooter className="mt-6 gap-2">
                    <Button
                        type="button"
                        variant="outline"
                        onClick={() => setIsOtpModalOpen(false)}
                        className="text-xs font-semibold"
                    >
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        disabled={verifyOtpMutation.isPending}
                        className="text-xs font-semibold bg-amber-500 text-black hover:bg-amber-400"
                    >
                        {verifyOtpMutation.isPending ? (
                            <>
                                <Loader2 className="h-4 w-4 animate-spin mr-1" />
                                Verifying...
                            </>
                        ) : (
                            'Verify & Complete'
                        )}
                    </Button>
                </DialogFooter>
            </form>
        </Dialog>
    );
};
