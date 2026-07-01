CREATE TABLE source_transactions (
    id              BIGSERIAL PRIMARY KEY,
    external_id     VARCHAR(255) NOT NULL,
    source          VARCHAR(50)  NOT NULL,
    description     VARCHAR(500) NOT NULL,
    amount          DECIMAL(15, 2) NOT NULL,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'ZAR',
    transaction_date TIMESTAMP   NOT NULL,
    account_id      VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_source_external_id_source UNIQUE (external_id, source)
);

CREATE INDEX idx_source_transactions_source ON source_transactions (source);
