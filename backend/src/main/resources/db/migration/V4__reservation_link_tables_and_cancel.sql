-- 1) Đơn đặt bàn KHÔNG còn giữ cột table_id trực tiếp
ALTER TABLE reservations
DROP COLUMN IF EXISTS table_id;

-- 2) Thêm các cột phục vụ huỷ / audit
ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS cancel_reason TEXT,
    ADD COLUMN IF NOT EXISTS canceled_by   TEXT;  -- 'USER:<email>' | 'STAFF:<email>' | 'ADMIN:<email>'

-- 3) Bảng liên kết N–N giữa reservation và bàn
CREATE TABLE IF NOT EXISTS reservation_tables (
                                                  reservation_id UUID NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    table_id       UUID NOT NULL REFERENCES restaurant_tables(id) ON DELETE RESTRICT,
    PRIMARY KEY (reservation_id, table_id)
    );

-- Tối ưu tìm kiếm
CREATE INDEX IF NOT EXISTS idx_resv_tables_table ON reservation_tables(table_id);
CREATE INDEX IF NOT EXISTS idx_resv_tables_resv  ON reservation_tables(reservation_id);
