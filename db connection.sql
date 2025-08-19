CREATE DATABASE jewellery;
USE jewellery;
SHOW databases;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    password VARCHAR(255)
);
