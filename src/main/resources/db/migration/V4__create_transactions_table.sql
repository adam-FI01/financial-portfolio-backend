CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    plaid_transaction_id VARCHAR(255),
    amount NUMERIC(19, 4) NOT NULL,
    description VARCHAR(500) NOT NULL,
    category VARCHAR(255),
    transaction_date DATE NOT NULL,
    pending BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts (id)
);

CREATE INDEX idx_transactions_account_id ON transactions (account_id);
CREATE INDEX idx_transactions_transaction_date ON transactions (transaction_date);
