CREATE TABLE IF NOT EXISTS users (
                                     id           UUID PRIMARY KEY,
                                     full_name    TEXT NOT NULL,
                                     email        TEXT UNIQUE,
                                     phone        TEXT UNIQUE,
                                     password_hash TEXT,
                                     role         TEXT,
                                     status       TEXT,
                                     avatar_url   TEXT,
                                     created_at   TIMESTAMPTZ,
                                     updated_at   TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_users_role   ON users(role);