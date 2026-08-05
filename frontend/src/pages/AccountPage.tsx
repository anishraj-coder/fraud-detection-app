import React from 'react';
import { useAuthStore } from '../store/useAuthStore';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/card';
import { Badge } from '../components/ui/badge';
import { Separator } from '../components/ui/separator';
import { User, ShieldCheck, Mail, Key, AlertCircle } from 'lucide-react';

function parseJwt(token: string) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(window.atob(base64));
    } catch {
        return null;
    }
}

export const AccountPage: React.FC = () => {
    const { jwt, account } = useAuthStore();
    const tokenPayload = jwt ? parseJwt(jwt) : null;

    return (
        <div className="container mx-auto py-8 max-w-4xl space-y-8">
            <div className="space-y-2">
                <h1 className="text-3xl font-extrabold tracking-tight text-foreground">Account Profile</h1>
                <p className="text-sm text-muted-foreground">
                    Manage your personal profile, bank account status, and active security sessions.
                </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* 1. Casdoor Profile Card */}
                <Card className="border-primary/20 shadow-md">
                    <CardHeader className="flex flex-row items-center gap-4 pb-4">
                        <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary/10 text-primary border border-primary/20">
                            <User className="h-6 w-6 text-primary" />
                        </div>
                        <div>
                            <CardTitle className="text-lg font-bold">User Information</CardTitle>
                            <CardDescription className="text-xs">Active authentication profile</CardDescription>
                        </div>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        <div className="flex justify-between items-center text-xs">
                            <span className="text-muted-foreground font-medium">Username</span>
                            <span className="font-semibold text-foreground">{tokenPayload?.name || 'N/A'}</span>
                        </div>
                        <Separator />
                        <div className="flex justify-between items-center text-xs">
                            <span className="text-muted-foreground font-medium">Email Address</span>
                            <span className="font-semibold text-foreground flex items-center gap-1">
                                <Mail className="h-3 w-3 text-muted-foreground" />
                                {tokenPayload?.email || 'N/A'}
                            </span>
                        </div>
                        <Separator />
                        <div className="flex justify-between items-center text-xs">
                            <span className="text-muted-foreground font-medium">User Type</span>
                            <Badge variant="outline" className="text-[10px] uppercase font-bold tracking-wider">
                                {tokenPayload?.type || 'normal-user'}
                            </Badge>
                        </div>
                        <Separator />
                        <div className="flex justify-between items-center text-xs">
                            <span className="text-muted-foreground font-medium">Application</span>
                            <span className="font-mono text-[10px] text-primary bg-primary/10 px-2 py-0.5 rounded">
                                {tokenPayload?.signupApplication || 'N/A'}
                            </span>
                        </div>
                    </CardContent>
                </Card>

                {/* 2. Bank Account Details Card */}
                <Card className="border-primary/20 shadow-md">
                    <CardHeader className="flex flex-row items-center gap-4 pb-4">
                        <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                            <ShieldCheck className="h-6 w-6 text-emerald-400" />
                        </div>
                        <div>
                            <CardTitle className="text-lg font-bold">Bank Account Details</CardTitle>
                            <CardDescription className="text-xs">Financial status information</CardDescription>
                        </div>
                    </CardHeader>
                    <CardContent className="space-y-4">
                        {account ? (
                            <>
                                <div className="flex justify-between items-center text-xs">
                                    <span className="text-muted-foreground font-medium">Account Number</span>
                                    <span className="font-mono font-bold text-foreground tracking-wide">{account.accountNumber}</span>
                                </div>
                                <Separator />
                                <div className="flex justify-between items-center text-xs">
                                    <span className="text-muted-foreground font-medium">Account Status</span>
                                    <Badge 
                                        className={`text-[10px] uppercase font-bold ${
                                            account.accountStatus === 'ACTIVE' 
                                                ? 'bg-emerald-500/20 text-emerald-400 hover:bg-emerald-500/20 border-emerald-500/30' 
                                                : 'bg-amber-500/20 text-amber-400 hover:bg-amber-500/20 border-amber-500/30'
                                        }`}
                                    >
                                        {account.accountStatus}
                                    </Badge>
                                </div>
                                <Separator />
                                <div className="flex justify-between items-center text-xs">
                                    <span className="text-muted-foreground font-medium">Account Balance</span>
                                    <span className="text-base font-extrabold text-foreground">
                                        ₹ {account.accountBalance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                                    </span>
                                </div>
                            </>
                        ) : (
                            <div className="flex flex-col items-center justify-center py-6 text-center space-y-3">
                                <AlertCircle className="h-8 w-8 text-amber-400" />
                                <div className="space-y-1">
                                    <h4 className="text-xs font-bold text-foreground">No Account Onboarded</h4>
                                    <p className="text-[11px] text-muted-foreground">
                                        You have not opened or onboarded a digital bank account yet.
                                    </p>
                                </div>
                            </div>
                        )}
                    </CardContent>
                </Card>
            </div>

            {/* 3. Session Security Card */}
            <Card className="border-primary/20 shadow-md">
                <CardHeader className="flex flex-row items-center gap-4 pb-4">
                    <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-sky-500/10 text-sky-400 border border-sky-500/20">
                        <Key className="h-6 w-6 text-sky-400" />
                    </div>
                    <div>
                        <CardTitle className="text-lg font-bold">Security Session Information</CardTitle>
                        <CardDescription className="text-xs">Decoded JWT claims & token parameters</CardDescription>
                    </div>
                </CardHeader>
                <CardContent className="space-y-4">
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
                        <div className="space-y-1">
                            <span className="text-muted-foreground font-medium">Token Issuer</span>
                            <p className="font-mono text-[10px] text-foreground bg-muted p-2 rounded truncate">
                                {tokenPayload?.iss || 'N/A'}
                            </p>
                        </div>
                        <div className="space-y-1">
                            <span className="text-muted-foreground font-medium">Token Audience</span>
                            <p className="font-mono text-[10px] text-foreground bg-muted p-2 rounded truncate">
                                {tokenPayload?.aud ? tokenPayload.aud[0] : 'N/A'}
                            </p>
                        </div>
                        <div className="space-y-1">
                            <span className="text-muted-foreground font-medium">Session ID (jti)</span>
                            <p className="font-mono text-[10px] text-foreground bg-muted p-2 rounded truncate">
                                {tokenPayload?.jti || 'N/A'}
                            </p>
                        </div>
                        <div className="space-y-1">
                            <span className="text-muted-foreground font-medium">Token Scope</span>
                            <p className="font-mono text-[10px] text-foreground bg-muted p-2 rounded truncate">
                                {tokenPayload?.scope || 'openid'}
                            </p>
                        </div>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
};
