import React from 'react';
import { Link } from 'react-router';
import { AlertCircle, ArrowLeft } from 'lucide-react';

export const NotFoundPage: React.FC = () => {
    return (
        <div className="flex flex-col items-center justify-center py-16 text-center space-y-4">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-destructive/10 text-destructive border border-destructive/20 mb-2">
                <AlertCircle className="h-8 w-8" />
            </div>
            <h1 className="text-3xl font-extrabold text-foreground tracking-tight">404 - Page Not Found</h1>
            <p className="text-xs text-muted-foreground max-w-sm">
                The requested URL route does not exist on the Avalon Banking platform.
            </p>
            <Link
                to="/"
                className="inline-flex items-center gap-2 px-4 py-2 text-xs font-semibold rounded-md bg-primary text-primary-foreground hover:bg-primary/90 transition-colors mt-4"
            >
                <ArrowLeft className="h-4 w-4" /> Return to Dashboard
            </Link>
        </div>
    );
};
