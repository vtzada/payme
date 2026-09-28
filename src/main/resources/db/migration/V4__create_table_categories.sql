CREATE TABLE categories
(
    id          UUID PRIMARY KEY,
    customer_id UUID         NOT NULL,
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL,

    CONSTRAINT fk_categories_customer FOREIGN KEY (customer_id) REFERENCES customers (id),

    CONSTRAINT uk_categories_customer_name UNIQUE (customer_id, name, type)
);

CREATE INDEX idx_categories_customer_id ON categories (customer_id);