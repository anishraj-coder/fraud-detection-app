import { create } from 'zustand';
import { type Account } from '../types/account.types';

interface AuthState {
    jwt: string | null;
    refreshToken: string | null;
    account: Account | null;
    isAuthenticated: boolean;
    setJwt: (token: string | null) => void;
    setTokens: (accessToken: string | null, refreshToken: string | null) => void;
    setAccount: (acc: Account | null) => void;
    logout: () => void;
}

const ACCESS_TOKEN_KEY = 'avalon_jwt_token';
const REFRESH_TOKEN_KEY = 'avalon_refresh_token';

export const useAuthStore = create<AuthState>((set) => {
    const savedJwt = localStorage.getItem(ACCESS_TOKEN_KEY);
    const savedRefreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);

    return {
        jwt: savedJwt,
        refreshToken: savedRefreshToken,
        account: null,
        isAuthenticated: Boolean(savedJwt),
        
        setJwt: (token) => {
            if (token) {
                localStorage.setItem(ACCESS_TOKEN_KEY, token);
                set({ jwt: token, isAuthenticated: true });
            } else {
                localStorage.removeItem(ACCESS_TOKEN_KEY);
                set({ jwt: null, isAuthenticated: false, account: null });
            }
        },

        setTokens: (accessToken, refreshToken) => {
            if (accessToken) {
                localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
                if (refreshToken) {
                    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
                }
                set({ jwt: accessToken, refreshToken: refreshToken || null, isAuthenticated: true });
            } else {
                localStorage.removeItem(ACCESS_TOKEN_KEY);
                localStorage.removeItem(REFRESH_TOKEN_KEY);
                set({ jwt: null, refreshToken: null, isAuthenticated: false, account: null });
            }
        },

        setAccount: (acc) => set({ account: acc }),

        logout: () => {
            localStorage.removeItem(ACCESS_TOKEN_KEY);
            localStorage.removeItem(REFRESH_TOKEN_KEY);
            set({ jwt: null, refreshToken: null, isAuthenticated: false, account: null });
        }
    };
});
