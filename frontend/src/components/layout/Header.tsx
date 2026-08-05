import React from 'react';
import { Link, useNavigate } from 'react-router';
import { useAuthStore } from '../../store/useAuthStore';
import { Button } from '../ui/button';
import {
    Dialog,
    DialogTrigger,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogDescription,
    DialogFooter,
    DialogClose
} from '../ui/dialog';
import { SidebarTrigger } from '../ui/sidebar';
import { ShieldCheck, LogOut, User, CreditCard, History, LayoutDashboard, UserPlus } from 'lucide-react';

export const Header: React.FC = () => {
    const { account, logout, isAuthenticated, jwt } = useAuthStore();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const getSessionName = () => {
        if (!jwt) return 'Customer Account';
        try {
            const base64Url = jwt.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const payload = JSON.parse(window.atob(base64));
            return payload.displayName || payload.name || 'Customer Account';
        } catch {
            return 'Customer Account';
        }
    };

    return (
        <header className="sticky top-0 z-40 w-full border-b border-border/40 bg-background/80 backdrop-blur-md">
            <div className="container mx-auto flex h-16 items-center justify-between px-4 sm:px-6 max-w-7xl">
                {/* Mobile Sidebar Toggle Button */}
                {isAuthenticated && (
                    <SidebarTrigger className="md:hidden text-foreground mr-2 shrink-0" />
                )}

                {/* Brand Logo */}
                <Link to="/" className="flex items-center gap-2.5 font-bold tracking-tight text-foreground transition-opacity hover:opacity-90 mr-auto md:mr-0">
                    <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary/10 text-primary border border-primary/20">
                        <ShieldCheck className="h-5 w-5 text-primary" />
                    </div>
                    <div className="flex flex-col">
                        <span className="text-lg font-black tracking-wider uppercase bg-gradient-to-r from-primary to-emerald-400 bg-clip-text text-transparent">
                            Avalon Bank
                        </span>
                        <span className="text-[10px] font-medium text-muted-foreground uppercase tracking-widest -mt-1">
                            Digital Banking
                        </span>
                    </div>
                </Link>

                {/* Navigation Links */}
                {isAuthenticated && (
                    <nav className="hidden md:flex items-center gap-1">
                        <Link
                            to="/"
                            className="flex items-center gap-2 px-3 py-2 text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-muted/50 rounded-md transition-colors"
                        >
                            <LayoutDashboard className="h-4 w-4" />
                            Dashboard
                        </Link>
                        <Link
                            to="/onboard"
                            className="flex items-center gap-2 px-3 py-2 text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-muted/50 rounded-md transition-colors"
                        >
                            <User className="h-4 w-4" />
                            Onboard Account
                        </Link>
                        <Link
                            to="/deposit"
                            className="flex items-center gap-2 px-3 py-2 text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-muted/50 rounded-md transition-colors"
                        >
                            <CreditCard className="h-4 w-4" />
                            Deposit
                        </Link>
                        <Link
                            to="/history"
                            className="flex items-center gap-2 px-3 py-2 text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-muted/50 rounded-md transition-colors"
                        >
                            <History className="h-4 w-4" />
                            Ledger
                        </Link>
                        <Link
                            to="/account"
                            className="flex items-center gap-2 px-3 py-2 text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-muted/50 rounded-md transition-colors"
                        >
                            <User className="h-4 w-4" />
                            Account
                        </Link>
                    </nav>
                )}

                {/* Auth Actions */}
                <div className="flex items-center gap-3">
                    {isAuthenticated ? (
                        <div className="flex items-center gap-3">
                            <div className="hidden lg:flex flex-col text-right">
                                <span className="text-xs font-semibold leading-tight text-foreground">
                                    {getSessionName()}
                                </span>
                                <span className="text-[10px] font-mono text-muted-foreground">
                                    {account?.accountNumber ? `Acc: ${account.accountNumber}` : 'Active Session'}
                                </span>
                            </div>

                            {/* Shadcn Confirm Logout Dialog */}
                            <Dialog>
                                <DialogTrigger render={
                                    <Button
                                        variant="destructive"
                                        size="icon"
                                        className="h-9 w-9"
                                        title="Sign Out"
                                    >
                                        <LogOut className="h-4 w-4" />
                                    </Button>
                                } />
                                <DialogContent>
                                    <DialogHeader>
                                        <DialogTitle>Confirm Sign Out</DialogTitle>
                                        <DialogDescription>
                                            Are you sure you want to log out of your Avalon Bank account session?
                                        </DialogDescription>
                                    </DialogHeader>
                                    <DialogFooter>
                                        <DialogClose render={<Button variant="outline">Cancel</Button>} />
                                        <Button variant="destructive" onClick={handleLogout}>
                                            Sign Out
                                        </Button>
                                    </DialogFooter>
                                </DialogContent>
                            </Dialog>
                        </div>
                    ) : (
                        <div className="flex items-center gap-2">
                            <Link to="/login">
                                <Button variant="ghost" size="sm" className="text-xs font-semibold">
                                    Sign In
                                </Button>
                            </Link>
                            <Link to="/login?tab=signup">
                                <Button size="sm" className="text-xs font-semibold gap-1.5">
                                    <UserPlus className="h-3.5 w-3.5" />
                                    Open Account
                                </Button>
                            </Link>
                        </div>
                    )}
                </div>
            </div>
        </header>
    );
};
