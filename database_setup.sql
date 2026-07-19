-- 1. Thiết lập Database
CREATE DATABASE IF NOT EXISTS fattie_bakery_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE fattie_bakery_db;

-- 2. Dọn dẹp dữ liệu cũ (để nhập dữ liệu mới)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
TRUNCATE TABLE discount_codes;
SET FOREIGN_KEY_CHECKS = 1;

-- 3. Thêm Danh mục (Categories) mới
INSERT INTO categories
(
    name,
    description,
    image_url,
    active
)
VALUES
    (
        'Bánh Kem',
        'Bánh kem tươi, bánh sinh nhật decor theo yêu cầu',
        '/images/categories/banhkem.jpg',
        TRUE
    ),
    (
        'Bánh Ngọt',
        'Bánh ngọt ăn kèm trà, muffin, cookie',
        '/images/categories/banhngot.jpg',
        TRUE
    ),
    (
        'Trà Thảo Mộc',
        'Trà hoa, trà trái cây thanh mát',
        '/images/categories/trathaomoc.jpg',
        TRUE
    ),
    (
        'Trà Sữa',
        'Trà sữa đậm vị, trân châu nhà làm',
        '/images/categories/trasua.jpg',
        TRUE
    ),
    (
        'Combo Tea-Break',
        'Set trà và bánh cho buổi chiều',
        '/images/categories/combo.jpg',
        TRUE
    );

-- 4. Thêm Sản phẩm (Products) mới - Cấu trúc tương thích hoàn toàn
INSERT INTO products
(
    active,
    age_range,
    color,
    description,
    featured,
    image_url,
    name,
    original_price,
    price,
    size,
    sold_count,
    stock_quantity,
    category_id
)
VALUES
    (
        TRUE,
        'Mọi lứa tuổi',
        NULL,
        'Cốt bánh bông lan mềm mịn, phủ kem tươi và dâu Đà Lạt.',
        TRUE,
        '/images/products/banhkem-dautay.jpg',
        'Bánh Kem Dâu Tây',
        420000,
        350000,
        '16cm',
        45,
        10,
        1
    ),
    (
        TRUE,
        'Mọi lứa tuổi',
        NULL,
        'Bánh kem socola nguyên chất 70%, vị đậm đà.',
        TRUE,
        '/images/products/banhkem-socola.jpg',
        'Bánh Kem Socola',
        450000,
        380000,
        '16cm',
        30,
        8,
        1
    ),
    (
        TRUE,
        'Mọi lứa tuổi',
        NULL,
        'Cookie bơ sữa thơm béo, giòn tan.',
        FALSE,
        '/images/products/cookie-bosua.jpg',
        'Cookie Bơ Sữa',
        60000,
        45000,
        'Gói 200g',
        200,
        100,
        2
    ),
    (
        TRUE,
        'Mọi lứa tuổi',
        NULL,
        'Muffin mềm ẩm với nhân việt quất.',
        TRUE,
        '/images/products/muffin-vietquat.jpg',
        'Muffin Việt Quất',
        45000,
        35000,
        'Size M',
        150,
        50,
        2
    ),
    (
        TRUE,
        'Người lớn',
        NULL,
        'Trà hoa cúc kết hợp mật ong rừng.',
        FALSE,
        '/images/products/tra-hoacuc-matong.jpg',
        'Trà Hoa Cúc Mật Ong',
        55000,
        45000,
        'Ly 500ml',
        300,
        200,
        3
    ),
    (
        TRUE,
        'Người lớn',
        NULL,
        'Trà đào cam sả thanh mát.',
        TRUE,
        '/images/products/tra-dao-camsa.jpg',
        'Trà Đào Cam Sả',
        65000,
        50000,
        'Ly 500ml',
        400,
        150,
        3
    ),
    (
        TRUE,
        'Người lớn',
        NULL,
        'Trà sữa truyền thống cùng trân châu hoàng kim.',
        TRUE,
        '/images/products/trasua-truyenthong.jpg',
        'Trà Sữa Truyền Thống',
        50000,
        40000,
        'Ly 500ml',
        500,
        200,
        4
    ),
    (
        TRUE,
        'Người lớn',
        NULL,
        'Combo trà chiều gồm trà và bánh ngọt.',
        TRUE,
        '/images/products/combo-teabreak.jpg',
        'Set Trà Chiều Fattie',
        120000,
        95000,
        'Set',
        85,
        30,
        5
    );
-- 5. Mã giảm giá (Discount Codes)
INSERT INTO discount_codes (code, discount_type, discount_value, minimum_order_amount, max_usage_count, start_date, end_date, active) VALUES
                                                                                                                                          ('FATTIE10', 'PERCENTAGE', 10, 200000, 100, '2026-01-01', '2026-12-31', TRUE),
                                                                                                                                          ('TRATIEU50K', 'FIXED_AMOUNT', 50000, 300000, 50, '2026-01-01', '2026-12-31', TRUE),
                                                                                                                                          ('BANHNGOT20', 'PERCENTAGE', 20, 500000, 30, '2026-01-01', '2026-12-31', TRUE);

SELECT 'Fattie Bakery Database Initialized Successfully!' AS message;

--6. Create roles admin & user
INSERT INTO roles(name)
VALUES
    ('ROLE_USER'),
    ('ROLE_ADMIN');