import { create } from 'zustand';
import {type TransactionStatus } from '../types/transaction.types';

interface SagaState {
    activeSagaRef: string | null;
    currentStatus: TransactionStatus | null;
    isOtpModalOpen: boolean;
    statusMessage: string;
    setActiveSagaRef: (ref: string | null) => void;
    setCurrentStatus: (status: TransactionStatus | null) => void;
    setIsOtpModalOpen: (open: boolean) => void;
    setStatusMessage: (msg: string) => void;
    clearSaga: () => void;
}

export const useSagaStore = create<SagaState>((set) => ({
    activeSagaRef: null,
    currentStatus: null,
    isOtpModalOpen: false,
    statusMessage: '',
    setActiveSagaRef: (ref) => set({ activeSagaRef: ref }),
    setCurrentStatus: (status) => set({ currentStatus: status }),
    setIsOtpModalOpen: (open) => set({ isOtpModalOpen: open }),
    setStatusMessage: (msg) => set({ statusMessage: msg }),
    clearSaga: () => set({
        activeSagaRef: null,
        currentStatus: null,
        isOtpModalOpen: false,
        statusMessage: ''
    })
}));
