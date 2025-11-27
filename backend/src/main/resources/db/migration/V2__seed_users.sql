INSERT INTO users (id, full_name, email, phone, password_hash, role, status, avatar_url, created_at, updated_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'Admin Hoa Ban', 'minh@gmail.com', '0900000001',
        '{bcrypt}$2a$10$f1XMXSKDf5DgUZYf.Yvwgen03PoY9qMs1R4qPGdjh/XWPlBPbT5LK', 'ADMIN', 'ACTIVE', NULL, now(), now()),
       ('22222222-2222-2222-2222-222222222222', 'Nhân viên A', 'staff@hoaban.vn', '0900000002',
        '{bcrypt}$2a$10$f1XMXSKDf5DgUZYf.Yvwgen03PoY9qMs1R4qPGdjh/XWPlBPbT5LK', 'STAFF', 'ACTIVE', NULL, now(), now()),
       ('33333333-3333-3333-3333-333333333333', 'Khách hàng demo', 'vu@gmail.com', '0900000003',
        '{bcrypt}$2a$10$f1XMXSKDf5DgUZYf.Yvwgen03PoY9qMs1R4qPGdjh/XWPlBPbT5LK', 'CUSTOMER', 'ACTIVE', NULL, now(),
        now());


