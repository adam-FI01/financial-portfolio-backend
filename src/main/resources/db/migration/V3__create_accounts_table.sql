CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id UUID NOT NULL,
    plaid_account_id VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    current_balance NUMERIC(19, 4) NOT NULL,
    available_balance NUMERIC(19, 4),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_accounts_institution FOREIGN KEY (institution_id) REFERENCES institutions (id),
    CONSTRAINT chk_accounts_type CHECK (type IN ('CHECKING', 'SAVINGS', 'CREDIT_CARD', 'INVESTMENT', 'LOAN', 'OTHER'))
);

CREATE INDEX idx_accounts_institution_id ON accounts (institution_id);
