-- Drop table if it already exists to allow clean re-initialization
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS payments_fraud;

-- Create payments_fraud table matching the Payment entity definition
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

-- =========================================================================
-- INDEXES FOR QUERY OPTIMIZATION
-- =========================================================================

-- 1. Index on razor_payment_id: CRITICAL for Webhook performance
CREATE INDEX IF NOT EXISTS idx_payments_razor_payment_id
    ON payments_fraud(razor_payment_id);

-- 2. Index on account_number: Optimizes user payment history lookups
CREATE INDEX IF NOT EXISTS idx_payments_account_number
    ON payments_fraud(account_number);

-- 3. Composite Index on payment_status and created_at:
-- Optimizes background reconciliation jobs filtering by status over time
CREATE INDEX IF NOT EXISTS idx_payments_status_created
    ON payments_fraud(payment_status, created_at);