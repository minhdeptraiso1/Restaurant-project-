-- Voucher cần biết tốn bao nhiêu điểm để đổi
ALTER TABLE vouchers
    ADD COLUMN IF NOT EXISTS point_cost BIGINT NOT NULL DEFAULT 0;

-- Mỗi order có thể áp 1 user_voucher (để mark redeemed khi pay)
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS applied_user_voucher_id UUID,
    ADD CONSTRAINT fk_orders_user_voucher
    FOREIGN KEY (applied_user_voucher_id) REFERENCES user_vouchers(id) ON DELETE SET NULL;
