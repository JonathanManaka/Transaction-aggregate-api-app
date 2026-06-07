CREATE TABLE transactions (
    id              BIGSERIAL PRIMARY KEY,
    external_id     VARCHAR(255) NOT NULL,
    source          VARCHAR(50)  NOT NULL,
    description     VARCHAR(500) NOT NULL,
    amount          DECIMAL(15, 2) NOT NULL,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'ZAR',
    category        VARCHAR(50),
    transaction_date TIMESTAMP   NOT NULL,
    account_id      VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_external_id_source UNIQUE (external_id, source)
);

CREATE INDEX idx_transactions_category ON transactions (category);
CREATE INDEX idx_transactions_account_id ON transactions (account_id);
CREATE INDEX idx_transactions_transaction_date ON transactions (transaction_date);

