 CREATE DATABASE jewelry_ecommerce;
 USE jewelry_ecommerce;

-- ========================================
-- CORE TABLES
-- ========================================

-- 1. Materials Table
CREATE TABLE materials (
    material_id INT PRIMARY KEY AUTO_INCREMENT,
    material_name VARCHAR(100) NOT NULL UNIQUE,
    current_rate DECIMAL(10,4) NOT NULL, -- Price per gram/unit
    unit VARCHAR(20) NOT NULL DEFAULT 'gram', -- gram, ounce, carat, etc.
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Material Rate History Table
CREATE TABLE material_rate_history (
    history_id INT PRIMARY KEY AUTO_INCREMENT,
    material_id INT NOT NULL,
    rate DECIMAL(10,4) NOT NULL,
    effective_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (material_id) REFERENCES materials(material_id)
);

-- 3. Categories Table
CREATE TABLE categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL,
    parent_category_id INT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_category_id) REFERENCES categories(category_id) ON DELETE SET NULL
);

-- 4. Products Table
CREATE TABLE products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(200) NOT NULL,
    sku VARCHAR(100) UNIQUE NOT NULL,
    category_id INT NOT NULL,
    description TEXT,
    base_price DECIMAL(10,2) NOT NULL DEFAULT 0, -- Base manufacturing cost
    markup_percentage DECIMAL(5,2) NOT NULL DEFAULT 0, -- Profit margin
    weight DECIMAL(8,3), -- Total weight in grams
    dimensions VARCHAR(100), -- L x W x H
    stock_quantity INT NOT NULL DEFAULT 0,
    min_stock_level INT DEFAULT 5,
    is_active BOOLEAN DEFAULT TRUE,
    featured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

-- 5. Product Materials Junction Table
CREATE TABLE product_materials (
    product_material_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    material_id INT NOT NULL,
    quantity DECIMAL(8,3) NOT NULL, -- Quantity of material used (in material's unit)
    percentage DECIMAL(5,2), -- Percentage of total product weight
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES materials(material_id),
    UNIQUE KEY unique_product_material (product_id, material_id)
);

-- 6. Product Images Table
CREATE TABLE product_images (
    image_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(200),
    is_primary BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);



-- =========================================
-- TABLES FOR SEARCH BAR IMPLEMENTATION
-- =========================================



-- 7. Master table for attribute definitions
CREATE TABLE attributes (
    attribute_id INT PRIMARY KEY AUTO_INCREMENT,
    attribute_name VARCHAR(100) NOT NULL UNIQUE
);

-- 8. Table for possible values for each attribute
CREATE TABLE attribute_values (
    value_id INT PRIMARY KEY AUTO_INCREMENT,
    attribute_id INT NOT NULL,
    attribute_value VARCHAR(255) NOT NULL,
    FOREIGN KEY (attribute_id) REFERENCES attributes(attribute_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- 9. Junction table linking products with specific attribute values
CREATE TABLE product_attribute_values (
    product_id INT NOT NULL,
    value_id INT NOT NULL,
    PRIMARY KEY (product_id, value_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (value_id) REFERENCES attribute_values(value_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);



-- ========================================
-- USER MANAGEMENT TABLES
-- ========================================



-- 10. Admin Users Table
CREATE TABLE admin_users (
    admin_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    role ENUM('super_admin', 'admin', 'manager', 'staff') DEFAULT 'staff',
    permissions JSON, -- Store specific permissions
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



-- ========================================
-- SHOPPING CART & ORDER TABLES
-- ========================================



-- 11. Cart Header Table: one cart per session
CREATE TABLE cart_header (
  cart_header_id INT PRIMARY KEY AUTO_INCREMENT,
  session_id VARCHAR(128) NOT NULL UNIQUE,  -- each browser session gets a unique ID
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)ENGINE=InnoDB;

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
)ENGINE=InnoDB;

-- 13. Order Status Type Table
CREATE TABLE order_status_types (
  order_status_id INT PRIMARY KEY AUTO_INCREMENT,
  order_status_name ENUM('pending', 'processing', 'shipped', 'delivered', 'cancelled', 'refunded') DEFAULT 'pending'
);

-- 14. Payment Status Type Table
CREATE TABLE payment_status_types (
  payment_status_id INT PRIMARY KEY AUTO_INCREMENT,
  payment_status_name ENUM('pending', 'failed', 'verified', 'refunded') DEFAULT 'pending'
);

-- 15. Orders Table
CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    session_id INT NOT NULL,
    user_name VARCHAR(100) NOT NULL,
    user_address VARCHAR(255) NOT NULL,
    telephone_number VARCHAR(20) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    order_status_id INT NOT NULL,
    payment_status_id INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    shipping_amount DECIMAL(10,2) DEFAULT 0,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_status_id) REFERENCES order_status_types(order_status_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    FOREIGN KEY (payment_status_id) REFERENCES payment_status_types(payment_status_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    FOREIGN KEY (session_id) REFERENCES cart_header(cart_header_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
)ENGINE=InnoDB;


-- 16. Order Items Table
CREATE TABLE order_items (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL, -- Price at time of order
    total_price DECIMAL(10,2) NOT NULL,
    material_rates_snapshot JSON, -- Store material rates at time of order
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- 17. Slips Table (TABLE FOR SLP UPLOADING)
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
    CONSTRAINT fk_payment_status_id
        FOREIGN KEY (payment_status_id) REFERENCES payment_status_types(payment_status_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_order_slips_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id)
        ON DELETE CASCADE ON UPDATE CASCADE
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
    FOREIGN KEY (product_id) REFERENCES products(product_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);



-- ========================================
-- INDEXES FOR PERFORMANCE
-- ========================================



-- 1. Products Table
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_active ON products(is_active);
CREATE INDEX idx_products_featured ON products(featured);
CREATE INDEX idx_products_sku ON products(sku);

-- 2. Materials Table
CREATE INDEX idx_materials_active ON materials(is_active);

-- 3. Material Rate History
CREATE INDEX idx_material_history_date ON material_rate_history(effective_date);

-- 4. Orders Table
CREATE INDEX idx_orders_status ON orders(order_status_id);
CREATE INDEX idx_orders_payment_status ON orders(payment_status_id);
CREATE INDEX idx_orders_session ON orders(session_id);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- 5. Shopping Cart Table
CREATE INDEX idx_cart_user ON cart_header(session_id);

-- 6. Product Materials
CREATE INDEX idx_product_materials_product ON product_materials(product_id);
CREATE INDEX idx_product_materials_material ON product_materials(material_id);

-- 7. Reviews Table
CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);



-- ========================================
-- DYNAMIC PRICING FUNCTION
-- ========================================



DELIMITER //
CREATE FUNCTION calculate_product_price(p_product_id INT)
RETURNS DECIMAL(10,2)
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE v_base_price DECIMAL(10,2) DEFAULT 0;
    DECLARE v_markup_percentage DECIMAL(5,2) DEFAULT 0;
    DECLARE v_material_cost DECIMAL(10,2) DEFAULT 0;
    DECLARE v_final_price DECIMAL(10,2);

    -- Get base price and markup from products table
    SELECT base_price, markup_percentage
    INTO v_base_price, v_markup_percentage
    FROM products
    WHERE product_id = p_product_id;

    -- Calculate total material cost using only active materials
    SELECT COALESCE(SUM(pm.quantity * m.current_rate), 0)
    INTO v_material_cost
    FROM product_materials pm
    JOIN materials m ON pm.material_id = m.material_id
    WHERE pm.product_id = p_product_id
      AND m.is_active = TRUE;

    -- Final price calculation
    SET v_final_price = (v_base_price + v_material_cost) * (1 + v_markup_percentage / 100);

    RETURN v_final_price;
END //
DELIMITER ;



-- ========================================
-- SAMPLE DATA INSERTION
-- ========================================



-- 1. Insert sample materials
INSERT INTO materials (material_name, current_rate, unit) VALUES
('Gold 24K', 65.50, 'gram'),
('Gold 18K', 49.00, 'gram'),
('Silver 925', 0.85, 'gram'),
('Diamond', 5000.00, 'carat'),
('Platinum', 32.00, 'gram'),
('Rose Gold 14K', 42.00, 'gram');


-- Insert sample categories

-- 2. Main categories
INSERT INTO categories (category_name, description) VALUES
('Rings', 'All types of rings including engagement, wedding, and fashion rings'),
('Necklaces', 'Necklaces, chains, and pendants'),
('Earrings', 'Stud earrings, hoops, and drop earrings'),
('Bracelets', 'Tennis bracelets, bangles, and charm bracelets'),
('Watches', 'Luxury and fashion watches');

-- 3. Subcategories
INSERT INTO categories (category_name, parent_category_id, description) VALUES
('Engagement Rings', 1, 'Diamond and gemstone engagement rings'),
('Wedding Bands', 1, 'Plain and decorated wedding bands'),
('Fashion Rings', 1, 'Statement and everyday fashion rings');

-- 4. Insert sample admin user
INSERT INTO admin_users (username, email, password_hash, full_name, role) VALUES
('admin', 'admin@jewelrystore.com', '$2y$10$example_hash_here', 'System Administrator', 'super_admin');

-- 5. Insert sample products
INSERT INTO products (product_name, sku, category_id, description, base_price, markup_percentage, weight, stock_quantity) VALUES
('Classic Diamond Engagement Ring', 'ENG001', 6, 'Beautiful solitaire diamond engagement ring in 18K white gold', 500.00, 150.00, 3.5, 5),
('Gold Wedding Band', 'WED001', 7, 'Simple 14K gold wedding band', 200.00, 100.00, 4.2, 10),
('Diamond Stud Earrings', 'EAR001', 3, 'Classic diamond stud earrings in 18K gold', 800.00, 120.00, 2.1, 8);

-- Insert product materials

-- 6. Engagement Ring
INSERT INTO product_materials (product_id, material_id, quantity) VALUES
(1, 2, 3.0),  -- 3 grams of 18K Gold
(1, 4, 1.0);  -- 1 carat Diamond

-- 7. Wedding Band
INSERT INTO product_materials (product_id, material_id, quantity) VALUES
(2, 6, 4.0);  -- 4 grams of Rose Gold 14K

-- 8. Earrings
INSERT INTO product_materials (product_id, material_id, quantity) VALUES
(3, 2, 2.0),  -- 2 grams of 18K Gold
(3, 4, 0.5);  -- 0.5 carat Diamond


-- Attributes for search bar

-- 9. Attributes
INSERT INTO attributes (attribute_name) VALUES
('Metal'),
('Stone'),
('Ring Size'),
('Style');

-- 10. Attribute Values
INSERT INTO attribute_values (attribute_id, attribute_value) VALUES
(1, 'Gold 18K'),
(1, 'Rose Gold 14K'),
(1, 'Silver 925'),
(2, 'Diamond'),
(2, 'Emerald'),
(3, '5'),
(3, '6'),
(3, '7'),
(4, 'Engagement'),
(4, 'Wedding'),
(4, 'Fashion');


-- Links products to attribute values

-- 11. Classic Diamond Engagement Ring
INSERT INTO product_attribute_values (product_id, value_id) VALUES
(1, 1), -- Gold 18K
(1, 4), -- Diamond
(1, 6), -- Ring Size 5
(1, 10); -- Style: Engagement

-- 12. Gold Wedding Band
INSERT INTO product_attribute_values (product_id, value_id) VALUES
(2, 2), -- Rose Gold 14K
(2, 4), -- Diamond (optional: maybe plain, depends on your sample)
(2, 7), -- Ring Size 6
(2, 11); -- Style: Wedding

-- 13. Diamond Stud Earrings
INSERT INTO product_attribute_values (product_id, value_id) VALUES
(3, 1), -- Gold 18K
(3, 4), -- Diamond
(3, 9), -- Ring Size 7 (or can be null if not applicable)
(3, 12); -- Style: Fashion


-- ========================================
-- USEFUL QUERIES
-- ========================================
-- WHERE p.product_id = 1;
-- Query to get current product prices
-- SELECT 
--     p.product_name,
--     p.sku,
--     calculate_product_price(p.product_id) as current_price,
--     p.stock_quantity
-- FROM products p 
-- WHERE p.is_active = TRUE;

-- Query to get product with materials breakdown
-- SELECT 
--     p.product_name,
--     m.material_name,
--     pm.quantity,
--     m.current_rate,
--     (pm.quantity * m.current_rate) as material_cost
-- FROM products p
-- JOIN product_materials pm ON p.product_id = pm.product_id
-- JOIN materials m ON pm.material_id = m.material_id
-- WHERE p.product_id = 1;
