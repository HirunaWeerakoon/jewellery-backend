-- MySQL DDL for jewellery app (basic)
CREATE DATABASE IF NOT EXISTS jewellerydb;
USE jewellerydb;

CREATE TABLE IF NOT EXISTS products (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  price DECIMAL(12,2) NOT NULL,
  image_url VARCHAR(512),
  stock INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_name VARCHAR(255) NOT NULL,
  customer_email VARCHAR(255) NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL,
  status VARCHAR(50) NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  product_id BIGINT,
  unit_price DECIMAL(12,2) NOT NULL,
  quantity INT NOT NULL,
  subtotal DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS slips (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL UNIQUE,
  file_name VARCHAR(512) NOT NULL,
  file_type VARCHAR(255) NOT NULL,
  file_path VARCHAR(1024) NOT NULL,
  file_size BIGINT NOT NULL,
  uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_slip_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- example seed product
INSERT INTO products (name, description, price, stock) VALUES
('Silver Ring', 'Sterling silver ring', 39.99, 20),
('Gold Necklace', '18k gold plated necklace', 129.99, 5);
