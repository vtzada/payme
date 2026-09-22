CREATE TABLE accounts
(
    id           UUID PRIMARY KEY,
    customer_id  UUID           NOT NULL,
    name         VARCHAR(255)   NOT NULL,
    balance      DECIMAL(19, 2) NOT NULL,
    account_type VARCHAR(50)    NOT NULL,
    sync_type    VARCHAR(50)    NOT NULL,
    created_at   TIMESTAMP      NOT NULL,

    CONSTRAINT fk_accounts_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);