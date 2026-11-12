CREATE DATABASE Foodshare_db;
USE Foodshare_db;

show databases;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE donations (
    donation_id INT PRIMARY KEY AUTO_INCREMENT,
    food_name VARCHAR(100) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    donor_username VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

SELECT user_id, full_name, username, password
FROM foodshare_db.users
ORDER BY user_id DESC;

SELECT * FROM FoodShareAppRequests;

