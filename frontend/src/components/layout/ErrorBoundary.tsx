import React from 'react';
import { useRouteError } from 'react-router';
import { Button } from '../ui/button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '../ui/card';
import { AlertTriangle, Home, RefreshCw } from 'lucide-react';

export const ErrorBoundary: React.FC = () => {
    const error = useRouteError() as any;
    console.error("ErrorBoundary caught an unexpected error:", error);

    const errorMessage = error instanceof Error 
        ? error.message 
        : (typeof error === 'string' 
            ? error 
            : (error?.message || error?.statusText || JSON.stringify(error)));

    return (
        <div className="min-h-screen bg-background text-foreground flex items-center justify-center p-4 font-sans antialiased w-full">
            <Card className="max-w-md w-full border-destructive/20 shadow-2xl bg-card/50 backdrop-blur-md">
                <CardHeader className="text-center pb-2">
                    <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-destructive/10 text-destructive mb-4 border border-destructive/20">
                        <AlertTriangle className="h-6 w-6" />
                    </div>
                    <CardTitle className="text-xl font-bold tracking-tight">Unexpected System Failure</CardTitle>
                    <CardDescription className="text-xs">
                        An error occurred while rendering this interface component.
                    </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4 py-4">
                    <div className="rounded-lg bg-muted/50 p-4 border border-border/50">
                        <p className="text-xs font-mono break-all text-destructive font-semibold">
                            {errorMessage || "Unknown error details"}
                        </p>
                    </div>
                </CardContent>
                <CardFooter className="flex flex-col sm:flex-row gap-3 pt-2">
                    <Button 
                        className="w-full text-xs h-9" 
                        onClick={() => window.location.href = '/'}
                    >
                        <Home className="mr-2 h-4 w-4" />
                        Go to Dashboard
                    </Button>
                    <Button 
                        variant="outline" 
                        className="w-full text-xs h-9" 
                        onClick={() => window.location.reload()}
                    >
                        <RefreshCw className="mr-2 h-4 w-4" />
                        Reload Page
                    </Button>
                </CardFooter>
            </Card>
        </div>
    );
};
