INSERT INTO payments_fraud (id, razor_payment_id, account_number, amount, currency, payment_status, description, failure_reason, created_at, updated_at)
VALUES
-- Completed Payment for Aarav Sharma
('pay-001-20260723', 'order_N1a2B3c4D5e6F1', '50100012345671', 500.00, 'INR', 'COMPLETED', 'Utility Bill Payment', NULL, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),

-- Newly Created Payment for Priya Patel (Awaiting User Action)
('pay-002-20260723', 'order_N2b3C4d5E6f7G2', '50100012345672', 1250.75, 'INR', 'CREATED', 'Online Merchant Purchase', NULL, NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '30 minutes'),

-- Pending Verification Payment for Rohan Das
('pay-003-20260723', 'order_N3c4D5e6F7g8H3', '50100012345673', 2500.00, 'INR', 'PENDING', 'Wallet Add Money', NULL, NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '10 minutes'),

-- Failed Payment for Vikram Singh (Simulating Payment Gateway Decline)
('pay-004-20260723', 'order_N4d5E6f7G8h9I4', '50100012345675', 10000.00, 'INR', 'FAILED', 'Software License Fee', 'Payment failed due to insufficient funds', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

-- Refunded Payment for Sneha Kulkarni
('pay-005-20260723', 'order_N5e6F7g8H9i0J5', '50100012345676', 750.25, 'INR', 'REFUNDED', 'E-commerce Return Refund', NULL, NOW() - INTERVAL '3 days', NOW() - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;