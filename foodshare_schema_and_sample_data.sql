-- FoodShare: complete table definitions (safe to run on an existing database:
-- nothing is dropped and existing tables/data are left untouched).
-- The application also does this automatically on start-up.
USE foodshare_db;

CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS Donations (
    donation_id INT PRIMARY KEY AUTO_INCREMENT,
    food_name VARCHAR(100) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    donor_username VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',   -- AVAILABLE / DISTRIBUTED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS FoodShareAppRequests (
    request_id VARCHAR(20) PRIMARY KEY,
    recipient_name VARCHAR(100) NOT NULL,
    food_required VARCHAR(100) NOT NULL,
    quantity_required INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',      -- PENDING / FULFILLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS FoodShareContacts (
    contact_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    contact_type VARCHAR(20) NOT NULL,                  -- DONOR / RECIPIENT
    address VARCHAR(255), phone VARCHAR(30), email VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS FoodShareInventory (
    inventory_id INT PRIMARY KEY AUTO_INCREMENT,
    donor_id INT NOT NULL,
    food_name VARCHAR(100) NOT NULL,
    quantity_available INT NOT NULL
);

CREATE TABLE IF NOT EXISTS FoodShareNeeds (
    need_id INT PRIMARY KEY AUTO_INCREMENT,
    recipient_id INT NOT NULL,
    food_name VARCHAR(100) NOT NULL,
    quantity_required INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'       -- PENDING / FULFILLED
);

CREATE TABLE IF NOT EXISTS FoodShareProcessedHistory (
    history_id INT PRIMARY KEY AUTO_INCREMENT,
    item_type VARCHAR(20), item_id VARCHAR(30), food_name VARCHAR(100),
    quantity INT, matched_name VARCHAR(100),
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20)
);

-- Only needed if your Donations table was created before this update:
-- ALTER TABLE Donations ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE';

-- ---------------------------------------------------------------
-- OPTIONAL sample data (run ONLY if FoodShareContacts is empty)
-- ---------------------------------------------------------------
-- INSERT INTO FoodShareContacts (full_name, contact_type, address, phone, email) VALUES
--  ('Green Valley Restaurant','DONOR','12 MG Road, Bengaluru','9800000001','greenvalley@example.com'),
--  ('City Bakery','DONOR','45 Church Street, Bengaluru','9800000002','citybakery@example.com'),
--  ('Hope Shelter','RECIPIENT','8 Indiranagar, Bengaluru','9800000011','hope@example.com'),
--  ('Sunrise Orphanage','RECIPIENT','21 Koramangala, Bengaluru','9800000012','sunrise@example.com');
-- INSERT INTO FoodShareInventory (donor_id, food_name, quantity_available) VALUES (1,'Rice',50),(2,'Bread',30);
-- INSERT INTO FoodShareNeeds (recipient_id, food_name, quantity_required) VALUES (3,'Rice',20),(4,'Bread',10);
