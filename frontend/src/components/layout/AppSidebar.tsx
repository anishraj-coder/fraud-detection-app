import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router';
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
import {
    Sidebar,
    SidebarContent,
    SidebarFooter,
    SidebarGroup,
    SidebarGroupContent,
    SidebarGroupLabel,
    SidebarHeader,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
    useSidebar
} from '../ui/sidebar';
import { ShieldCheck, LayoutDashboard, User, CreditCard, History, LogOut } from 'lucide-react';

export const AppSidebar: React.FC = () => {
    const { account, logout, jwt } = useAuthStore();
    const navigate = useNavigate();
    const location = useLocation();
    const { setOpenMobile } = useSidebar();

    const handleLogout = () => {
        setOpenMobile(false);
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

    const navItems = [
        { title: 'Dashboard', url: '/', icon: LayoutDashboard },
        { title: 'Onboard Account', url: '/onboard', icon: User },
        { title: 'Deposit', url: '/deposit', icon: CreditCard },
        { title: 'Ledger', url: '/history', icon: History },
        { title: 'Account', url: '/account', icon: User }
    ];

    return (
        <Sidebar side="left" className="md:hidden">
            {/* Header */}
            <SidebarHeader className="border-b border-border/40 p-4">
                <div className="flex items-center gap-2.5 font-bold tracking-tight text-foreground">
                    <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary/10 text-primary border border-primary/20">
                        <ShieldCheck className="h-5 w-5 text-primary" />
                    </div>
                    <div className="flex flex-col">
                        <span className="text-sm font-black tracking-wider uppercase bg-gradient-to-r from-primary to-emerald-400 bg-clip-text text-transparent">
                            Avalon Bank
                        </span>
                        <span className="text-[9px] font-medium text-muted-foreground uppercase tracking-widest -mt-1">
                            Digital Banking
                        </span>
                    </div>
                </div>
            </SidebarHeader>

            {/* Content */}
            <SidebarContent>
                <SidebarGroup>
                    <SidebarGroupLabel className="px-4 text-[10px] font-bold uppercase tracking-wider text-muted-foreground">
                        Navigation
                    </SidebarGroupLabel>
                    <SidebarGroupContent className="px-2 mt-2">
                        <SidebarMenu>
                            {navItems.map((item) => {
                                const Icon = item.icon;
                                const isActive = location.pathname === item.url;
                                return (
                                    <SidebarMenuItem key={item.title}>
                                        <SidebarMenuButton
                                            isActive={isActive}
                                            onClick={() => setOpenMobile(false)}
                                            render={
                                                <Link to={item.url} className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-colors w-full">
                                                    <Icon className="h-4 w-4 shrink-0" />
                                                    <span>{item.title}</span>
                                                </Link>
                                            }
                                        />
                                    </SidebarMenuItem>
                                );
                            })}
                        </SidebarMenu>
                    </SidebarGroupContent>
                </SidebarGroup>
            </SidebarContent>

            {/* Footer */}
            <SidebarFooter className="border-t border-border/40 p-4">
                <div className="flex items-center justify-between gap-3 w-full">
                    <div className="flex flex-col text-left truncate">
                        <span className="text-xs font-semibold text-foreground truncate">
                            {getSessionName()}
                        </span>
                        <span className="text-[10px] font-mono text-muted-foreground truncate">
                            {account?.accountNumber ? `Acc: ${account.accountNumber}` : 'Active Session'}
                        </span>
                    </div>

                    <Dialog>
                        <DialogTrigger render={
                            <Button
                                variant="destructive"
                                size="icon"
                                className="h-8 w-8 shrink-0"
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
            </SidebarFooter>
        </Sidebar>
    );
};
