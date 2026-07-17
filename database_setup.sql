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
INSERT INTO categories (name, description, active) VALUES
                                                       ('Bánh Kem', 'Bánh kem tươi, bánh sinh nhật decor theo yêu cầu', TRUE),
                                                       ('Bánh Ngọt', 'Bánh ngọt ăn kèm trà, các loại muffin, cookie', TRUE),
                                                       ('Trà Thảo Mộc', 'Trà hoa, trà trái cây thanh mát', TRUE),
                                                       ('Trà Sữa', 'Trà sữa đậm vị, trân châu nhà làm', TRUE),
                                                       ('Combo Tea-Break', 'Set trà và bánh cho các buổi chiều', TRUE);

-- 4. Thêm Sản phẩm (Products) mới - Cấu trúc tương thích hoàn toàn
INSERT INTO products (name, description, price, original_price, stock_quantity, category_id, size, age_range, featured, sold_count) VALUES
                                                                                                                                        ('Bánh Kem Dâu Tây', 'Cốt bánh bông lan mềm mịn, dâu tươi Đà Lạt', 350000, 420000, 10, 1, '16cm', 'Mọi lứa tuổi', TRUE, 45),
                                                                                                                                        ('Bánh Kem Socola', 'Socola đen nguyên chất 70%, vị đắng dịu ngọt', 380000, 450000, 8, 1, '16cm', 'Mọi lứa tuổi', TRUE, 30),
                                                                                                                                        ('Cookie Bơ Sữa', 'Bánh quy bơ thơm lừng, giòn tan', 45000, 60000, 100, 2, 'Gói 200g', 'Mọi lứa tuổi', FALSE, 200),
                                                                                                                                        ('Muffin Việt Quất', 'Bánh muffin ẩm mịn với mứt việt quất tươi', 35000, 45000, 50, 2, 'Size M', 'Mọi lứa tuổi', TRUE, 150),
                                                                                                                                        ('Trà Hoa Cúc Mật Ong', 'Thư giãn với trà hoa cúc và mật ong rừng', 45000, 55000, 200, 3, 'Ly 500ml', 'Người lớn', FALSE, 300),
                                                                                                                                        ('Trà Đào Cam Sả', 'Trà đào thanh mát với hương sả thơm dịu', 50000, 65000, 150, 3, 'Ly 500ml', 'Người lớn', TRUE, 400),
                                                                                                                                        ('Trà Sữa Truyền Thống', 'Trà sữa đậm vị, trân châu hoàng kim', 40000, 50000, 200, 4, 'Ly 500ml', 'Người lớn', TRUE, 500),
                                                                                                                                        ('Set Trà Chiều Fattie', 'Combo gồm 1 trà hoa và 2 bánh ngọt nhỏ', 95000, 120000, 30, 5, 'Set', 'Người lớn', TRUE, 85);

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