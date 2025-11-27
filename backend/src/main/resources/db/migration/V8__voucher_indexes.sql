CREATE INDEX IF NOT EXISTS idx_vouchers_status_time ON vouchers(status, start_at, end_at);
CREATE INDEX IF NOT EXISTS idx_vouchers_point_cost   ON vouchers(point_cost);

CREATE TABLE IF NOT EXISTS otp_tokens (
                                          id UUID PRIMARY KEY,
                                          email TEXT NOT NULL,
                                          purpose TEXT NOT NULL,
                                          code_hash TEXT NOT NULL,
                                          expires_at TIMESTAMPTZ NOT NULL,
                                          consumed_at TIMESTAMPTZ,
                                          attempts INT NOT NULL DEFAULT 0,
                                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );
CREATE INDEX IF NOT EXISTS idx_otp_email_purpose ON otp_tokens(email, purpose);