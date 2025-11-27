
CREATE UNIQUE INDEX IF NOT EXISTS ux_orders_open_per_table
ON orders(table_id)
WHERE status = 'OPEN';
