-- ================================================================
-- V15: Bổ sung ON UPDATE CASCADE cho toàn bộ foreign key
-- ================================================================

-- ===== USERS =====
-- loyalty_accounts → users
ALTER TABLE loyalty_accounts
DROP CONSTRAINT IF EXISTS loyalty_accounts_user_id_fkey,
    ADD CONSTRAINT loyalty_accounts_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE;

-- user_vouchers.user_id → users
ALTER TABLE user_vouchers
DROP CONSTRAINT IF EXISTS user_vouchers_user_id_fkey,
    ADD CONSTRAINT user_vouchers_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE;


-- ===== AREAS =====
ALTER TABLE restaurant_tables
DROP CONSTRAINT IF EXISTS restaurant_tables_area_id_fkey,
    ADD CONSTRAINT restaurant_tables_area_id_fkey
        FOREIGN KEY (area_id) REFERENCES areas(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE;


-- ===== DISHES =====
ALTER TABLE dishes
DROP CONSTRAINT IF EXISTS dishes_category_id_fkey,
    ADD CONSTRAINT dishes_category_id_fkey
        FOREIGN KEY (category_id) REFERENCES categories(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE;


-- ===== COMBOS =====
ALTER TABLE combo_items
DROP CONSTRAINT IF EXISTS combo_items_combo_id_fkey,
    ADD CONSTRAINT combo_items_combo_id_fkey
        FOREIGN KEY (combo_id) REFERENCES combos(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE;

ALTER TABLE combo_items
DROP CONSTRAINT IF EXISTS combo_items_dish_id_fkey,
    ADD CONSTRAINT combo_items_dish_id_fkey
        FOREIGN KEY (dish_id) REFERENCES dishes(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE;


-- ===== RESERVATIONS =====
ALTER TABLE reservations
DROP CONSTRAINT IF EXISTS reservations_user_id_fkey,
    ADD CONSTRAINT reservations_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE;

-- reservation_tables (reservation_id)
ALTER TABLE reservation_tables
DROP CONSTRAINT IF EXISTS reservation_tables_reservation_id_fkey,
    ADD CONSTRAINT reservation_tables_reservation_id_fkey
        FOREIGN KEY (reservation_id) REFERENCES reservations(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE;

-- reservation_tables (table_id)
ALTER TABLE reservation_tables
DROP CONSTRAINT IF EXISTS reservation_tables_table_id_fkey,
    ADD CONSTRAINT reservation_tables_table_id_fkey
        FOREIGN KEY (table_id) REFERENCES restaurant_tables(id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE;


-- ===== ORDERS =====
ALTER TABLE orders
DROP CONSTRAINT IF EXISTS orders_user_id_fkey,
    ADD CONSTRAINT orders_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;


ALTER TABLE orders
DROP CONSTRAINT IF EXISTS orders_table_id_fkey,
    ADD CONSTRAINT orders_table_id_fkey
        FOREIGN KEY (table_id) REFERENCES restaurant_tables(id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;

ALTER TABLE orders
DROP CONSTRAINT IF EXISTS fk_orders_user_voucher,
    ADD CONSTRAINT fk_orders_user_voucher
        FOREIGN KEY (applied_user_voucher_id) REFERENCES user_vouchers(id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;


-- ===== ORDER ITEMS =====
ALTER TABLE order_items
DROP CONSTRAINT IF EXISTS order_items_order_id_fkey,
    ADD CONSTRAINT order_items_order_id_fkey
        FOREIGN KEY (order_id) REFERENCES orders(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE;


-- ===== PAYMENTS =====
ALTER TABLE payments
DROP CONSTRAINT IF EXISTS payments_order_id_fkey,
    ADD CONSTRAINT payments_order_id_fkey
        FOREIGN KEY (order_id) REFERENCES orders(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE;


-- ===== REVIEWS =====
ALTER TABLE reviews
DROP CONSTRAINT IF EXISTS fk_reviews_order,
    ADD CONSTRAINT fk_reviews_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE;

ALTER TABLE reviews
DROP CONSTRAINT IF EXISTS fk_reviews_user,
    ADD CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id) REFERENCES users(id)
            ON DELETE CASCADE
            ON UPDATE CASCADE;


-- ===== OTP TOKENS =====
-- (Không có foreign key — bỏ qua)

-- DONE
