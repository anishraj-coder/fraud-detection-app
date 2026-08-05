INSERT INTO accounts_fraud (
    account_number,
    user_id,
    account_holder_name,
    account_type,
    account_status,
    email,
    phone,
    account_balance,
    daily_transaction_limit,
    created_at,
    updated_at
)
VALUES
    ('50100012345671', 'usr-a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'Aarav Sharma', 'SAVINGS', 'ACTIVE', 'aarav.sharma@example.in', '9876543210', 75000.50, 100000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('50100012345672', 'usr-b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'Priya Patel', 'CHECKING', 'ACTIVE', 'priya.patel@example.in', '9876543211', 125000.75, 500000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('50100012345673', 'usr-c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f', 'Rohan Das', 'SAVINGS', 'ACTIVE', 'rohan.das@example.com', '9876543212', 4500.00, 100000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('50100012345674', 'usr-d4e5f6a7-b8c9-0d1e-2f3a-4b5c6d7e8f9a', 'Ananya Iyer', 'BUSINESS', 'BLOCKED', 'ananya.iyer@example.net', '9876543213', 89000.20, 500000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);