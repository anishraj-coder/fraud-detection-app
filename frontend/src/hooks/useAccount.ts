import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../lib/axios';
import { API_CONFIG } from '../config/api.config';
import type { Account, OnboardingRequest } from '../types/account.types';
import { useAuthStore } from '../store/useAuthStore';

export const ACCOUNT_QUERY_KEY = ['myAccount'];

export function useMyAccount() {
    const jwt = useAuthStore((state) => state.jwt);
    const setAccount = useAuthStore((state) => state.setAccount);

    return useQuery<Account>({
        queryKey: ACCOUNT_QUERY_KEY,
        queryFn: async () => {
            const res = await apiClient.get(API_CONFIG.ENDPOINTS.getMyAccount);
            const accData = res.data;
            setAccount(accData);
            return accData;
        },
        enabled: Boolean(jwt),
        staleTime: 1000 * 30, // 30s caching
        retry: 1
    });
}

export function useOnboardAccount() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (payload: OnboardingRequest) => {
            const res = await apiClient.post(API_CONFIG.ENDPOINTS.onboardAccount, payload);
            return res.data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ACCOUNT_QUERY_KEY });
        }
    });
}
