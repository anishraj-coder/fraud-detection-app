import { useEffect } from 'react';
import { useAuthStore } from '../store/useAuthStore';
import { refreshCasdoorToken } from '../lib/casdoor';

function parseJwt(token: string) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(window.atob(base64));
    } catch {
        return null;
    }
}

export function useTokenRefreshWatcher() {
    const { jwt, refreshToken, setTokens, logout } = useAuthStore();

    useEffect(() => {
        if (!jwt || !refreshToken) return;

        const payload = parseJwt(jwt);
        if (!payload || !payload.exp) return;

        const expiresAtMs = payload.exp * 1000;
        const nowMs = Date.now();
        
        // Trigger refresh 30 seconds before expiration
        const refreshDelayMs = expiresAtMs - nowMs - 30000;

        if (refreshDelayMs <= 0) {
            console.info('Token is already expired or expiring within 30s. Refreshing immediately...');
            refreshCasdoorToken(refreshToken)
                .then(({ accessToken, refreshToken: newRefresh }) => {
                    setTokens(accessToken, newRefresh);
                })
                .catch((err) => {
                    console.error('Immediate token refresh failed:', err);
                    logout();
                    window.location.href = '/login';
                });
            return;
        }

        console.info(`Scheduling background token refresh in ${(refreshDelayMs / 1000).toFixed(0)} seconds...`);
        const timer = setTimeout(() => {
            console.info('Token refresh timer fired. Triggering proactive refresh...');
            refreshCasdoorToken(refreshToken)
                .then(({ accessToken, refreshToken: newRefresh }) => {
                    setTokens(accessToken, newRefresh);
                })
                .catch((err) => {
                    console.error('Proactive token refresh failed:', err);
                    logout();
                    window.location.href = '/login';
                });
        }, refreshDelayMs);

        return () => clearTimeout(timer);
    }, [jwt, refreshToken, setTokens, logout]);
}
