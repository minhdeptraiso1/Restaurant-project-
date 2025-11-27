ALTER TABLE combos
    ADD COLUMN IF NOT EXISTS min_people int,
    ADD COLUMN IF NOT EXISTS max_people int;