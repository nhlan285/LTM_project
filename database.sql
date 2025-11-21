-- ========================================
-- DATABASE SCHEMA FOR FILE CONVERTER SYSTEM
-- ========================================

CREATE DATABASE IF NOT EXISTS file_converter_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE file_converter_db;

-- Tasks Table: Stores conversion job information
CREATE TABLE IF NOT EXISTS tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL COMMENT 'Original uploaded filename',
    file_path_input VARCHAR(500) NOT NULL COMMENT 'Full path to the input DOCX file',
    file_path_output VARCHAR(500) DEFAULT NULL COMMENT 'Full path to the converted PDF file',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'Status: PENDING, PROCESSING, COMPLETED, FAILED',
    error_message TEXT DEFAULT NULL COMMENT 'Error details if conversion fails',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When the task was created',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Users Table (Optional - for authentication, not required for this demo)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert a demo user (Password: 123456)
INSERT INTO users (username, password, email) 
VALUES ('admin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'admin@example.com')
ON DUPLICATE KEY UPDATE username=username;

-- Sample query to check tasks
-- SELECT id, original_filename, status, created_at FROM tasks ORDER BY created_at DESC LIMIT 10;
