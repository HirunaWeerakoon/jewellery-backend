CREATE DATABASE jewelry_ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE jewelry_ecommerce;

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;



-- ========================================
-- CORE TABLES
-- ========================================



-- 1. Gold Rate History Table (simple time series of gold rates)
CREATE TABLE gold_rate_history (
    history_id INT PRIMARY KEY AUTO_INCREMENT,
    rate DECIMAL(12,4) NOT NULL,         -- price per gram (or your chosen unit)
    effective_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Categories Table
CREATE TABLE categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL,
    category_slug VARCHAR(120) NOT NULL UNIQUE,
    parent_category_id INT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_category_id)
        REFERENCES categories(category_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. categories_closure table (stores all ancestor->descendant pairs)
CREATE TABLE categories_closure (
    ancestor_id INT NOT NULL,
    descendant_id INT NOT NULL,
    depth INT NOT NULL,
    PRIMARY KEY (ancestor_id, descendant_id),
    FOREIGN KEY (ancestor_id) REFERENCES categories(category_id) ON DELETE CASCADE,
    FOREIGN KEY (descendant_id) REFERENCES categories(category_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Products Table
CREATE TABLE products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(200) NOT NULL,
    sku VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    base_price DECIMAL(12,2) NOT NULL DEFAULT 0,
    markup_percentage DECIMAL(6,2) NOT NULL DEFAULT 0,
    weight DECIMAL(9,3),
    dimensions VARCHAR(100),
    stock_quantity INT NOT NULL DEFAULT 0,
    min_stock_level INT DEFAULT 5,
    is_active BOOLEAN DEFAULT TRUE,
    featured BOOLEAN DEFAULT FALSE,
    is_gold BOOLEAN DEFAULT FALSE,
    gold_weight_grams DECIMAL(12,4) DEFAULT 0.0000, -- grams of gold contained
    gold_purity_karat TINYINT NULL               -- e.g., 24, 18, 14 (NULL if not applicable)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Product-Categories Junction Table (many-to-many)
CREATE TABLE product_categories (
    product_id INT NOT NULL,
    category_id INT NOT NULL,
    PRIMARY KEY (product_id, category_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Product Images Table
CREATE TABLE product_images (
    image_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(200),
    is_primary BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



-- =========================================
-- FILTER / ATTRIBUTE TABLES
-- =========================================



-- 7. Attributes Table
CREATE TABLE attributes (
    attribute_id INT PRIMARY KEY AUTO_INCREMENT,
    attribute_name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Attribute Values Table
CREATE TABLE attribute_values (
    value_id INT PRIMARY KEY AUTO_INCREMENT,
    attribute_id INT NOT NULL,
    attribute_value VARCHAR(200) NOT NULL,
    FOREIGN KEY (attribute_id) REFERENCES attributes(attribute_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Product-Attribute Values Junction Table
CREATE TABLE product_attribute_values (
    pav_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    value_id INT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (value_id) REFERENCES attribute_values(value_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



-- ========================================
-- USER MANAGEMENT TABLES
-- ========================================



-- 10. Admin User Table
CREATE TABLE admin_users (
    admin_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    role ENUM('super_admin', 'admin', 'manager', 'staff') DEFAULT 'staff',
    permissions JSON,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



-- ========================================
-- SHOPPING CART & ORDER TABLES
-- ========================================



-- 11. Cart Header Table
CREATE TABLE cart_header (
  cart_header_id INT PRIMARY KEY AUTO_INCREMENT,
  session_id VARCHAR(128) NOT NULL UNIQUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. Cart Items Table
CREATE TABLE cart_items (
    cart_item_id INT PRIMARY KEY AUTO_INCREMENT,
    cart_header_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_header_id) REFERENCES cart_header(cart_header_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE KEY unique_cart_product (cart_header_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 13. Order Status Type Table
CREATE TABLE order_status_types (
  order_status_id INT PRIMARY KEY AUTO_INCREMENT,
  order_status_name ENUM('pending', 'processing', 'shipped', 'delivered', 'cancelled', 'refunded') DEFAULT 'pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 14. Payment Status Type Table
CREATE TABLE payment_status_types (
  payment_status_id INT PRIMARY KEY AUTO_INCREMENT,
  payment_status_name ENUM('pending', 'failed', 'verified', 'refunded') DEFAULT 'pending'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 15. Orders Table
CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    cart_header_id INT NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    user_address VARCHAR(255) NOT NULL,
    telephone_number VARCHAR(20) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    order_status_id INT NOT NULL,
    payment_status_id INT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    tax_amount DECIMAL(12,2) DEFAULT 0,
    shipping_amount DECIMAL(12,2) DEFAULT 0,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_status_id) REFERENCES order_status_types(order_status_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (payment_status_id) REFERENCES payment_status_types(payment_status_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (cart_header_id) REFERENCES cart_header(cart_header_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 16. Order Items Table
CREATE TABLE order_items (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    material_rates_snapshot JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 17. Slips Table
CREATE TABLE slips (
    slip_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size INT NOT NULL,
    checksum VARCHAR(128),
    notes VARCHAR(512),
    payment_status_id INT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT fk_payment_status_id FOREIGN KEY (payment_status_id) REFERENCES payment_status_types(payment_status_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_order_slips_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



-- ========================================
-- REVIEW MANAGEMENT TABLES
-- ========================================



-- 18. Reviews Table
CREATE TABLE reviews (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    reviewer_name VARCHAR(100) NOT NULL,
    reviewer_email VARCHAR(100),
    rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment_text TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_approved BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



-- ========================================
-- INDEXES FOR PERFORMANCE
-- ========================================



-- ====== PRODUCT / CATEGORY / IMAGE / ATTRIBUTE INDEXES ======

-- allow fast lookup by SKU (unique already, but re-declare as explicit index if needed)
CREATE UNIQUE INDEX IF NOT EXISTS uq_products_sku ON products(sku);

-- text search on product name + description (InnoDB fulltext requires MySQL 5.6+)
CREATE FULLTEXT INDEX IF NOT EXISTS ft_products_name_desc ON products(product_name, description);

-- filter by status + price ranges quickly (useful for filtered queries)
CREATE INDEX IF NOT EXISTS idx_products_active_price ON products(is_active, base_price);

-- filters often include gold-specific fields (is_gold + gold_purity + gold_weight)
CREATE INDEX IF NOT EXISTS idx_products_gold_filters ON products(is_gold, gold_purity_karat, gold_weight_grams);

-- product -> images join; quickly find primary image(s)
CREATE INDEX IF NOT EXISTS idx_product_images_product_primary ON product_images(product_id, is_primary, sort_order);

-- product_categories has PK(product_id, category_id) but reverse lookup by category_id needs index
CREATE INDEX IF NOT EXISTS idx_product_categories_category ON product_categories(category_id);

-- product_attribute_values: find products by attribute value quickly
CREATE INDEX IF NOT EXISTS idx_pav_value ON product_attribute_values(value_id);
CREATE INDEX IF NOT EXISTS idx_pav_product ON product_attribute_values(product_id);

-- attribute_values: quickly find all values for an attribute
CREATE INDEX IF NOT EXISTS idx_attribute_values_attribute ON attribute_values(attribute_id);


-- ====== CATEGORY HIERARCHY / CLOSURE INDEXES ======

-- find all ancestors/descendants quickly
CREATE INDEX IF NOT EXISTS idx_categories_closure_descendant ON categories_closure(descendant_id);
CREATE INDEX IF NOT EXISTS idx_categories_closure_ancestor ON categories_closure(ancestor_id);


-- ====== CART / ORDERS / PAYMENTS / SLIPS / REVIEWS INDEXES ======

-- speed up cart lookups by session and items by cart
CREATE INDEX IF NOT EXISTS idx_cart_header_session ON cart_header(session_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart ON cart_items(cart_header_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product ON cart_items(product_id);

-- orders: common filters / listing by date / status / email
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(order_status_id);
CREATE INDEX IF NOT EXISTS idx_orders_payment_status ON orders(payment_status_id);
CREATE INDEX IF NOT EXISTS idx_orders_email ON orders(user_email(100));

-- order_items: lookup by order, and product-based reporting
CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product ON order_items(product_id);

-- slips: find by order and payment_status quickly
CREATE INDEX IF NOT EXISTS idx_slips_order_payment ON slips(order_id, payment_status_id);

-- reviews: show reviews per product and filter by rating
CREATE INDEX IF NOT EXISTS idx_reviews_product ON reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_rating ON reviews(rating);


-- ====== ADMIN / SEARCH / MISC ======

-- admin users lookups
CREATE INDEX IF NOT EXISTS idx_admin_users_email ON admin_users(email(120));

-- gold rate history: queries by date range
CREATE INDEX IF NOT EXISTS idx_gold_rate_effective_date ON gold_rate_history(effective_date);



-- ========================================
-- QUERIES FOR PERFORMANCE
-- ========================================



-- 1a: get product ids for a category (including child categories)
SELECT pc.product_id
FROM product_categories pc
JOIN categories_closure cc ON cc.descendant_id = pc.category_id
WHERE cc.ancestor_id = :categoryId;

-- 2: category + price range + pagination (pageSize + offset)
SELECT p.product_id, p.product_name, p.sku, p.base_price, p.stock_quantity,
       pi.image_url AS primary_image
FROM (
    -- derived table: candidate product ids from category
    SELECT DISTINCT pc.product_id
    FROM product_categories pc
    JOIN categories_closure cc ON cc.descendant_id = pc.category_id
    WHERE cc.ancestor_id = :categoryId
) AS candidate
JOIN products p ON p.product_id = candidate.product_id
LEFT JOIN product_images pi
  ON pi.product_id = p.product_id AND pi.is_primary = TRUE
WHERE p.is_active = 1
  AND p.base_price BETWEEN :minPrice AND :maxPrice
ORDER BY p.base_price ASC, p.product_name
LIMIT :pageSize OFFSET :offset;

-- 3a: products that have ALL selected attribute values (value_ids passed)
-- :valueIds is a comma-separated list or provided as a derived table/temporary table
SELECT p.product_id, p.product_name, p.base_price
FROM products p
JOIN product_attribute_values pav ON pav.product_id = p.product_id
JOIN attribute_values av ON av.value_id = pav.value_id
JOIN (
    SELECT DISTINCT pc.product_id
    FROM product_categories pc
    JOIN categories_closure cc ON cc.descendant_id = pc.category_id
    WHERE cc.ancestor_id = :categoryId
) AS candidate ON candidate.product_id = p.product_id
WHERE p.is_active = 1
  AND p.base_price BETWEEN :minPrice AND :maxPrice
  AND pav.value_id IN (:valueId1, :valueId2, :valueId3) -- selected attribute values
GROUP BY p.product_id
HAVING COUNT(DISTINCT pav.value_id) = :numSelectedValues
ORDER BY p.base_price ASC
LIMIT :pageSize OFFSET :offset;

-- 4.
SELECT p.product_id, p.product_name, p.sku, p.base_price, p.gold_purity_karat,
       pi.image_url AS primary_image
FROM (
    -- candidate products: matched by category, text (optional), and/or preliminary attribute join
    SELECT DISTINCT p.product_id
    FROM products p
    -- join by category
    JOIN product_categories pc ON pc.product_id = p.product_id
    JOIN categories_closure cc ON cc.descendant_id = pc.category_id
    -- text search (optional: fallback to LIKE if fulltext not available)
    WHERE cc.ancestor_id = :categoryId
      AND p.is_active = 1
      AND p.base_price BETWEEN :minPrice AND :maxPrice
      AND (MATCH(p.product_name, p.description) AGAINST (:searchQuery IN NATURAL LANGUAGE MODE)
           OR :searchQuery IS NULL)
) candidate
JOIN products p ON p.product_id = candidate.product_id
LEFT JOIN product_images pi ON pi.product_id = p.product_id AND pi.is_primary = TRUE
WHERE
  -- gold filter (apply only if provided)
  (:filterIsGold IS NULL OR p.is_gold = :filterIsGold)
  AND (:purity IS NULL OR p.gold_purity_karat = :purity)
ORDER BY
  CASE WHEN :sortBy = 'price_asc' THEN p.base_price END ASC,
  CASE WHEN :sortBy = 'price_desc' THEN p.base_price END DESC,
  p.product_name
LIMIT :pageSize OFFSET :offset;

-- 5: fast count using same derived candidate
SELECT COUNT(*) AS total
FROM (
    SELECT DISTINCT pc.product_id
    FROM product_categories pc
    JOIN categories_closure cc ON cc.descendant_id = pc.category_id
    JOIN products p ON p.product_id = pc.product_id
    WHERE cc.ancestor_id = :categoryId
      AND p.is_active = 1
      AND p.base_price BETWEEN :minPrice AND :maxPrice
      -- include attribute conditions here if required (but be careful with GROUP BY)
) t;

-- 6.
SELECT p.product_id, p.product_name, p.sku, pi.image_url
FROM products p
LEFT JOIN product_images pi ON pi.product_id = p.product_id AND pi.is_primary = TRUE
WHERE p.is_active = 1
  AND MATCH(p.product_name, p.description) AGAINST (:q IN NATURAL LANGUAGE MODE)
ORDER BY MATCH(p.product_name, p.description) AGAINST (:q) DESC
LIMIT 20;

--7.
EXPLAIN FORMAT=JSON
<your SELECT ...>;




-- ========================================
-- DYNAMIC PRICING FUNCTION (gold-only; no material table)
-- ========================================



-- Logic:
--  - If product.is_gold = TRUE and gold_weight_grams > 0 and gold_purity_karat IS NOT NULL:
--       material_cost = gold_weight_grams * (gold_purity_karat / 24) * latest_gold_rate
--  - Else material_cost = 0
--  - Final price = (base_price + material_cost) * (1 + markup_percentage/100)


DELIMITER //
CREATE FUNCTION calculate_product_price(p_product_id INT)
RETURNS DECIMAL(12,2)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE v_base_price DECIMAL(12,2) DEFAULT 0;
    DECLARE v_markup_percentage DECIMAL(6,2) DEFAULT 0;
    DECLARE v_is_gold BOOLEAN DEFAULT FALSE;
    DECLARE v_gold_weight DECIMAL(12,4) DEFAULT 0;
    DECLARE v_gold_karat TINYINT DEFAULT NULL;
    DECLARE v_latest_rate DECIMAL(12,4) DEFAULT 0;
    DECLARE v_material_cost DECIMAL(12,2) DEFAULT 0;
    DECLARE v_final_price DECIMAL(12,2);

    SELECT base_price, markup_percentage, is_gold, gold_weight_grams, gold_purity_karat
    INTO v_base_price, v_markup_percentage, v_is_gold, v_gold_weight, v_gold_karat
    FROM products
    WHERE product_id = p_product_id;

    -- get latest gold rate (most recent effective_date). If none, rate = 0
    SELECT COALESCE(rate, 0) INTO v_latest_rate
    FROM gold_rate_history
    WHERE effective_date = (SELECT MAX(effective_date) FROM gold_rate_history)
    LIMIT 1;

    IF v_is_gold AND v_gold_weight > 0 AND v_gold_karat IS NOT NULL THEN
        -- purity factor = karat / 24
        SET v_material_cost = v_gold_weight * (v_gold_karat / 24.0) * v_latest_rate;
    ELSE
        SET v_material_cost = 0;
    END IF;

    SET v_final_price = (v_base_price + v_material_cost) * (1 + v_markup_percentage / 100);

    RETURN ROUND(v_final_price, 2);
END //
DELIMITER ;



-- ========================================
-- SAMPLE DATA (categories + example products)
-- ========================================
-- Populate Order Status Types
INSERT INTO order_status_types (order_status_name) VALUES
('pending'), ('processing'), ('shipped'), ('delivered'), ('cancelled'), ('refunded'), ('verified'), ('paid'); -- Added missing statuses used in code

-- Populate Payment Status Types
INSERT INTO payment_status_types (payment_status_name) VALUES
('pending'), ('failed'), ('verified'), ('refunded'); -- Removed duplicate PaymentStatus


-- 1. Gold Rate History
INSERT INTO gold_rate_history (rate, effective_date) VALUES
(85.2500, '2025-10-01'),
(86.0000, '2025-10-05'),
(87.5000, '2025-10-10');


-- 2. Categories
INSERT INTO categories (category_name, category_slug, parent_category_id, is_active) VALUES
('Jewelry', 'jewelry', NULL, TRUE),
('Rings', 'rings', 1, TRUE),
('Necklaces', 'necklaces', 1, TRUE),
('Earrings', 'earrings', 1, TRUE),
('Gold Rings', 'gold-rings', 2, TRUE),
('Silver Rings', 'silver-rings', 2, TRUE);


-- 3. Categories Closure
INSERT INTO categories_closure (ancestor_id, descendant_id, depth) VALUES
(1, 1, 0),
(1, 2, 1),
(1, 3, 1),
(1, 4, 1),
(2, 2, 0),
(2, 5, 1),
(2, 6, 1),
(5, 5, 0),
(6, 6, 0);


-- 4. Products
INSERT INTO products (product_name, sku, description, base_price, markup_percentage, weight, dimensions, stock_quantity, is_active, featured, is_gold, gold_weight_grams, gold_purity_karat)
VALUES
('Elegant Gold Ring', 'GR001', '18K gold ring with embedded diamonds.', 350.00, 15.00, 12.500, '2x2x1 cm', 20, TRUE, TRUE, TRUE, 12.5, 18),
('Silver Pearl Necklace', 'SN002', 'Sterling silver chain with freshwater pearls.', 200.00, 20.00, 25.000, '20x2x1 cm', 15, TRUE, TRUE, FALSE, 0.0000, NULL),
('Diamond Earrings', 'DE003', 'Pair of diamond stud earrings in 14K gold.', 450.00, 18.00, 8.000, '1x1x1 cm', 10, TRUE, FALSE, TRUE, 8.0, 14),
('Plain Silver Ring', 'SR004', 'Classic 925 sterling silver band.', 50.00, 10.00, 6.000, '2x2x1 cm', 30, TRUE, FALSE, FALSE, 0.0000, NULL);


-- 5. Product-Categories Junction
INSERT INTO product_categories (product_id, category_id) VALUES
(1, 5),
(2, 3),
(3, 4),
(4, 6);


-- 6. Product Images
INSERT INTO product_images (product_id, image_url, alt_text, is_primary, sort_order) VALUES
(1, 'images/products/gold_ring_1.jpg', 'Elegant Gold Ring', TRUE, 1),
(1, 'images/products/gold_ring_2.jpg', 'Gold Ring Side View', FALSE, 2),
(2, 'images/products/silver_necklace.jpg', 'Silver Pearl Necklace', TRUE, 1),
(3, 'images/products/diamond_earrings.jpg', 'Diamond Earrings', TRUE, 1),
(4, 'images/products/silver_ring.jpg', 'Plain Silver Ring', TRUE, 1);


-- 7. Attributes
INSERT INTO attributes (attribute_name) VALUES
('Material'),
('Color'),
('Size'),
('Style');


-- 8. Attribute Values
INSERT INTO attribute_values (attribute_id, attribute_value) VALUES
(1, 'Gold'),
(1, 'Silver'),
(2, 'Yellow'),
(2, 'White'),
(3, 'Small'),
(3, 'Medium'),
(3, 'Large'),
(4, 'Classic'),
(4, 'Modern');


-- 9. Product-Attribute Values
INSERT INTO product_attribute_values (product_id, value_id) VALUES
(1, 1),
(1, 3),
(1, 8),
(2, 2),
(2, 9),
(3, 1),
(3, 4),
(4, 2);


-- 10. Admin Users
INSERT INTO admin_users (username, email, password_hash, full_name, role, permissions)
VALUES
('superadmin', 'admin@example.com', 'hashed_password_123', 'Main Administrator', 'super_admin', '{"manage_users": true, "edit_products": true, "view_orders": true}'),
('jewelry_mgr', 'manager@example.com', 'hashed_password_456', 'Store Manager', 'manager', '{"edit_products": true, "view_orders": true}');


-- 11. Cart Header
INSERT INTO cart_header (session_id) VALUES
('session_abc123'),
('session_def456');


-- 12. Cart Items
INSERT INTO cart_items (cart_header_id, product_id, quantity) VALUES
(1, 1, 1),
(1, 3, 2),
(2, 4, 1);


-- 13. Order Status Types
INSERT INTO order_status_types (order_status_name) VALUES
('pending'), ('processing'), ('shipped'), ('delivered'), ('cancelled'), ('refunded');


-- 14. Payment Status Types
INSERT INTO payment_status_types (payment_status_name) VALUES
('pending'), ('failed'), ('verified'), ('refunded');


-- 15. Orders
INSERT INTO orders (cart_header_id, user_name, user_address, telephone_number, user_email, order_status_id, payment_status_id, subtotal, tax_amount, shipping_amount, discount_amount, total_amount, currency, notes)
VALUES
(1, 'John Doe', '123 Gold Street, Colombo', '+94770000000', 'john@example.com', 2, 3, 1000.00, 50.00, 20.00, 0.00, 1070.00, 'USD', 'Please deliver before 25th Oct.'),
(2, 'Jane Smith', '456 Silver Ave, Kandy', '+94771234567', 'jane@example.com', 1, 1, 50.00, 5.00, 10.00, 0.00, 65.00, 'USD', NULL);


-- 16. Order Items
INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price, material_rates_snapshot)
VALUES
(1, 1, 1, 350.00, 350.00, '{"gold_rate": 87.50, "purity": 18}'),
(1, 3, 2, 450.00, 900.00, '{"gold_rate": 87.50, "purity": 14}'),
(2, 4, 1, 50.00, 50.00, '{"material": "silver"}');


-- 17. Slips
INSERT INTO slips (order_id, file_name, file_path, file_type, file_size, checksum, notes, payment_status_id, verified)
VALUES
(1, 'payment_receipt_1.jpg', 'uploads/slips/payment_receipt_1.jpg', 'image/jpeg', 204800, 'abc123xyz', 'Payment verified successfully.', 3, TRUE),
(2, 'payment_receipt_2.jpg', 'uploads/slips/payment_receipt_2.jpg', 'image/jpeg', 198000, 'def456uvw', 'Pending verification.', 1, FALSE);


-- 18. Reviews
INSERT INTO reviews (product_id, reviewer_name, reviewer_email, rating, comment_text, is_approved)
VALUES
(1, 'Alice', 'alice@example.com', 5, 'Beautiful craftsmanship and quality!', TRUE),
(3, 'Bob', 'bob@example.com', 4, 'Looks elegant, but delivery took a bit long.', TRUE),
(4, 'Catherine', 'cat@example.com', 5, 'Simple and classy silver ring.', TRUE);
