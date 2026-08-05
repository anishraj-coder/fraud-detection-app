import React from 'react';
import { Navigate, Outlet } from 'react-router';
import { useAuthStore } from '../../store/useAuthStore';
import { Loader2 } from 'lucide-react';

export const ProtectedRoute: React.FC = () => {
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

    // If OAuth2 authorization code is in the URL, wait for token exchange to complete!
    const hasAuthCode = new URLSearchParams(window.location.search).has('code');

    if (!isAuthenticated) {
        if (hasAuthCode) {
            return (
                <div className="flex flex-col items-center justify-center min-h-[60vh] gap-3 text-muted-foreground text-xs font-medium">
                    <Loader2 className="h-6 w-6 animate-spin text-primary" />
                    <span>Completing Casdoor Single Sign-On...</span>
                </div>
            );
        }
        return <Navigate to="/login" replace />;
    }

    return <Outlet />;
};
