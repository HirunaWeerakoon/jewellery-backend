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
    FOREIGN KEY (parent_category_id) REFERENCES categories(category_id)
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

-- ========================================
-- USER MANAGEMENT TABLES
-- ========================================

-- 7. Users Table
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    date_of_birth DATE,
    gender ENUM('Male', 'Female', 'Other'),
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
-- 8. Admin Users Table
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
-- 9. User Addresses Table
CREATE TABLE user_addresses (
    address_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    address_type ENUM('billing', 'shipping', 'both') DEFAULT 'both',
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    company VARCHAR(100),
    address_line1 VARCHAR(200) NOT NULL,
    address_line2 VARCHAR(200),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ========================================
-- SHOPPING & ORDER TABLES
-- ========================================
-- 10. Shopping Cart Table
CREATE TABLE shopping_cart (
    cart_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_product (user_id, product_id)
);

-- 11. Orders Table
CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    user_id INT NOT NULL,
    order_status ENUM('pending', 'processing', 'shipped', 'delivered', 'cancelled', 'refunded') DEFAULT 'pending',
    payment_status ENUM('pending', 'paid', 'failed', 'refunded') DEFAULT 'pending',
    subtotal DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) DEFAULT 0,
    shipping_amount DECIMAL(10,2) DEFAULT 0,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    total_amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    billing_address_id INT NOT NULL,
    shipping_address_id INT NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (billing_address_id) REFERENCES user_addresses(address_id),
    FOREIGN KEY (shipping_address_id) REFERENCES user_addresses(address_id)
);

-- 12. Order Items Table
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

-- ========================================
-- ADDITIONAL TABLES
-- ========================================

-- 13. Wishlist Table
CREATE TABLE wishlist (
    wishlist_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_product_wishlist (user_id, product_id)
);

-- ========================================
-- INDEXES FOR PERFORMANCE
-- ========================================

-- Product search indexes
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_active ON products(is_active);
CREATE INDEX idx_products_featured ON products(featured);
CREATE INDEX idx_products_sku ON products(sku);

-- Material rate updates
CREATE INDEX idx_materials_active ON materials(is_active);
CREATE INDEX idx_material_history_date ON material_rate_history(effective_date);

-- Order management
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(order_status);
CREATE INDEX idx_orders_date ON orders(created_at);

-- Shopping and user experience
CREATE INDEX idx_cart_user ON shopping_cart(user_id);
CREATE INDEX idx_wishlist_user ON wishlist(user_id);

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
    
    -- Get base price and markup
    SELECT base_price, markup_percentage 
    INTO v_base_price, v_markup_percentage
    FROM products 
    WHERE product_id = p_product_id;
    
    -- Calculate material cost
    SELECT COALESCE(SUM(pm.quantity * m.current_rate), 0)
    INTO v_material_cost
    FROM product_materials pm
    JOIN materials m ON pm.material_id = m.material_id
    WHERE pm.product_id = p_product_id AND m.is_active = TRUE;
    
    -- Calculate final price
    SET v_final_price = (v_base_price + v_material_cost) * (1 + v_markup_percentage/100);
    
    RETURN v_final_price;
END //
DELIMITER ;

-- ========================================
-- SAMPLE DATA INSERTION
-- ========================================

-- Insert sample materials
INSERT INTO materials (material_name, current_rate, unit) VALUES
('Gold 24K', 65.50, 'gram'),
('Gold 18K', 49.00, 'gram'),
('Silver 925', 0.85, 'gram'),
('Diamond', 5000.00, 'carat'),
('Platinum', 32.00, 'gram'),
('Rose Gold 14K', 42.00, 'gram');

-- Insert sample categories
INSERT INTO categories (category_name, description) VALUES
('Rings', 'All types of rings including engagement, wedding, and fashion rings'),
('Necklaces', 'Necklaces, chains, and pendants'),
('Earrings', 'Stud earrings, hoops, and drop earrings'),
('Bracelets', 'Tennis bracelets, bangles, and charm bracelets'),
('Watches', 'Luxury and fashion watches');

-- Insert subcategories
INSERT INTO categories (category_name, parent_category_id, description) VALUES
('Engagement Rings', 1, 'Diamond and gemstone engagement rings'),
('Wedding Bands', 1, 'Plain and decorated wedding bands'),
('Fashion Rings', 1, 'Statement and everyday fashion rings');

-- Insert sample admin user
INSERT INTO admin_users (username, email, password_hash, full_name, role) VALUES
('admin', 'admin@jewelrystore.com', '$2y$10$example_hash_here', 'System Administrator', 'super_admin');

-- Insert sample products
INSERT INTO products (product_name, sku, category_id, description, base_price, markup_percentage, weight, stock_quantity) VALUES
('Classic Diamond Engagement Ring', 'ENG001', 1, 'Beautiful solitaire diamond engagement ring in 18K white gold', 500.00, 150.00, 3.5, 5),
('Gold Wedding Band', 'WED001', 1, 'Simple 14K gold wedding band', 200.00, 100.00, 4.2, 10),
('Diamond Stud Earrings', 'EAR001', 3, 'Classic diamond stud earrings in 18K gold', 800.00, 120.00, 2.1, 8);

-- Insert product materials (example for engagement ring)
INSERT INTO product_materials (product_id, material_id, quantity) VALUES
(1, 2, 3.0), -- 3 grams of 18K Gold
(1, 4, 1.0); -- 1 carat Diamond

-- Insert product materials (example for wedding band)
INSERT INTO product_materials (product_id, material_id, quantity) VALUES
(2, 6, 4.0); -- 4 grams of Rose Gold 14K

-- Insert product materials (example for earrings)
INSERT INTO product_materials (product_id, material_id, quantity) VALUES
(3, 2, 2.0), -- 2 grams of 18K Gold
(3, 4, 0.5); -- 0.5 carat total Diamond

-- ========================================
-- ANU'S EDITS
-- ========================================

-- TABLE FOR SLP UPLOADING
CREATE TABLE slips (
    slip_id VARCHAR(10) PRIMARY KEY,
    order_id VARCHAR(10) NOT NULL,
    uploaded_by_user_id VARCHAR(20) UNSIGNED,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(512) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size INT NOT NULL,
    checksum VARCHAR(128),
    notes VARCHAR(512),
    slip_status ENUM('pending', 'slip_uploaded', 'verified') DEFAULT 'pending',
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT fk_order_slips_order
        FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_uploaded_by_user_id
        FOREIGN KEY (uploaded_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
