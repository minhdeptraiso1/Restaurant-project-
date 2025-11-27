-- ===== CATEGORIES =====
CREATE TABLE IF NOT EXISTS categories (
                                          id           UUID PRIMARY KEY,
                                          name         TEXT NOT NULL UNIQUE,
                                          description  TEXT,
                                          status       TEXT NOT NULL DEFAULT 'ACTIVE',
                                          created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

-- ===== DISHES =====
CREATE TABLE IF NOT EXISTS dishes (
                                      id           UUID PRIMARY KEY,
                                      category_id  UUID NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    name         TEXT NOT NULL,
    description  TEXT,
    unit         TEXT NOT NULL DEFAULT 'PORTION', -- PORTION/PLATE/BOWL/GLASS...
    price        NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    status       TEXT NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE/INACTIVE
    image_url    TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(category_id, name)
    );

CREATE INDEX IF NOT EXISTS idx_dishes_category ON dishes(category_id);
CREATE INDEX IF NOT EXISTS idx_dishes_status   ON dishes(status);

-- ===== COMBOS (gói món) =====
CREATE TABLE IF NOT EXISTS combos (
                                      id           UUID PRIMARY KEY,
                                      name         TEXT NOT NULL UNIQUE,
                                      description  TEXT,
                                      price        NUMERIC(12,2) NOT NULL CHECK (price >= 0), -- giá bán combo (cố định)
    status       TEXT NOT NULL DEFAULT 'ACTIVE',            -- ACTIVE/INACTIVE
    image_url    TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

-- ===== COMBO ITEMS (danh sách món trong combo) =====
CREATE TABLE IF NOT EXISTS combo_items (
                                           combo_id     UUID NOT NULL REFERENCES combos(id) ON DELETE CASCADE,
    dish_id      UUID NOT NULL REFERENCES dishes(id) ON DELETE RESTRICT,
    quantity     INT  NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (combo_id, dish_id)
    );

CREATE INDEX IF NOT EXISTS idx_combo_items_dish ON combo_items(dish_id);
