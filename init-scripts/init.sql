-- Create keycloak & casdoor databases for auth
CREATE DATABASE casdoor;

-- Connect to accounts database (default POSTGRES_DB)
\c accounts;

-- =========================================================================
-- 1. ACCOUNTS SERVICE SCHEMA & SEED DATA
-- =========================================================================
CREATE TABLE IF NOT EXISTS accounts_fraud (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    user_id VARCHAR(100) NOT NULL UNIQUE,
    account_holder_name VARCHAR(100) NOT NULL,
    account_type VARCHAR(30) NOT NULL,
    account_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    account_balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    daily_transaction_limit DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts_fraud(user_id);

INSERT INTO accounts_fraud (
    account_number, user_id, account_holder_name, account_type, account_status, email, phone, account_balance, daily_transaction_limit, created_at, updated_at
) VALUES
    ('50100012345671', 'usr-a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'Aarav Sharma', 'SAVINGS', 'ACTIVE', 'aarav.sharma@example.in', '9876543210', 75000.50, 100000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('50100012345672', 'usr-b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'Priya Patel', 'CHECKING', 'ACTIVE', 'priya.patel@example.in', '9876543211', 125000.75, 500000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('50100012345673', 'usr-c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f', 'Rohan Das', 'SAVINGS', 'ACTIVE', 'rohan.das@example.com', '9876543212', 4500.00, 100000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('50100012345674', 'usr-d4e5f6a7-b8c9-0d1e-2f3a-4b5c6d7e8f9a', 'Ananya Iyer', 'BUSINESS', 'BLOCKED', 'ananya.iyer@example.net', '9876543213', 89000.20, 500000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (account_number) DO NOTHING;


-- =========================================================================
-- 2. TRANSACTIONS SERVICE SCHEMA & SEED DATA
-- =========================================================================
CREATE TABLE IF NOT EXISTS transactions_fraud (
    id VARCHAR(255) PRIMARY KEY,
    sender_account_number VARCHAR(20),
    receiver_account_number VARCHAR(20),
    reference_number VARCHAR(100) NOT NULL UNIQUE,
    amount DECIMAL(19, 4) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(50) NOT NULL,
    description VARCHAR(100),
    failure_reason VARCHAR(255),
    type VARCHAR(50) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_transactions_sender_account ON transactions_fraud(sender_account_number);
CREATE INDEX IF NOT EXISTS idx_transactions_receiver_account ON transactions_fraud(receiver_account_number);
CREATE INDEX IF NOT EXISTS idx_transactions_status_created ON transactions_fraud(status, created_at);

INSERT INTO transactions_fraud (id, sender_account_number, receiver_account_number, reference_number, amount, created_at, completed_at, status, description, failure_reason, type)
VALUES
('tx-901', '50100012345671', '50100012345672', 'REF-20260717-001', 5000.00, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours', 'COMPLETED', 'Rent payment share', NULL, 'TRANSFER'),
('tx-902', '50100012345673', '50100012345675', 'REF-20260717-002', 1200.50, NOW() - INTERVAL '1 hour', NULL, 'PROCESSING', 'Invoice #4412', NULL, 'PAYMENT'),
('tx-903', '50100012345676', '50100012345680', 'REF-20260717-003', 65000.00, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', 'FAILED', 'High value transfer test', 'Transaction amount exceeds daily remaining limit', 'TRANSFER'),
('tx-904', '50100012345679', '50100012345671', 'REF-20260717-004', 150.00, NOW() - INTERVAL '30 minutes', NULL, 'PENDING_VERIFICATION', 'Peer transfer validation required', NULL, 'PAYMENT')
ON CONFLICT (id) DO NOTHING;


-- =========================================================================
-- 3. PAYMENTS SERVICE SCHEMA & SEED DATA
-- =========================================================================
CREATE TABLE IF NOT EXISTS payments_fraud (
    id VARCHAR(255) PRIMARY KEY,
    razor_payment_id VARCHAR(255),
    account_number VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    payment_status VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    failure_reason VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_payments_razor_payment_id ON payments_fraud(razor_payment_id);
CREATE INDEX IF NOT EXISTS idx_payments_account_number ON payments_fraud(account_number);
CREATE INDEX IF NOT EXISTS idx_payments_status_created ON payments_fraud(payment_status, created_at);

INSERT INTO payments_fraud (id, razor_payment_id, account_number, amount, currency, payment_status, description, failure_reason, created_at, updated_at)
VALUES
('pay-001-20260723', 'order_N1a2B3c4D5e6F1', '50100012345671', 500.00, 'INR', 'COMPLETED', 'Utility Bill Payment', NULL, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),
('pay-002-20260723', 'order_N2b3C4d5E6f7G2', '50100012345672', 1250.75, 'INR', 'CREATED', 'Online Merchant Purchase', NULL, NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '30 minutes'),
('pay-003-20260723', 'order_N3c4D5e6F7g8H3', '50100012345673', 2500.00, 'INR', 'PENDING', 'Wallet Add Money', NULL, NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '10 minutes'),
('pay-004-20260723', 'order_N4d5E6f7G8h9I4', '50100012345675', 10000.00, 'INR', 'FAILED', 'Software License Fee', 'Payment failed due to insufficient funds', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),
('pay-005-20260723', 'order_N5e6F7g8H9i0J5', '50100012345676', 750.25, 'INR', 'REFUNDED', 'E-commerce Return Refund', NULL, NOW() - INTERVAL '3 days', NOW() - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;
