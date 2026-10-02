-- ============================================================
--  LAN Chat Application — Database Setup Script
--  Run this once in MySQL to set up everything the app needs.
--
--  Usage:
--    mysql -u root -p < setup.sql
-- ============================================================

-- 1. Create the database
CREATE DATABASE IF NOT EXISTS LANChat;
USE LANChat;

-- 2. Create the 'users' table
CREATE TABLE IF NOT EXISTS users (
    id       INT          PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- 3. Create the application user (matches DatabaseConnectivity.java)
--    If you prefer to use 'root', you can skip this step.
CREATE USER IF NOT EXISTS 'Shubh'@'localhost' IDENTIFIED BY '12345';
GRANT ALL PRIVILEGES ON LANChat.* TO 'Shubh'@'localhost';
FLUSH PRIVILEGES;

-- Done! The server is ready to run.
SELECT 'Setup complete — LANChat database is ready.' AS Status;
