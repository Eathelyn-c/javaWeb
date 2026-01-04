-- Advertisement Management System Database Schema
-- Created: 2025-11-22

-- Drop existing database if exists
DROP DATABASE IF EXISTS ad_management;

-- Create database
CREATE DATABASE ad_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ad_management;

-- Users table (advertisers)
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    company_name VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Categories table
CREATE TABLE categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default categories
INSERT INTO categories (category_name, description) VALUES
('food', 'Food and beverage advertisements'),
('makeup', 'Cosmetics and beauty products'),
('digital', 'Digital products and electronics'),
('sport', 'Sports equipment and activities'),
('clothes', 'Fashion and clothing'),
('book', 'Books and publications'),
('others', 'Other miscellaneous advertisements');

-- Advertisements table
CREATE TABLE advertisements (
    ad_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    category_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    ad_type ENUM('text', 'image', 'video', 'text_image') NOT NULL,
    text_content TEXT,
    image_url VARCHAR(500),
    video_url VARCHAR(500),
    target_url VARCHAR(500),
    status ENUM('active', 'paused', 'deleted') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    start_date DATETIME,
    end_date DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    INDEX idx_user_id (user_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Advertisement statistics table
CREATE TABLE ad_statistics (
    stat_id INT PRIMARY KEY AUTO_INCREMENT,
    ad_id INT NOT NULL,
    view_count INT DEFAULT 0,
    click_count INT DEFAULT 0,
    last_viewed TIMESTAMP NULL,
    last_clicked TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ad_id) REFERENCES advertisements(ad_id) ON DELETE CASCADE,
    UNIQUE KEY unique_ad_stat (ad_id),
    INDEX idx_ad_id (ad_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- API publish logs table (for tracking API publishing to news, video, shopping platforms)
CREATE TABLE api_publish_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    ad_id INT NOT NULL,
    platform_type ENUM('news', 'video', 'shopping') NOT NULL,
    platform_name VARCHAR(100),
    publish_status ENUM('success', 'failed', 'pending') DEFAULT 'pending',
    request_data TEXT,
    response_data TEXT,
    error_message TEXT,
    published_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ad_id) REFERENCES advertisements(ad_id) ON DELETE CASCADE,
    INDEX idx_ad_id (ad_id),
    INDEX idx_platform_type (platform_type),
    INDEX idx_publish_status (publish_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- View tracking table (detailed view logs)
CREATE TABLE view_logs (
    view_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ad_id INT NOT NULL,
    viewer_ip VARCHAR(45),
    user_agent TEXT,
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ad_id) REFERENCES advertisements(ad_id) ON DELETE CASCADE,
    INDEX idx_ad_id (ad_id),
    INDEX idx_viewed_at (viewed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Click tracking table (detailed click logs)
CREATE TABLE click_logs (
    click_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ad_id INT NOT NULL,
    clicker_ip VARCHAR(45),
    user_agent TEXT,
    clicked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ad_id) REFERENCES advertisements(ad_id) ON DELETE CASCADE,
    INDEX idx_ad_id (ad_id),
    INDEX idx_clicked_at (clicked_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create views for easier querying
CREATE VIEW ad_statistics_view AS
SELECT 
    a.ad_id,
    a.title,
    a.ad_type,
    c.category_name,
    u.username,
    u.company_name,
    COALESCE(s.view_count, 0) as view_count,
    COALESCE(s.click_count, 0) as click_count,
    a.status,
    a.created_at
FROM advertisements a
LEFT JOIN ad_statistics s ON a.ad_id = s.ad_id
JOIN categories c ON a.category_id = c.category_id
JOIN users u ON a.user_id = u.user_id;
