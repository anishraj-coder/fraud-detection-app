import { useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../lib/axios';
import { API_CONFIG } from '../config/api.config';
import type { Transaction, CustomerTransferRequest } from '../types/transaction.types';
import { useAuthStore } from '../store/useAuthStore';
import { useSagaStore } from '../store/useSagaStore';

export const TRANSACTIONS_QUERY_KEY = ['transactionHistory'];
export const SAGA_STATUS_QUERY_KEY = ['sagaStatus'];

export function useTransactionHistory() {
    const jwt = useAuthStore((state) => state.jwt);

    return useQuery<Transaction[]>({
        queryKey: TRANSACTIONS_QUERY_KEY,
        queryFn: async () => {
            const res = await apiClient.get(API_CONFIG.ENDPOINTS.getTransactionHistory);
            return res.data;
        },
        enabled: Boolean(jwt),
        staleTime: 1000 * 10 // 10s caching
    });
}

export function useTransferMoney() {
    const queryClient = useQueryClient();
    const setActiveSagaRef = useSagaStore((state) => state.setActiveSagaRef);
    const setStatusMessage = useSagaStore((state) => state.setStatusMessage);

    return useMutation({
        mutationFn: async (payload: CustomerTransferRequest) => {
            const res = await apiClient.post(API_CONFIG.ENDPOINTS.initiateTransfer, payload);
            return res.data;
        },
        onSuccess: (data) => {
            if (data?.referenceNumber) {
                setActiveSagaRef(data.referenceNumber);
                setStatusMessage('Transfer initiated. Streaming real-time SSE SAGA updates...');
            }
            queryClient.invalidateQueries({ queryKey: TRANSACTIONS_QUERY_KEY });
        }
    });
}

export function useSagaStatus(refNum: string | null) {
    const queryClient = useQueryClient();
    const setIsOtpModalOpen = useSagaStore((state) => state.setIsOtpModalOpen);
    const setCurrentStatus = useSagaStore((state) => state.setCurrentStatus);

    // Reactive WebFlux Server-Sent Events (SSE) Listener
    useEffect(() => {
        if (!refNum) return;

        const sseUrl = `${API_CONFIG.GATEWAY_URL}/api/v1/transactions/transaction/stream/${refNum}`;
        const eventSource = new EventSource(sseUrl);

        eventSource.addEventListener('saga-step', (event: MessageEvent) => {
            try {
                const tx: Transaction = JSON.parse(event.data);
                setCurrentStatus(tx.status);

                // Instantly update React Query cache
                queryClient.setQueryData([...SAGA_STATUS_QUERY_KEY, refNum], tx);

                if (tx.status === 'PENDING_VERIFICATION') {
                    setIsOtpModalOpen(true);
                } else if (tx.status === 'COMPLETED' || tx.status === 'FAILED_REFUNDED') {
                    queryClient.invalidateQueries({ queryKey: ['myAccount'] });
                    queryClient.invalidateQueries({ queryKey: TRANSACTIONS_QUERY_KEY });
                    eventSource.close();
                }
            } catch (err) {
                console.warn('Failed to parse SSE SAGA payload:', err);
            }
        });

        eventSource.onerror = () => {
            eventSource.close();
        };

        return () => {
            eventSource.close();
        };
    }, [refNum, queryClient, setCurrentStatus, setIsOtpModalOpen]);

    return useQuery<Transaction>({
        queryKey: [...SAGA_STATUS_QUERY_KEY, refNum],
        queryFn: async () => {
            if (!refNum) throw new Error('No SAGA reference');
            const res = await apiClient.get(API_CONFIG.ENDPOINTS.getTransactionStatus(refNum));
            const tx: Transaction = res.data;

            setCurrentStatus(tx.status);

            if (tx.status === 'PENDING_VERIFICATION') {
                setIsOtpModalOpen(true);
            } else if (tx.status === 'COMPLETED' || tx.status === 'FAILED_REFUNDED') {
                queryClient.invalidateQueries({ queryKey: ['myAccount'] });
                queryClient.invalidateQueries({ queryKey: TRANSACTIONS_QUERY_KEY });
            }
            return tx;
        },
        enabled: Boolean(refNum),
        refetchInterval: (query) => {
            const data = query.state.data;
            if (!data) return 3000;
            if (data.status === 'COMPLETED' || data.status === 'FAILED' || data.status === 'FAILED_REFUNDED') {
                return false;
            }
            return 3000; // Fallback polling if SSE is disconnected
        }
    });
}

export function useVerifyOtp() {
    const queryClient = useQueryClient();
    const setIsOtpModalOpen = useSagaStore((state) => state.setIsOtpModalOpen);

    return useMutation({
        mutationFn: async ({ refId, otp }: { refId: string; otp: string }) => {
            const res = await apiClient.post(API_CONFIG.ENDPOINTS.verifyOtp(refId, otp));
            return res.data;
        },
        onSuccess: () => {
            setIsOtpModalOpen(false);
            queryClient.invalidateQueries({ queryKey: ['myAccount'] });
            queryClient.invalidateQueries({ queryKey: TRANSACTIONS_QUERY_KEY });
        }
    });
}
