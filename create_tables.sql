-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS bankdb;

-- Use the database
USE bankdb;

-- Drop existing tables to start fresh (with proper order due to foreign key constraints)
DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS app_user;
DROP TABLE IF EXISTS user_role;

-- Create the customer table
CREATE TABLE customer
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    contactInfo VARCHAR(255)
) ENGINE = InnoDB;

-- Create the account table with a foreign key to customer
CREATE TABLE account
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    accountNumber VARCHAR(255)   NOT NULL UNIQUE,
    balance       DECIMAL(15, 2) NOT NULL,
    interestRate  DECIMAL(5, 4)  NOT NULL,
    customer_id   BIGINT,
    FOREIGN KEY (customer_id) REFERENCES customer (id)
) ENGINE = InnoDB;

-- Create the app_user table
CREATE TABLE app_user
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
) ENGINE = InnoDB;

-- Create the user_role table to manage user roles
CREATE TABLE user_role
(
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    app_user_id BIGINT      NOT NULL,
    role_name    VARCHAR(50) NOT NULL,
    FOREIGN KEY (app_user_id) REFERENCES app_user (id)
) ENGINE = InnoDB;

-- Create a database user for the application
-- Note: In a real environment, use a more secure password!
CREATE USER IF NOT EXISTS 'bankuser'@'localhost' IDENTIFIED BY 'bankpassword';
GRANT ALL PRIVILEGES ON bankdb.* TO 'bankuser'@'localhost';
FLUSH PRIVILEGES;

-- Insert some sample data
INSERT INTO customer (name, contactInfo)
VALUES ('John Doe', 'john.doe@example.com');
INSERT INTO customer (name, contactInfo)
VALUES ('Jane Smith', 'jane.smith@example.com');

INSERT INTO account (accountNumber, balance, interestRate, customer_id)
VALUES ('ACC001', 5000.00, 0.0250, 1);
INSERT INTO account (accountNumber, balance, interestRate, customer_id)
VALUES ('ACC002', 12500.00, 0.0300, 2);

INSERT INTO app_user (username, password)
VALUES ('admin', 'adminpass');

INSERT INTO user_role (app_user_id, role_name)
VALUES (1, 'ADMIN');

INSERT INTO user_role (app_user_id, role_name)
VALUES (1, 'USER');

INSERT INTO app_user (username, password)
VALUES ('user', 'userpass');

INSERT INTO user_role (app_user_id, role_name)
VALUES (2, 'USER');