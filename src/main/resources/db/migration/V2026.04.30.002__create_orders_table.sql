CREATE TABLE orders.orders (
    id VARCHAR(36) PRIMARY KEY,
    id_account VARCHAR(36) NOT NULL,
    date TIMESTAMP NOT NULL,
    total NUMERIC(19, 2) NOT NULL
);
