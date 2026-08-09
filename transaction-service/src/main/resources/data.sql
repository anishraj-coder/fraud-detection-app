INSERT INTO transactions_fraud (id, sender_account_number, receiver_account_number, reference_number, amount, created_at, completed_at, status, description, failure_reason, type)
VALUES
-- Transfer from Aarav to Priya
('tx-901', '50100012345671', '50100012345672', 'REF-20260717-001', 5000.00, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours', 'COMPLETED', 'Rent payment share', NULL, 'TRANSFER'),

-- Payment from Rohan to Vikram's Business Account
('tx-902', '50100012345673', '50100012345675', 'REF-20260717-002', 1200.50, NOW() - INTERVAL '1 hour', NULL, 'PROCESSING', 'Invoice #4412', NULL, 'PAYMENT'),

-- Failed Transfer from Sneha to Kabir (Simulating a failure)
('tx-903', '50100012345676', '50100012345680', 'REF-20260717-003', 65000.00, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', 'FAILED', 'High value transfer test', 'Transaction amount exceeds daily remaining limit', 'TRANSFER'),

-- Pending Deposit/Payment verification for Meera
('tx-904', '50100012345679', '50100012345671', 'REF-20260717-004', 150.00, NOW() - INTERVAL '30 minutes', NULL, 'PENDING_VERIFICATION', 'Peer transfer validation required', NULL, 'PAYMENT')
ON CONFLICT (id) DO NOTHING;