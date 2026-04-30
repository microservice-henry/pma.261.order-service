CREATE TABLE orders.order_items (
    id VARCHAR(36) PRIMARY KEY,
    id_order VARCHAR(36) NOT NULL,
    id_product VARCHAR(36) NOT NULL,
    quantity INTEGER NOT NULL,
    total NUMERIC(19, 2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (id_order) REFERENCES orders.orders(id)
);
