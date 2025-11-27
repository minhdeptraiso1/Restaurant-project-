-- Bỏ UNIQUE cũ:
ALTER TABLE user_vouchers
DROP CONSTRAINT IF EXISTS user_vouchers_user_id_voucher_id_redeemed_key;

-- Chỉ cấm 2 bản ghi pending cùng loại cho 1 user
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_voucher_pending
    ON user_vouchers(user_id, voucher_id)
    WHERE redeemed = false;

CREATE TABLE reviews (
                         id UUID PRIMARY KEY,
                         order_id UUID NOT NULL,
                         user_id UUID NOT NULL,
                         rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                         comment TEXT,
                         created_at TIMESTAMP DEFAULT NOW(),

                         CONSTRAINT fk_reviews_order
                             FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,

                         CONSTRAINT fk_reviews_user
                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

                         CONSTRAINT uq_reviews_order UNIQUE(order_id)
);


