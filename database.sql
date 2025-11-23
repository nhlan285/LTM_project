CREATE DATABASE IF NOT EXISTS file_converter_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE file_converter_db;

CREATE TABLE IF NOT EXISTS upload_batches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    total_files INT NOT NULL,
    completed_files INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'Status of the overall batch',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    batch_id INT DEFAULT NULL COMMENT 'Foreign key to upload_batches.id when uploading multiple files',
    sequence_order INT DEFAULT NULL COMMENT 'Original order inside the batch',
    display_name VARCHAR(255) DEFAULT NULL COMMENT 'File name shown to user (can differ from stored name)',
    original_filename VARCHAR(255) NOT NULL COMMENT 'Original uploaded filename',
    file_path_input VARCHAR(500) NOT NULL COMMENT 'Full path to the input DOCX file',
    file_path_output VARCHAR(500) DEFAULT NULL COMMENT 'Full path to the converted PDF file',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'Status: PENDING, PROCESSING, COMPLETED, FAILED',
    error_message TEXT DEFAULT NULL COMMENT 'Error details if conversion fails',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'When the task was created',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    FOREIGN KEY (batch_id) REFERENCES upload_batches(id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_batch (batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

