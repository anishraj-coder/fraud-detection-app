import { z } from 'zod';

export const transferSchema = z.object({
    receiverAccountNumber: z
        .string()
        .min(1, 'Receiver account number is required')
        .regex(/^\d+$/, 'Account number must contain digits only'),
    amount: z
        .number({ message: 'Please enter a valid amount' })
        .positive('Transfer amount must be greater than 0'),
    description: z.string().optional()
});

export type TransferSchemaType = z.infer<typeof transferSchema>;

export const onboardingSchema = z.object({
    accountHolderName: z
        .string()
        .min(2, 'Account holder name must be at least 2 characters'),
    accountType: z.enum(['SAVINGS', 'CURRENT', 'CHECKING', 'BUSINESS']),
    phone: z
        .string()
        .min(10, 'Please enter a valid phone number'),
    initialDeposit: z
        .number({ message: 'Please enter a valid deposit amount' })
        .min(100, 'Initial deposit must be at least ₹100')
});

export type OnboardingSchemaType = z.infer<typeof onboardingSchema>;

export const depositSchema = z.object({
    amount: z
        .number({ message: 'Please enter a valid deposit amount' })
        .min(100, 'Deposit amount must be at least ₹100')
});

export type DepositSchemaType = z.infer<typeof depositSchema>;

export const otpSchema = z.object({
    otp: z
        .string()
        .length(6, 'Verification OTP must be exactly 6 digits')
        .regex(/^\d{6}$/, 'OTP must contain 6 digits only')
});

export type OtpSchemaType = z.infer<typeof otpSchema>;

export const loginCredentialsSchema = z.object({
    username: z.string().min(1, 'Username or Email is required'),
    password: z.string().min(1, 'Password is required'),
    rememberMe: z.boolean().optional()
});

export type LoginCredentialsSchemaType = z.infer<typeof loginCredentialsSchema>;

export const signUpSchema = z
    .object({
        fullName: z.string().min(2, 'Full name must be at least 2 characters'),
        email: z.string().email('Please enter a valid email address'),
        username: z.string().min(3, 'Username must be at least 3 characters'),
        password: z.string().min(6, 'Password must be at least 6 characters'),
        confirmPassword: z.string().min(6, 'Please confirm your password')
    })
    .refine((data) => data.password === data.confirmPassword, {
        message: 'Passwords do not match',
        path: ['confirmPassword']
    });

export type SignUpSchemaType = z.infer<typeof signUpSchema>;
