-- ============================================================
--  Mini Banking System — Database Schema
--  Database  : MySQL
--  File      : schema.sql
--
--  Run this file once to create the database and all tables:
--    mysql -u root -p < schema.sql
-- ============================================================

-- 1. Create and select the database
CREATE DATABASE IF NOT EXISTS mini_banking_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE mini_banking_db;

-- ============================================================
-- TABLE: customers
-- Stores registered bank customers.
-- ============================================================
CREATE TABLE IF NOT EXISTS customers (
    customer_id   INT            NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100)   NOT NULL,
    email         VARCHAR(100)   NOT NULL UNIQUE,
    phone         VARCHAR(15)    NOT NULL,
    address       TEXT           NOT NULL,
    date_of_birth DATE           NOT NULL,
    password      VARCHAR(255)   NOT NULL,   -- store hashed in production
    created_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (customer_id)
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: accounts
-- Stores bank accounts linked to customers (one per customer for this demo).
-- ============================================================
CREATE TABLE IF NOT EXISTS accounts (
    account_id     INT              NOT NULL AUTO_INCREMENT,
    account_number VARCHAR(10)      NOT NULL UNIQUE,
    customer_id    INT              NOT NULL,
    account_type   ENUM('SAVINGS', 'CURRENT') NOT NULL DEFAULT 'SAVINGS',
    balance        DECIMAL(15, 2)   NOT NULL DEFAULT 0.00,
    status         ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (account_id),
    CONSTRAINT fk_account_customer
        FOREIGN KEY (customer_id) REFERENCES customers (customer_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- TABLE: transactions
-- Records every financial operation (deposit, withdrawal, transfer).
-- ============================================================
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id   INT              NOT NULL AUTO_INCREMENT,
    account_id       INT              NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER') NOT NULL,
    amount           DECIMAL(15, 2)   NOT NULL,
    reference_id     INT              DEFAULT NULL,   -- counterpart account_id for transfers
    description      VARCHAR(255)     DEFAULT NULL,
    status           ENUM('SUCCESS', 'FAILED') NOT NULL DEFAULT 'SUCCESS',
    transaction_date TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (transaction_id),
    CONSTRAINT fk_transaction_account
        FOREIGN KEY (account_id) REFERENCES accounts (account_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- Useful indices for faster lookups
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_accounts_customer_id
    ON accounts (customer_id);

CREATE INDEX IF NOT EXISTS idx_transactions_account_id
    ON transactions (account_id);

-- ============================================================
-- Verify schema
-- ============================================================
SHOW TABLES;
