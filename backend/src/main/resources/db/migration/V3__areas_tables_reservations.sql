-- ===== AREAS =====
CREATE TABLE IF NOT EXISTS areas (
  id           UUID PRIMARY KEY,
  name         TEXT NOT NULL UNIQUE,
  description  TEXT,
  status       TEXT NOT NULL DEFAULT 'ACTIVE',
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===== TABLES (dùng restaurant_tables để tránh keyword TABLE) =====
CREATE TABLE IF NOT EXISTS restaurant_tables (
  id           UUID PRIMARY KEY,
  area_id      UUID NOT NULL REFERENCES areas(id) ON DELETE RESTRICT,
  code         TEXT NOT NULL,  -- ví dụ T01, T02
  seats        INT  NOT NULL CHECK (seats > 0),
  status       TEXT NOT NULL DEFAULT 'AVAILABLE',
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE(area_id, code)
);

CREATE INDEX IF NOT EXISTS idx_tables_area ON restaurant_tables(area_id);
CREATE INDEX IF NOT EXISTS idx_tables_status ON restaurant_tables(status);

-- ===== RESERVATIONS =====
CREATE TABLE IF NOT EXISTS reservations (
  id           UUID PRIMARY KEY,
  user_id      UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  table_id     UUID NOT NULL REFERENCES restaurant_tables(id) ON DELETE RESTRICT,
  start_time   TIMESTAMPTZ NOT NULL,
  end_time     TIMESTAMPTZ   NOT NULL,
  party_size   INT NOT NULL CHECK (party_size > 0),
  status       TEXT NOT NULL DEFAULT 'PENDING', -- PENDING/CONFIRMED/CANCELLED/COMPLETED
  note         TEXT,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_reservations_table_time ON reservations(table_id, start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_reservations_user ON reservations(user_id);
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations(status);
