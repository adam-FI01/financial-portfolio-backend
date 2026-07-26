CREATE TABLE holdings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    shares NUMERIC(19, 6) NOT NULL,
    cost_basis NUMERIC(19, 4) NOT NULL,
    current_value NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_holdings_account FOREIGN KEY (account_id) REFERENCES accounts (id)
);

CREATE INDEX idx_holdings_account_id ON holdings (account_id);
