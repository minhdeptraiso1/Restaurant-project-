-- ===== ORDERS =====
CREATE TABLE IF NOT EXISTS orders (
                                      id              UUID PRIMARY KEY,
                                      user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    table_id        UUID REFERENCES restaurant_tables(id) ON DELETE SET NULL,
    types           TEXT NOT NULL,
    status          TEXT NOT NULL,
    subtotal        NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount        NUMERIC(12,2) NOT NULL DEFAULT 0,
    tax             NUMERIC(12,2) NOT NULL DEFAULT 0,
    total           NUMERIC(12,2) NOT NULL DEFAULT 0,
    note            TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );
CREATE INDEX IF NOT EXISTS idx_orders_user        ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_table       ON orders(table_id);
CREATE INDEX IF NOT EXISTS idx_orders_status      ON orders(status);

-- ===== ORDER ITEMS =====
CREATE TABLE IF NOT EXISTS order_items (
                                           id         UUID PRIMARY KEY,
                                           order_id   UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    item_type  TEXT NOT NULL,                -- DISH/COMBO
    item_id    UUID NOT NULL,                -- id của dish/ combo tương ứng
    name       TEXT NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    quantity   INT NOT NULL CHECK (quantity > 0),
    line_total NUMERIC(12,2) NOT NULL CHECK (line_total >= 0)
    );
CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);

-- ===== PAYMENTS =====
CREATE TABLE IF NOT EXISTS payments (
                                        id          UUID PRIMARY KEY,
                                        order_id    UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    method      TEXT NOT NULL,
    amount      NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    status      TEXT NOT NULL,                 -- PENDING/SUCCEEDED/FAILED
    paid_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );
CREATE INDEX IF NOT EXISTS idx_payments_order ON payments(order_id);

-- ===== LOYALTY POINTS (tích luỹ) =====
CREATE TABLE IF NOT EXISTS loyalty_accounts (
                                                user_id     UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    points      BIGINT NOT NULL DEFAULT 0,  -- tổng điểm hiện có
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

-- ===== VOUCHERS =====
CREATE TABLE IF NOT EXISTS vouchers (
                                        id           UUID PRIMARY KEY,
                                        code         TEXT NOT NULL UNIQUE,        -- mã voucher
                                        name         TEXT NOT NULL,
                                        type         TEXT NOT NULL,               -- PERCENT/FIXED
                                        value        NUMERIC(12,2) NOT NULL,      -- % hoặc số tiền tuỳ type
    min_order    NUMERIC(12,2) NOT NULL DEFAULT 0,
    max_discount NUMERIC(12,2),               -- trần giảm nếu type = PERCENT (nullable)
    status       TEXT NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE/INACTIVE/EXPIRED
    start_at     TIMESTAMPTZ,
    end_at       TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

-- ===== USER VOUCHERS (đổi điểm lấy voucher) =====
CREATE TABLE IF NOT EXISTS user_vouchers (
                                             id          UUID PRIMARY KEY,
                                             user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    voucher_id  UUID NOT NULL REFERENCES vouchers(id) ON DELETE CASCADE,
    redeemed    BOOLEAN NOT NULL DEFAULT FALSE,
    redeemed_at TIMESTAMPTZ,
    UNIQUE(user_id, voucher_id, redeemed) -- tránh cấp trùng voucher chưa dùng
    );
CREATE INDEX IF NOT EXISTS idx_user_vouchers_user ON user_vouchers(user_id);
