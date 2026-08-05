import { useMutation, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../lib/axios';
import { API_CONFIG } from '../config/api.config';
import type { PaymentRequest, RazorpayOrder } from '../types/payment.types';

export function useCreatePaymentOrder() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (payload: PaymentRequest): Promise<RazorpayOrder> => {
            const res = await apiClient.post(API_CONFIG.ENDPOINTS.createPaymentOrder, payload);
            return res.data;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['myAccount'] });
        }
    });
}
