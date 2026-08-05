export const API_CONFIG = {
    GATEWAY_URL: import.meta.env.VITE_GATEWAY_URL || 'http://localhost:8089',
    
    // Casdoor Configuration
    CASDOOR: {
        SERVER_URL: import.meta.env.VITE_CASDOOR_SERVER_URL || 'http://localhost:8000',
        CLIENT_ID: import.meta.env.VITE_CASDOOR_CLIENT_ID || 'f76a11192a4d7a9ed1db',
        CLIENT_SECRET: import.meta.env.VITE_CASDOOR_CLIENT_SECRET || '',
        APP_NAME: import.meta.env.VITE_CASDOOR_APP_NAME || 'fraud-application',
        ORG_NAME: import.meta.env.VITE_CASDOOR_ORG_NAME || 'fraud-service',
        REDIRECT_PATH: '/'
    },

    ENDPOINTS: {
        getMyAccount: '/api/v1/accounts/me',
        getMyBalance: '/api/v1/accounts/me/balance',
        onboardAccount: '/api/v1/accounts/me/onboard',
        createPaymentOrder: '/api/v1/payments/create',
        initiateTransfer: '/api/v1/transactions/transfer',
        getTransactionStatus: (refNum: string) => `/api/v1/transactions/transaction?referenceNumber=${refNum}`,
        verifyOtp: (refId: string, otp: string) => `/api/v1/transactions/transaction/verify/${refId}?otp=${otp}`,
        getTransactionHistory: '/api/v1/transactions/transaction/history'
    }
};
