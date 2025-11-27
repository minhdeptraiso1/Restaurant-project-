ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS transaction_id varchar(64) UNIQUE,
    ADD COLUMN IF NOT EXISTS bank_code varchar(32),
    ADD COLUMN IF NOT EXISTS card_type varchar(32),
    ADD COLUMN IF NOT EXISTS description varchar(255),
    ADD COLUMN IF NOT EXISTS extra_data varchar(1024),
    ADD COLUMN IF NOT EXISTS expired_at timestamptz;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS address varchar(255);
