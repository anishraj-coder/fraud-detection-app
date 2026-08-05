DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS accounts_fraud;

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