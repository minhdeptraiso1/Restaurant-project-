INSERT INTO categories (id, name, description, status, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111101', 'Khai Vị', 'Các món khai vị truyền thống và hiện đại', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111102', 'Món Nướng', 'Các món nướng đặc sản Tây Bắc', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111103', 'Món Xào', 'Các món xào hấp dẫn', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111104', 'Lẩu', 'Lẩu các loại cho gia đình và nhóm bạn', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111105', 'Cơm & Cháo', 'Cơm và cháo dinh dưỡng', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111106', 'Món Chính', 'Các món chính đặc sản', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111107', 'Đồ Uống', 'Nước giải khát và đồ uống có cồn', 'ACTIVE', NOW(), NOW()),
    ('11111111-1111-1111-1111-111111111108', 'Tráng Miệng', 'Các món tráng miệng ngon miệng', 'ACTIVE', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- ===== INSERT 40 DISHES =====
INSERT INTO dishes (id, category_id, name, description, unit, price, status, image_url, created_at, updated_at)
VALUES
-- Khai Vị
('22222222-2222-2222-2222-222222222201', '11111111-1111-1111-1111-111111111101', 'Gỏi Cuốn Tôm Thịt', 'Gỏi cuốn tươi ngon với tôm, thịt và rau sống', 'PLATE', 45000, 'ACTIVE', 'https://images.unsplash.com/photo-1594007654729-407eedc4be65', NOW(), NOW()),
('22222222-2222-2222-2222-222222222202', '11111111-1111-1111-1111-111111111101', 'Nem Rán', 'Nem rán giòn rụm, nhân thịt đầy đặn', 'PLATE', 50000, 'ACTIVE', 'https://images.unsplash.com/photo-1582169296194-e4d644c48063', NOW(), NOW()),
('22222222-2222-2222-2222-222222222203', '11111111-1111-1111-1111-111111111101', 'Salad Rau Củ', 'Salad tươi mát với sốt đặc biệt', 'PLATE', 40000, 'ACTIVE', 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd', NOW(), NOW()),
('22222222-2222-2222-2222-222222222204', '11111111-1111-1111-1111-111111111101', 'Chả Giò Rế', 'Chả giò Tây Bắc truyền thống', 'PLATE', 55000, 'ACTIVE', 'https://images.unsplash.com/photo-1563245372-f21724e3856d', NOW(), NOW()),
('22222222-2222-2222-2222-222222222205', '11111111-1111-1111-1111-111111111101', 'Đậu Hũ Chiên Giòn', 'Đậu hũ chiên vàng giòn, ăn kèm nước chấm', 'PLATE', 35000, 'ACTIVE', 'https://images.unsplash.com/photo-1631452180519-c014fe946bc7', NOW(), NOW()),
('22222222-2222-2222-2222-222222222206', '11111111-1111-1111-1111-111111111101', 'Gỏi Gà Bắp Cải', 'Gỏi gà trộn bắp cải tươi ngon', 'PLATE', 60000, 'ACTIVE', 'https://images.unsplash.com/photo-1546833999-b9f581a1996d', NOW(), NOW()),

-- Món Nướng
('22222222-2222-2222-2222-222222222207', '11111111-1111-1111-1111-111111111102', 'Lợn Cắp Nách Nướng', 'Đặc sản Tây Bắc - Lợn cắp nách nướng thơm lừng', 'PORTION', 350000, 'ACTIVE', 'https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba', NOW(), NOW()),
('22222222-2222-2222-2222-222222222208', '11111111-1111-1111-1111-111111111102', 'Gà Nướng Mật Ong', 'Gà nguyên con nướng mật ong thơm ngon', 'PORTION', 280000, 'ACTIVE', 'https://images.unsplash.com/photo-1598103442097-8b74394b95c6', NOW(), NOW()),
('22222222-2222-2222-2222-222222222209', '11111111-1111-1111-1111-111111111102', 'Cá Hồi Nướng Muối Ớt', 'Cá hồi tươi nướng muối ớt đậm đà', 'PORTION', 220000, 'ACTIVE', 'https://images.unsplash.com/photo-1467003909585-2f8a72700288', NOW(), NOW()),
('22222222-2222-2222-2222-222222222210', '11111111-1111-1111-1111-111111111102', 'Sườn Nướng BBQ', 'Sườn heo nướng BBQ sốt đặc biệt', 'PORTION', 180000, 'ACTIVE', 'https://images.unsplash.com/photo-1544025162-d76694265947', NOW(), NOW()),
('22222222-2222-2222-2222-222222222211', '11111111-1111-1111-1111-111111111102', 'Bò Nướng Lá Lốt', 'Bò cuộn lá lốt nướng thơm', 'PLATE', 120000, 'ACTIVE', 'https://images.unsplash.com/photo-1559620192-032c4bc4674e', NOW(), NOW()),
('22222222-2222-2222-2222-222222222212', '11111111-1111-1111-1111-111111111102', 'Cánh Gà Nướng Mắm Tỏi', 'Cánh gà nướng mắm tỏi cay thơm', 'PLATE', 90000, 'ACTIVE', 'https://images.unsplash.com/photo-1604503468506-a8da13d82791', NOW(), NOW()),
('22222222-2222-2222-2222-222222222213', '11111111-1111-1111-1111-111111111102', 'Chân Gà Nướng Sa Tế', 'Chân gà nướng sa tế cay nồng', 'PLATE', 70000, 'ACTIVE', 'https://images.unsplash.com/photo-1599487488170-d11ec9c172f0', NOW(), NOW()),
('22222222-2222-2222-2222-222222222214', '11111111-1111-1111-1111-111111111102', 'Mực Nướng Bơ Tỏi', 'Mực tươi nướng bơ tỏi thơm phức', 'PLATE', 150000, 'ACTIVE', 'https://images.unsplash.com/photo-1563379926898-05f4575a45d8', NOW(), NOW()),

-- Món Xào
('22222222-2222-2222-2222-222222222215', '11111111-1111-1111-1111-111111111103', 'Bò Xào Lúc Lắc', 'Bò Úc xào lúc lắc khoai tây', 'PLATE', 160000, 'ACTIVE', 'https://images.unsplash.com/photo-1588168333986-5078d3ae3976', NOW(), NOW()),
('22222222-2222-2222-2222-222222222216', '11111111-1111-1111-1111-111111111103', 'Mực Xào Sa Tế', 'Mực xào sa tế cay nồng', 'PLATE', 130000, 'ACTIVE', 'https://images.unsplash.com/photo-1563379926898-05f4575a45d8', NOW(), NOW()),
('22222222-2222-2222-2222-222222222217', '11111111-1111-1111-1111-111111111103', 'Tôm Xào Thập Cẩm', 'Tôm xào rau củ đầy màu sắc', 'PLATE', 140000, 'ACTIVE', 'https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2', NOW(), NOW()),
('22222222-2222-2222-2222-222222222218', '11111111-1111-1111-1111-111111111103', 'Rau Muống Xào Tỏi', 'Rau muống xào tỏi giòn ngọt', 'PLATE', 35000, 'ACTIVE', 'https://images.unsplash.com/photo-1546833999-b9f581a1996d', NOW(), NOW()),
('22222222-2222-2222-2222-222222222219', '11111111-1111-1111-1111-111111111103', 'Thịt Ba Chỉ Xào Măng', 'Thịt ba chỉ xào măng đậm đà', 'PLATE', 95000, 'ACTIVE', 'https://images.unsplash.com/photo-1563245372-f21724e3856d', NOW(), NOW()),
('22222222-2222-2222-2222-222222222220', '11111111-1111-1111-1111-111111111103', 'Gà Xào Sả Ớt', 'Gà xào sả ớt cay thơm', 'PLATE', 110000, 'ACTIVE', 'https://images.unsplash.com/photo-1598103442097-8b74394b95c6', NOW(), NOW()),
('22222222-2222-2222-2222-222222222221', '11111111-1111-1111-1111-111111111103', 'Bông Cải Xào Nấm', 'Bông cải xào nấm thanh đạm', 'PLATE', 55000, 'ACTIVE', 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd', NOW(), NOW()),
('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111103', 'Hải Sản Xào Hành Tây', 'Hải sản tổng hợp xào hành tây', 'PLATE', 180000, 'ACTIVE', 'https://images.unsplash.com/photo-1563379926898-05f4575a45d8', NOW(), NOW()),

-- Lẩu
('22222222-2222-2222-2222-222222222223', '11111111-1111-1111-1111-111111111104', 'Lẩu Thái Hải Sản', 'Lẩu thái chua cay với hải sản tươi ngon', 'BOWL', 450000, 'ACTIVE', 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43', NOW(), NOW()),
('22222222-2222-2222-2222-222222222224', '11111111-1111-1111-1111-111111111104', 'Lẩu Bò Nhúng Dấm', 'Lẩu bò nhúng dấm phong cách Hà Nội', 'BOWL', 380000, 'ACTIVE', 'https://images.unsplash.com/photo-1588168333986-5078d3ae3976', NOW(), NOW()),
('22222222-2222-2222-2222-222222222225', '11111111-1111-1111-1111-111111111104', 'Lẩu Gà Lá É', 'Lẩu gà lá é đặc sản Tây Bắc', 'BOWL', 420000, 'ACTIVE', 'https://images.unsplash.com/photo-1598103442097-8b74394b95c6', NOW(), NOW()),
('22222222-2222-2222-2222-222222222226', '11111111-1111-1111-1111-111111111104', 'Lẩu Hải Sản Tokbokki', 'Lẩu tokbokki kết hợp hải sản tươi', 'BOWL', 480000, 'ACTIVE', 'https://images.unsplash.com/photo-1563379926898-05f4575a45d8', NOW(), NOW()),

-- Cơm & Cháo
('22222222-2222-2222-2222-222222222227', '11111111-1111-1111-1111-111111111105', 'Cơm Chiên Hải Sản', 'Cơm chiên hải sản đầy đặn', 'PLATE', 65000, 'ACTIVE', 'https://images.unsplash.com/photo-1512058564366-18510be2db19', NOW(), NOW()),
('22222222-2222-2222-2222-222222222228', '11111111-1111-1111-1111-111111111105', 'Cơm Chiên Dương Châu', 'Cơm chiên Dương Châu truyền thống', 'PLATE', 55000, 'ACTIVE', 'https://images.unsplash.com/photo-1563245372-f21724e3856d', NOW(), NOW()),
('22222222-2222-2222-2222-222222222229', '11111111-1111-1111-1111-111111111105', 'Cháo Gà Sợi', 'Cháo gà sợi dinh dưỡng', 'BOWL', 45000, 'ACTIVE', 'https://images.unsplash.com/photo-1546833999-b9f581a1996d', NOW(), NOW()),
('22222222-2222-2222-2222-222222222230', '11111111-1111-1111-1111-111111111105', 'Cơm Trắng', 'Cơm trắng thơm dẻo', 'BOWL', 15000, 'ACTIVE', 'https://images.unsplash.com/photo-1563245372-f21724e3856d', NOW(), NOW()),
('22222222-2222-2222-2222-222222222231', '11111111-1111-1111-1111-111111111105', 'Cháo Hải Sản', 'Cháo hải sản tươi ngon bổ dưỡng', 'BOWL', 60000, 'ACTIVE', 'https://images.unsplash.com/photo-1563379926898-05f4575a45d8', NOW(), NOW()),

-- Món Chính
('22222222-2222-2222-2222-222222222232', '11111111-1111-1111-1111-111111111106', 'Cá Lóc Hấp Bia', 'Cá lóc hấp bia thơm lừng', 'PORTION', 280000, 'ACTIVE', 'https://images.unsplash.com/photo-1467003909585-2f8a72700288', NOW(), NOW()),
('22222222-2222-2222-2222-222222222233', '11111111-1111-1111-1111-111111111106', 'Vịt Quay Bắc Kinh', 'Vịt quay giòn da, thịt mềm', 'PORTION', 320000, 'ACTIVE', 'https://images.unsplash.com/photo-1599487488170-d11ec9c172f0', NOW(), NOW()),
('22222222-2222-2222-2222-222222222234', '11111111-1111-1111-1111-111111111106', 'Cá Chép Om Dưa', 'Cá chép om dưa đậm đà', 'PORTION', 240000, 'ACTIVE', 'https://images.unsplash.com/photo-1467003909585-2f8a72700288', NOW(), NOW()),
('22222222-2222-2222-2222-222222222235', '11111111-1111-1111-1111-111111111106', 'Thịt Kho Tàu', 'Thịt kho tàu truyền thống', 'PLATE', 85000, 'ACTIVE', 'https://images.unsplash.com/photo-1563245372-f21724e3856d', NOW(), NOW()),

-- Đồ Uống
('22222222-2222-2222-2222-222222222236', '11111111-1111-1111-1111-111111111107', 'Bia Sài Gòn', 'Bia Sài Gòn chai 330ml', 'GLASS', 25000, 'ACTIVE', 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64', NOW(), NOW()),
('22222222-2222-2222-2222-222222222237', '11111111-1111-1111-1111-111111111107', 'Nước Ngọt Có Gas', 'Coca/Pepsi/7Up/Sprite', 'GLASS', 20000, 'ACTIVE', 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97', NOW(), NOW()),
('22222222-2222-2222-2222-222222222238', '11111111-1111-1111-1111-111111111107', 'Trà Đá', 'Trà đá miễn phí', 'GLASS', 0, 'ACTIVE', 'https://images.unsplash.com/photo-1556679343-c7306c1976bc', NOW(), NOW()),

-- Tráng Miệng
('22222222-2222-2222-2222-222222222239', '11111111-1111-1111-1111-111111111108', 'Chè Ba Màu', 'Chè ba màu mát lạnh', 'GLASS', 25000, 'ACTIVE', 'https://images.unsplash.com/photo-1563805042-7684c019e1cb', NOW(), NOW()),
('22222222-2222-2222-2222-222222222240', '11111111-1111-1111-1111-111111111108', 'Hoa Quả Tươi', 'Đĩa hoa quả theo mùa', 'PLATE', 50000, 'ACTIVE', 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- ===== INSERT 10 COMBOS =====
INSERT INTO combos (id, name, description, price, status, image_url, min_people, max_people, created_at, updated_at)
VALUES
    -- Combo 1: Combo Gia Đình 4 người
    ('33333333-3333-3333-3333-333333333301', 'Combo Gia Đình 4 Người', 'Set đầy đủ cho gia đình 4 người với lợn cắp nách, gà nướng, rau và cơm', 950000, 'ACTIVE', 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1', 4, 6, NOW(), NOW()),

    -- Combo 2: Combo Lẩu Hải Sản
    ('33333333-3333-3333-3333-333333333302', 'Combo Lẩu Hải Sản VIP', 'Lẩu thái hải sản + gỏi cuốn + nem rán + nước ngọt', 550000, 'ACTIVE', 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43', 2, 4, NOW(), NOW()),

    -- Combo 3: Combo Nướng BBQ
    ('33333333-3333-3333-3333-333333333303', 'Combo BBQ Đặc Biệt', 'Sườn nướng + mực nướng + bò lá lốt + cơm + rau', 420000, 'ACTIVE', 'https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba', 3, 5, NOW(), NOW()),

    -- Combo 4: Combo Couple Lãng Mạn
    ('33333333-3333-3333-3333-333333333304', 'Combo Couple Lãng Mạn', 'Set cho 2 người: cá hồi nướng, salad, bò xào, cơm chiên và nước', 480000, 'ACTIVE', 'https://images.unsplash.com/photo-1467003909585-2f8a72700288', 2, 2, NOW(), NOW()),

    -- Combo 5: Combo Nhậu Nhẹ
    ('33333333-3333-3333-3333-333333333305', 'Combo Nhậu Nhẹ 4 Người', 'Chân gà nướng + cánh gà nướng + đậu hũ chiên + bia', 280000, 'ACTIVE', 'https://images.unsplash.com/photo-1599487488170-d11ec9c172f0', 4, 6, NOW(), NOW()),

    -- Combo 6: Combo Văn Phòng
    ('33333333-3333-3333-3333-333333333306', 'Combo Văn Phòng Express', 'Cơm chiên hải sản + gỏi cuốn + nước ngọt (giao nhanh)', 95000, 'ACTIVE', 'https://images.unsplash.com/photo-1512058564366-18510be2db19', 1, 1, NOW(), NOW()),

    -- Combo 7: Combo Chay
    ('33333333-3333-3333-3333-333333333307', 'Combo Chay Thanh Tịnh', 'Đậu hũ chiên + rau muống xào + bông cải xào nấm + cơm trắng', 120000, 'ACTIVE', 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd', 2, 3, NOW(), NOW()),

    -- Combo 8: Combo Tiệc Nhỏ
    ('33333333-3333-3333-3333-333333333308', 'Combo Tiệc Nhỏ 6-8 Người', 'Gà nướng + lẩu bò + bò xào + hải sản xào + 2 rau + cơm', 1200000, 'ACTIVE', 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1', 6, 8, NOW(), NOW()),

    -- Combo 9: Combo Khai Vị
    ('33333333-3333-3333-3333-333333333309', 'Combo Khai Vị Trọn Vị', 'Nem rán + gỏi cuốn + gỏi gà + salad + nước ngọt', 180000, 'ACTIVE', 'https://images.unsplash.com/photo-1594007654729-407eedc4be65', 3, 5, NOW(), NOW()),

    -- Combo 10: Combo Đặc Sản Tây Bắc
    ('33333333-3333-3333-3333-333333333310', 'Combo Đặc Sản Tây Bắc', 'Lợn cắp nách nướng + lẩu gà lá é + chả giò rế + rượu cần', 850000, 'ACTIVE', 'https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba', 4, 6, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- ===== INSERT COMBO ITEMS (món trong combo) =====

-- Combo 1: Combo Gia Đình 4 Người
INSERT INTO combo_items (combo_id, dish_id, quantity) VALUES
                                                          ('33333333-3333-3333-3333-333333333301', '22222222-2222-2222-2222-222222222207', 1), -- Lợn cắp nách
                                                          ('33333333-3333-3333-3333-333333333301', '22222222-2222-2222-2222-222222222208', 1), -- Gà nướng
                                                          ('33333333-3333-3333-3333-333333333301', '22222222-2222-2222-2222-222222222218', 2), -- Rau muống xào
                                                          ('33333333-3333-3333-3333-333333333301', '22222222-2222-2222-2222-222222222230', 4); -- Cơm trắng

-- Combo 2: Combo Lẩu Hải Sản VIP
INSERT INTO combo_items (combo_id, dish_id, quantity) VALUES
                                                          ('33333333-3333-3333-3333-333333333302', '22222222-2222-2222-2222-222222222223', 1), -- Lẩu thái hải sản
                                                          ('33333333-3333-3333-3333-333333333302', '22222222-2222-2222-2222-222222222201', 1), -- Gỏi cuốn
                                                          ('33333333-3333-3333-3333-333333333302', '22222222-2222-2222-2222-222222222202', 1), -- Nem rán
                                                          ('33333333-3333-3333-3333-333333333302', '22222222-2222-2222-2222-222222222237', 4); -- Nước ngọt

-- Combo 3: Combo BBQ Đặc Biệt
INSERT INTO combo_items (combo_id, dish_id, quantity) VALUES
                                                          ('33333333-3333-3333-3333-333333333303', '22222222-2222-2222-2222-222222222210', 1), -- Sườn nướng
                                                          ('33333333-3333-3333-3333-333333333303', '22222222-2222-2222-2222-222222222214', 1), -- Mực nướng
                                                          ('33333333-3333-3333-3333-333333333303', '22222222-2222-2222-2222-222222222211', 1), -- Bò lá lốt
                                                          ('33333333-3333-3333-3333-333333333303', '22222222-2222-2222-2222-222222222230', 2), -- Cơm trắng
                                                          ('33333333-3333-3333-3333-333333333303', '22222222-2222-2222-2222-222222222218', 1); -- Rau muống

-- Combo 4: Combo Couple Lãng Mạn
INSERT INTO combo_items (combo_id, dish_id, quantity) VALUES
                                                          ('33333333-3333-3333-3333-333333333304', '22222222-2222-2222-2222-222222222209', 1), -- Cá hồi nướng
                                                          ('33333333-3333-3333-3333-333333333304', '22222222-2222-2222-2222-222222222203', 1), -- Salad
                                                          ('33333333-3333-3333-3333-333333333304', '22222222-2222-2222-2222-222222222215', 1), -- Bò xào lúc lắc
                                                          ('33333333-3333-3333-3333-333333333304', '22222222-2222-2222-2222-222222222227', 2), -- Cơm chiên hải sản
                                                          ('33333333-3333-3333-3333-333333333304', '22222222-2222-2222-2222-222222222237', 2); -- Nước ngọt

-- Combo 5: Combo Nhậu Nhẹ
INSERT INTO combo_items (combo_id, dish_id, quantity) VALUES
                                                          ('33333333-3333-3333-3333-333333333305', '22222222-2222-2222-2222-222222222213', 2), -- Chân gà nướng
                                                          ('33333333-3333-3333-3333-333333333305', '22222222-2222-2222-2222-222222222212', 2), -- Cánh gà nướng
                                                          ('33333333-3333-3333-3333-333333333305', '22222222-2222-2222-2222-222222222205', 1), -- Đậu hũ chiên
                                                          ('33333333-3333-3333-3333-333333333305', '22222222-2222-2222-2222-222222222236', 8); -- Bia

-- Combo 6: Combo Văn Phòng
INSERT INTO combo_items (combo_id, dish_id, quantity) VALUES
                                                          ('33333333-3333-3333-3333-333333333306', '22222222-2222-2222-2222-222222222227', 1), -- Cơm chiên hải sản
                                                          ('33333333-3333-3333-3333-333333333306', '22222222-2222-2222-2222-222222222201', 1), -- Gỏi cuốn
                                                          ('33333333-3333-3333-3333-333333333306', '22222222-2222-2222-2222-222222222237', 1); -- Nước ngọt

-- Combo 7: Combo Chay
INSERT INTO combo_items (combo_id, dish_id, quantity) VALUES
                                                          ('33333333-3333-3333-3333-333333333307', '22222222-2222-2222-2222-222222222205', 1), -- Đậu hũ chiên
                                                          ('33333333-3333-3333-3333-333333333307', '22222222-2222-2222-2222-222222222218', 1), -- Rau muống xào
                                                          ('33333333-3333-3333-3333-333333333307', '22222222-2222-2222-2222-222222222221', 1), -- Bông cải xào nấm
                                                          ('33333333-3333-3333-3333-333333333307', '22222222-2222-2222-2222-222222222230', 2); -- Cơm trắng

-- Combo 8: Combo Tiệc Nhỏ
INSERT INTO combo_items (combo_id, dish_id, quantity) VALUES
                                                          ('33333333-3333-3333-3333-333333333308', '22222222-2222-2222-2222-222222222208', 1), -- Gà nướng
                                                          ('33333333-3333-3333-3333-333333333308', '22222222-2222-2222-2222-222222222224', 1), -- Lẩu bò
                                                          ('33333333-3333-3333-3333-333333333308', '22222222-2222-2222-2222-222222222215', 1), -- Bò xào
                                                          ('33333333-3333-3333-3333-333333333308', '22222222-2222-2222-2222-222222222222', 1), -- Hải sản xào
                                                          ('33333333-3333-3333-3333-333333333308', '22222222-2222-2222-2222-222222222218', 2), -- Rau muống
                                                          ('33333333-3333-3333-3333-333333333308', '22222222-2222-2222-2222-222222222230', 8); -- Cơm trắng

-- Combo 9: Combo Khai Vị
INSERT INTO combo_items (combo_id, dish_id, quantity) VALUES
                                                          ('33333333-3333-3333-3333-333333333309', '22222222-2222-2222-2222-222222222202', 1), -- Nem rán
                                                          ('33333333-3333-3333-3333-333333333309', '22222222-2222-2222-2222-222222222201', 1), -- Gỏi cuốn
                                                          ('33333333-3333-3333-3333-333333333309', '22222222-2222-2222-2222-222222222206', 1), -- Gỏi gà
                                                          ('33333333-3333-3333-3333-333333333309', '22222222-2222-2222-2222-222222222203', 1), -- Salad
                                                          ('33333333-3333-3333-3333-333333333309', '22222222-2222-2222-2222-222222222237', 4); -- Nước ngọt

-- Combo 10: Combo Đặc Sản Tây Bắc
INSERT INTO combo_items (combo_id, dish_id, quantity) VALUES
                                                          ('33333333-3333-3333-3333-333333333310', '22222222-2222-2222-2222-222222222207', 1), -- Lợn cắp nách
                                                          ('33333333-3333-3333-3333-333333333310', '22222222-2222-2222-2222-222222222225', 1), -- Lẩu gà lá é
                                                          ('33333333-3333-3333-3333-333333333310', '22222222-2222-2222-2222-222222222204', 1), -- Chả giò rế
                                                          ('33333333-3333-3333-3333-333333333310', '22222222-2222-2222-2222-222222222230', 4); -- Cơm trắng

-- ===== DONE =====
-- Tổng cộng: 8 danh mục + 40 món ăn + 10 combo
