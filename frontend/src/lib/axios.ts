import axios from 'axios';
import { API_CONFIG } from '../config/api.config';
import { useAuthStore } from '../store/useAuthStore';
import { refreshCasdoorToken } from './casdoor';

export const apiClient = axios.create({
    baseURL: API_CONFIG.GATEWAY_URL,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
});

// Request Interceptor: Attach JWT Bearer Token
apiClient.interceptors.request.use(
    (config) => {
        const token = useAuthStore.getState().jwt;
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

let isRefreshing = false;
let failedQueue: Array<{
    resolve: (token: string) => void;
    reject: (error: any) => void;
}> = [];

const processQueue = (error: any, token: string | null = null) => {
    failedQueue.forEach((promise) => {
        if (error) {
            promise.reject(error);
        } else if (token) {
            promise.resolve(token);
        }
    });
    failedQueue = [];
};

// Response Interceptor: Handle Expired Tokens & Automatic Silent Refresh
apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {
            const refreshToken = useAuthStore.getState().refreshToken;

            // If no refresh token exists, immediately logout and redirect to /login
            if (!refreshToken) {
                console.warn('No refresh token available. Logging out and redirecting to login...');
                useAuthStore.getState().logout();
                if (!window.location.pathname.startsWith('/login')) {
                    window.location.href = '/login';
                }
                return Promise.reject(error);
            }

            if (isRefreshing) {
                // Queue concurrent requests while token refresh is in progress
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                })
                    .then((token) => {
                        originalRequest.headers.Authorization = `Bearer ${token}`;
                        return apiClient(originalRequest);
                    })
                    .catch((err) => Promise.reject(err));
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                console.info('Access token expired (401). Attempting automatic silent token refresh via Casdoor...');
                const { accessToken: newAccessToken, refreshToken: newRefreshToken } = await refreshCasdoorToken(refreshToken);

                useAuthStore.getState().setTokens(newAccessToken, newRefreshToken);
                processQueue(null, newAccessToken);

                originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                return apiClient(originalRequest);
            } catch (refreshErr) {
                console.error('Failed to refresh token. Session expired. Redirecting to login...', refreshErr);
                processQueue(refreshErr, null);
                useAuthStore.getState().logout();

                if (!window.location.pathname.startsWith('/login')) {
                    window.location.href = '/login';
                }
                return Promise.reject(refreshErr);
            } finally {
                isRefreshing = false;
            }
        }

        return Promise.reject(error);
    }
);
