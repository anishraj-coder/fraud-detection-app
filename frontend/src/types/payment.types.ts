export interface RazorpayOrder {
    razorpayOrderId: string;
    razorpayKeyId: string;
    amount: number;
    currency: string;
    accountNumber: string;
}

export interface PaymentRequest {
    accountNumber: string;
    amount: number;
    description?: string;
}
