    -- ===== AREAS =====
    INSERT INTO areas (id, name, description, status, created_at, updated_at)
    VALUES
        ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'Khu VIP', 'Khu vực VIP sang trọng, phục vụ khách đặc biệt', 'ACTIVE', NOW(), NOW()),
        ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'Nhà Gỗ', 'Khu nhà gỗ truyền thống Tây Bắc', 'ACTIVE', NOW(), NOW()),
        ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'Nhà Bạt', 'Khu nhà bạt mát mẻ, thoáng đãng', 'ACTIVE', NOW(), NOW()),
        ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'Sân Vườn', 'Khu vực sân vườn ngoài trời', 'ACTIVE', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

    -- ===== RESTAURANT TABLES =====
    INSERT INTO restaurant_tables (id, area_id, code, seats, status, created_at, updated_at)
    VALUES
        -- Khu VIP
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb01', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'VIP01', 6, 'AVAILABLE', NOW(), NOW()),
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb02', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'VIP02', 8, 'AVAILABLE', NOW(), NOW()),
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb03', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'VIP03', 4, 'AVAILABLE', NOW(), NOW()),
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb04', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'VIP04', 10, 'AVAILABLE', NOW(), NOW()),

        -- Nhà Gỗ
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb05', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'NG01', 8, 'AVAILABLE', NOW(), NOW()),
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb06', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'NG02', 6, 'AVAILABLE', NOW(), NOW()),
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb07', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'NG03', 4, 'AVAILABLE', NOW(), NOW()),
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb08', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'NG04', 12, 'AVAILABLE', NOW(), NOW()),
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb09', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'NG05', 4, 'AVAILABLE', NOW(), NOW()),

        -- Nhà Bạt
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb10', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'NB01', 4, 'AVAILABLE', NOW(), NOW()),
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb11', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'NB02', 6, 'AVAILABLE', NOW(), NOW()),
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb12', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'NB03', 8, 'AVAILABLE', NOW(), NOW()),
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb13', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'NB04', 4, 'AVAILABLE', NOW(), NOW()),
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb14', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'NB05', 6, 'AVAILABLE', NOW(), NOW()),

        -- Sân Vườn
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb15', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'SV01', 2, 'AVAILABLE', NOW(), NOW()),
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb16', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'SV02', 4, 'AVAILABLE', NOW(), NOW()),
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb17', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'SV03', 6, 'AVAILABLE', NOW(), NOW()),
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb18', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'SV04', 8, 'AVAILABLE', NOW(), NOW()),
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb19', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'SV05', 4, 'AVAILABLE', NOW(), NOW()),
        ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb20', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'SV06', 10, 'AVAILABLE', NOW(), NOW())
        ON CONFLICT (id) DO NOTHING;

    -- ===== RESERVATIONS =====
    INSERT INTO reservations (id, user_id, start_time, end_time, party_size, status, note, created_at, updated_at)
    VALUES
        ('cccccccc-cccc-cccc-cccc-cccccccccc01', '33333333-3333-3333-3333-333333333333', NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day 2 hours', 4, 'CONFIRMED', 'Kỷ niệm sinh nhật - Ưu tiên khu VIP', NOW(), NOW()),
        ('cccccccc-cccc-cccc-cccc-cccccccccc02', '33333333-3333-3333-3333-333333333333', NOW() + INTERVAL '2 days', NOW() + INTERVAL '2 days 3 hours', 8, 'PENDING', 'Tiệc gia đình - Thích nhà gỗ', NOW(), NOW()),
        ('cccccccc-cccc-cccc-cccc-cccccccccc03', '22222222-2222-2222-2222-222222222222', NOW() + INTERVAL '3 days', NOW() + INTERVAL '3 days 2 hours', 2, 'CONFIRMED', 'Hẹn hò - Sân vườn lãng mạn', NOW(), NOW()),
        ('cccccccc-cccc-cccc-cccc-cccccccccc04', '22222222-2222-2222-2222-222222222222', NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day 3 hours', 6, 'CONFIRMED', 'Liên hoan nhóm - Nhà bạt thoáng mát', NOW(), NOW())
        ON CONFLICT (id) DO NOTHING;

    -- ===== RESERVATION TABLES =====
    INSERT INTO reservation_tables (reservation_id, table_id)
    VALUES
        ('cccccccc-cccc-cccc-cccc-cccccccccc01', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb01'), -- VIP01
        ('cccccccc-cccc-cccc-cccc-cccccccccc02', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb05'), -- NG01
        ('cccccccc-cccc-cccc-cccc-cccccccccc03', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb15'), -- SV01
        ('cccccccc-cccc-cccc-cccc-cccccccccc04', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb12')  -- NB03
        ON CONFLICT (reservation_id, table_id) DO NOTHING;

    -- ===== ORDERS =====
    INSERT INTO orders (id, user_id, table_id, types, status, subtotal, discount, tax, total, note, created_at, updated_at)
    VALUES
        ('dddddddd-dddd-dddd-dddd-dddddddddd01', '33333333-3333-3333-3333-333333333333', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb01', 'DINE_IN', 'PAID', 650000, 0, 65000, 715000, 'Khách hàng thân thiết - Khu VIP', NOW(), NOW()),
        ('dddddddd-dddd-dddd-dddd-dddddddddd02', '22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb07', 'DINE_IN', 'OPEN', 320000, 32000, 28800, 316800, 'Order tại quán - Nhà Gỗ', NOW(), NOW()),
        ('dddddddd-dddd-dddd-dddd-dddddddddd03', '33333333-3333-3333-3333-333333333333', NULL, 'DINE_IN', 'OPEN', 180000, 0, 18000, 198000, 'Order mang về', NOW(), NOW()),
        ('dddddddd-dddd-dddd-dddd-dddddddddd04', '22222222-2222-2222-2222-222222222222', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb12', 'DINE_IN', 'OPEN', 420000, 42000, 37800, 415800, 'Liên hoan nhóm - Nhà Bạt', NOW(), NOW())
        ON CONFLICT (id) DO NOTHING;

    -- ===== ORDER ITEMS =====
    INSERT INTO order_items (id, order_id, item_type, item_id, name, unit_price, quantity, line_total)
    VALUES
        -- Order 1 (Khu VIP)
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee01', 'dddddddd-dddd-dddd-dddd-dddddddddd01', 'COMBO', '33333333-3333-3333-3333-333333333301', 'Combo Gia Đình 4 Người', 950000, 1, 950000),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee02', 'dddddddd-dddd-dddd-dddd-dddddddddd01', 'DISH', '22222222-2222-2222-2222-222222222236', 'Bia Sài Gòn', 25000, 4, 100000),

        -- Order 2 (Nhà Gỗ)
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee03', 'dddddddd-dddd-dddd-dddd-dddddddddd02', 'DISH', '22222222-2222-2222-2222-222222222207', 'Lợn Cắp Nách Nướng', 350000, 1, 350000),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee04', 'dddddddd-dddd-dddd-dddd-dddddddddd02', 'DISH', '22222222-2222-2222-2222-222222222218', 'Rau Muống Xào Tỏi', 35000, 2, 70000),

        -- Order 3 (Mang về)
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee05', 'dddddddd-dddd-dddd-dddd-dddddddddd03', 'COMBO', '33333333-3333-3333-3333-333333333306', 'Combo Văn Phòng Express', 95000, 2, 190000),

        -- Order 4 (Nhà Bạt)
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee06', 'dddddddd-dddd-dddd-dddd-dddddddddd04', 'COMBO', '33333333-3333-3333-3333-333333333303', 'Combo BBQ Đặc Biệt', 420000, 1, 420000),
        ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeee07', 'dddddddd-dddd-dddd-dddd-dddddddddd04', 'DISH', '22222222-2222-2222-2222-222222222236', 'Bia Sài Gòn', 25000, 4, 100000)
        ON CONFLICT (id) DO NOTHING;

    -- ===== PAYMENTS =====
    INSERT INTO payments (id, order_id, method, amount, status, paid_at, created_at)
    VALUES
        ('ffffffff-ffff-ffff-ffff-ffffffff0001', 'dddddddd-dddd-dddd-dddd-dddddddddd01', 'CASH', 715000, 'SUCCEEDED', NOW(), NOW()),
        ('ffffffff-ffff-ffff-ffff-ffffffff0002', 'dddddddd-dddd-dddd-dddd-dddddddddd02', 'CARD', 316800, 'PENDING', NULL, NOW()),
        ('ffffffff-ffff-ffff-ffff-ffffffff0003', 'dddddddd-dddd-dddd-dddd-dddddddddd04', 'QR', 415800, 'SUCCEEDED', NOW(), NOW())
        ON CONFLICT (id) DO NOTHING;

    -- ===== LOYALTY ACCOUNTS =====
    INSERT INTO loyalty_accounts (user_id, points, updated_at)
    VALUES
        ('33333333-3333-3333-3333-333333333333', 1500, NOW()),
        ('22222222-2222-2222-2222-222222222222', 500, NOW())
        ON CONFLICT (user_id) DO NOTHING;

    -- ===== VOUCHERS =====
    INSERT INTO vouchers (id, code, name, type, value, min_order, max_discount, status, start_at, end_at, point_cost, created_at)
    VALUES
        ('44444444-4444-4444-4444-444444444401', 'WELCOME10', 'Giảm 10% cho khách mới', 'PERCENT', 10, 200000, 50000, 'ACTIVE', NOW(), NOW() + INTERVAL '30 days', 100, NOW()),
        ('44444444-4444-4444-4444-444444444402', 'FREESHIP', 'Miễn phí vận chuyển', 'FIXED', 30000, 150000, NULL, 'ACTIVE', NOW(), NOW() + INTERVAL '15 days', 50, NOW()),
        ('44444444-4444-4444-4444-444444444403', 'COMBO20', 'Giảm 20% combo', 'PERCENT', 20, 300000, 100000, 'ACTIVE', NOW(), NOW() + INTERVAL '60 days', 200, NOW()),
        ('44444444-4444-4444-4444-444444444404', 'VIP50', 'Giảm 50K cho đơn 500K', 'FIXED', 50000, 500000, NULL, 'ACTIVE', NOW(), NOW() + INTERVAL '7 days', 150, NOW()),
        ('44444444-4444-4444-4444-444444444405', 'GONHAGO', 'Ưu đãi đặc biệt khu Nhà Gỗ', 'PERCENT', 15, 400000, 75000, 'ACTIVE', NOW(), NOW() + INTERVAL '45 days', 180, NOW())
        ON CONFLICT (id) DO NOTHING;

    -- ===== USER VOUCHERS =====
    INSERT INTO user_vouchers (id, user_id, voucher_id, redeemed, redeemed_at)
    VALUES
        ('55555555-5555-5555-5555-555555555501', '33333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444401', false, NULL),
        ('55555555-5555-5555-5555-555555555502', '33333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444402', false, NULL),
        ('55555555-5555-5555-5555-555555555503', '22222222-2222-2222-2222-222222222222', '44444444-4444-4444-4444-444444444403', true, NOW()),
        ('55555555-5555-5555-5555-555555555504', '22222222-2222-2222-2222-222222222222', '44444444-4444-4444-4444-444444444405', false, NULL)
        ON CONFLICT (id) DO NOTHING;




    -- ===== OTP TOKENS =====
    INSERT INTO otp_tokens (id, email, purpose, code_hash, expires_at, consumed_at, attempts, created_at)
    VALUES
        ('77777777-7777-7777-7777-777777777701', 'vu@gmail.com', 'REGISTRATION', 'hashed_code_123456', NOW() + INTERVAL '10 minutes', NULL, 0, NOW()),
        ('77777777-7777-7777-7777-777777777702', 'staff@hoaban.vn', 'PASSWORD_RESET', 'hashed_code_654321', NOW() + INTERVAL '10 minutes', NULL, 0, NOW())
        ON CONFLICT (id) DO NOTHING;

    -- ===== UPDATE USERS WITH ADDRESS =====
    UPDATE users SET
                     address = '123 Đường Láng, Đống Đa, Hà Nội',
                     updated_at = NOW()
    WHERE id = '33333333-3333-3333-3333-333333333333';

    UPDATE users SET
                     address = '456 Phố Huế, Hai Bà Trưng, Hà Nội',
                     updated_at = NOW()
    WHERE id = '22222222-2222-2222-2222-222222222222';

    UPDATE users SET
                     address = '789 Tây Sơn, Đống Đa, Hà Nội',
                     updated_at = NOW()
    WHERE id = '11111111-1111-1111-1111-111111111111';

    -- ===== DONE =====
