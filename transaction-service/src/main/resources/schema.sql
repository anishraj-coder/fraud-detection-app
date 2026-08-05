-- Drop tables if they exist to allow clean re-initialization
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS transactions_fraud;

-- Create transactions_fraud table matching the Transaction entity
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

-- =========================================================================
-- INDEXES FOR QUERY OPTIMIZATION
-- =========================================================================

-- Note: reference_number already has an automatic unique index from the UNIQUE constraint

-- 1. Indexes on account numbers to optimize history/ledger lookups
CREATE INDEX IF NOT EXISTS idx_transactions_sender_account ON transactions_fraud(sender_account_number);
CREATE INDEX IF NOT EXISTS idx_transactions_receiver_account ON transactions_fraud(receiver_account_number);

-- 2. Composite Index for status and creation date (for reconciliation/audit jobs)
CREATE INDEX IF NOT EXISTS idx_transactions_status_created ON transactions_fraud(status, created_at);