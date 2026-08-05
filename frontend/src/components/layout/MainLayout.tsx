import React from 'react';
import { Outlet } from 'react-router';
import { Header } from './Header';
import { AppSidebar } from './AppSidebar';
import { SidebarProvider } from '../ui/sidebar';
import { OtpModal } from '../modals/OtpModal';

export const MainLayout: React.FC = () => {
    return (
        <SidebarProvider defaultOpen={false}>
            <div className="min-h-screen bg-background text-foreground flex flex-col font-sans antialiased w-full">
                <Header />
                <div className="flex flex-1 w-full relative">
                    <AppSidebar />
                    <main className="flex-1 container mx-auto px-4 py-8 max-w-7xl">
                        <Outlet />
                    </main>
                </div>
                <footer className="border-t border-border/40 py-6 text-center text-xs text-muted-foreground w-full">
                    <div className="container mx-auto">
                        Avalon Banking Platform &copy; 2026. Built with Spring Boot WebFlux, Kafka & React 19.
                    </div>
                </footer>
                {/* Global OTP Verification Modal */}
                <OtpModal />
            </div>
        </SidebarProvider>
    );
};
