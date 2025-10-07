-- USERS TABLE
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE products (
 product_id VARCHAR(50) PRIMARY KEY,
 name VARCHAR(150) NOT NULL,
 description TEXT,
 base_price DECIMAL(10,2) NOT NULL,
 approx_weight DECIMAL(10,2),
 stone VARCHAR(100),
 product_type ENUM('ring','necklace','bracelet','earring','other') DEFAULT 'other',
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 category_id VARCHAR(100),
 material_id VARCHAR(100),
 FOREIGN KEY (category_id) REFERENCES category(category_id),
 FOREIGN KEY (material_id) REFERENCES material(material_id)
);

-- MATERIAL TABLE
CREATE TABLE material (
    --rose_gold_id =RG1000, gold_id = G2000, silver_id=S3000
    material_id  VARCHAR (100)  ,
    product_id VARCHAR(50),
    name  VARCHAR (100) NOT NULL  ,
    PRIMARY KEY (material_id,product_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- CREATE CATEGORY TABLE
CREATE TABLE category(
   --necklaces_id = N1000 , rings_id = R2000, earrings_id =E3000
   category_id VARCHAR (100) PRIMARY KEY ,
   category_name VARCHAR (100) NOT NULL,
   product_id VARCHAR(50),
    PRIMARY KEY (category_id,product_id),
   FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- PRODUCT VARIANTS (for different materials, sizes, stones)
CREATE TABLE product_variants (
    variant_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    material VARCHAR(100),
    stone VARCHAR(100),
    size VARCHAR(10), -- Only applicable to rings
    additional_price DECIMAL(10,2) DEFAULT 0.00,
    stock INT DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- DELIVERY OPTIONS
CREATE TABLE delivery_options (
    delivery_id INT AUTO_INCREMENT PRIMARY KEY,
    option_name VARCHAR(100) NOT NULL,
    description TEXT,
    cost DECIMAL(10,2) NOT NULL,
    estimated_time VARCHAR(100)
);

-- CART TABLE
CREATE TABLE cart (
    cart_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- CART ITEMS
CREATE TABLE cart_items (
    cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
    cart_id INT NOT NULL,
    variant_id INT NOT NULL,
    quantity INT DEFAULT 1,
    FOREIGN KEY (cart_id) REFERENCES cart(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id) ON DELETE CASCADE
);

-- ORDERS
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    delivery_id INT,
    order_status ENUM('pending','processing','shipped','delivered','cancelled') DEFAULT 'pending',
    total_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (delivery_id) REFERENCES delivery_options(delivery_id)
);

-- ORDER ITEMS
CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    variant_id INT NOT NULL,
    quantity INT DEFAULT 1,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id)
);

-- PAYMENTS
CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('credit_card','debit_card','bank_transfer','cash_on_delivery') NOT NULL,
    payment_status ENUM('pending','completed','failed') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
);

-- WISHLIST
CREATE TABLE wishlist (
    wishlist_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- WISHLIST ITEMS
CREATE TABLE wishlist_items (
    wishlist_item_id INT AUTO_INCREMENT PRIMARY KEY,
    wishlist_id INT NOT NULL,
    variant_id INT NOT NULL,
    FOREIGN KEY (wishlist_id) REFERENCES wishlist(wishlist_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id)
);

-- REVIEWS
CREATE TABLE reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    rating INT CHECK (rating >=1 AND rating <=5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);
