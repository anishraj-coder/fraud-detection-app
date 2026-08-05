import React, { useEffect } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { RouterProvider } from 'react-router';
import { router } from './router';
import { exchangeCasdoorCodeForToken } from './lib/casdoor';
import { useAuthStore } from './store/useAuthStore';
import { useTokenRefreshWatcher } from './hooks/useTokenRefresh';

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            refetchOnWindowFocus: false,
            retry: 1
        }
    }
});

export const AppContent: React.FC = () => {
    // Mount the background token refresh watcher
    useTokenRefreshWatcher();

    return <RouterProvider router={router} />;
};

export const App: React.FC = () => {
    const setTokens = useAuthStore((state) => state.setTokens);

    useEffect(() => {
        // Detect OAuth2 Authorization Code returned from Casdoor / Google
        const urlParams = new URLSearchParams(window.location.search);
        const code = urlParams.get('code');

        if (code) {
            exchangeCasdoorCodeForToken(code)
                .then(({ accessToken, refreshToken }) => {
                    setTokens(accessToken, refreshToken);
                    // Clear ?code=... from URL bar while remaining on current route
                    window.history.replaceState({}, document.title, window.location.pathname);
                })
                .catch((err) => {
                    console.error('Failed to exchange Casdoor OAuth code:', err);
                    window.location.href = '/login';
                });
        }
    }, [setTokens]);

    return (
        <QueryClientProvider client={queryClient}>
            <AppContent />
        </QueryClientProvider>
    );
};

export default App;
