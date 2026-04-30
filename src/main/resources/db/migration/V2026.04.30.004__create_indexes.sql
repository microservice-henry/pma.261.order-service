CREATE INDEX idx_orders_account_date ON orders.orders (id_account, date);
CREATE INDEX idx_order_items_order ON orders.order_items (id_order);
