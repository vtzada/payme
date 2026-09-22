CREATE TABLE transactions
(
    id                     UUID PRIMARY KEY,
    account_id             UUID           NOT NULL,
    type                   VARCHAR(50)    NOT NULL,
    amount                 DECIMAL(19, 2) NOT NULL,
    date                   TIMESTAMP      NOT NULL,
    description            VARCHAR(255)   NOT NULL,
    category_id            UUID,
    destination_account_id UUID,
    created_at             TIMESTAMP      NOT NULL,

    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts (id)
);