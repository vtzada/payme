ALTER TABLE accounts
    ADD CONSTRAINT chk_accounts_balance_non_negative CHECK (balance >= 0);

ALTER TABLE transactions
    ADD CONSTRAINT chk_transactions_amount_positive CHECK (amount > 0);

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_category
    FOREIGN KEY (category_id) REFERENCES categories (id);

ALTER TABLE transactions
    ADD CONSTRAINT fk_transactions_destination
    FOREIGN KEY (destination_account_id) REFERENCES accounts (id);

CREATE INDEX IF NOT EXISTS idx_accounts_customer_id ON accounts (customer_id);
CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions (account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_destination_account_id ON transactions (destination_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_category_id ON transactions (category_id);
