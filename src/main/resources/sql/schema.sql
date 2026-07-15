-- Personal Accounting Dashboard Database Schema

-- Create database
CREATE DATABASE IF NOT EXISTS account_dashboard CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE account_dashboard;

-- User table
CREATE TABLE IF NOT EXISTS `user` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT 'User ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Username',
  `password` VARCHAR(255) NOT NULL COMMENT 'Password',
  `email` VARCHAR(100) COMMENT 'Email',
  `phone` VARCHAR(20) COMMENT 'Phone',
  `budget` DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Monthly budget',
  `avatar` VARCHAR(255) COMMENT 'Avatar URL',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  INDEX idx_username (`username`),
  INDEX idx_email (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User table';

-- Category table
CREATE TABLE IF NOT EXISTS `category` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Category ID',
  `name` VARCHAR(50) NOT NULL COMMENT 'Category name',
  `type` ENUM('income', 'expense') NOT NULL COMMENT 'Type: income/expense',
  `icon` VARCHAR(50) COMMENT 'Icon',
  `color` VARCHAR(20) COMMENT 'Color',
  `user_id` INT COMMENT 'User ID (NULL for system categories)',
  `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  INDEX idx_type (`type`),
  INDEX idx_user_id (`user_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Category table';

-- Bill table
CREATE TABLE IF NOT EXISTS `bill` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT 'Bill ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT 'Amount',
  `type` ENUM('income', 'expense') NOT NULL COMMENT 'Type: income/expense',
  `category_id` INT NOT NULL COMMENT 'Category ID',
  `user_id` INT NOT NULL COMMENT 'User ID',
  `date` DATE NOT NULL COMMENT 'Bill date',
  `remark` VARCHAR(500) COMMENT 'Remark',
  `images` TEXT COMMENT 'Images (JSON)',
  `location` VARCHAR(255) COMMENT 'Location',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
  INDEX idx_user_id (`user_id`),
  INDEX idx_category_id (`category_id`),
  INDEX idx_date (`date`),
  INDEX idx_type (`type`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`category_id`) REFERENCES `category`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bill table';

-- Insert system preset categories
INSERT INTO `category` (`name`, `type`, `icon`, `color`, `user_id`, `sort_order`) VALUES
('餐饮', 'expense', 'food', '#FF6B6B', NULL, 1),
('购物', 'expense', 'shopping', '#4ECDC4', NULL, 2),
('交通', 'expense', 'transport', '#45B7D1', NULL, 3),
('娱乐', 'expense', 'entertainment', '#FFA07A', NULL, 4),
('医疗', 'expense', 'medical', '#98D8C8', NULL, 5),
('学习', 'expense', 'education', '#F7DC6F', NULL, 6),
('住房', 'expense', 'housing', '#BB8FCE', NULL, 7),
('其他支出', 'expense', 'other', '#95A5A6', NULL, 8),
('工资', 'income', 'salary', '#2ECC71', NULL, 1),
('奖金', 'income', 'bonus', '#F1C40F', NULL, 2),
('投资', 'income', 'investment', '#3498DB', NULL, 3),
('兼职', 'income', 'parttime', '#E74C3C', NULL, 4),
('其他收入', 'income', 'other', '#9B59B6', NULL, 5);

-- Insert test users (password encrypted with MD5)
-- testuser password: 123456 -> e10adc3949ba59abbe56e057f20f883e
-- admin password: admin123 -> 0192023a7bbd73250516f069df18b500
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `budget`) VALUES
('testuser', 'e10adc3949ba59abbe56e057f20f883e', 'test@example.com', '13800138000', 5000.00),
('admin', '0192023a7bbd73250516f069df18b500', 'admin@example.com', '13900139000', 10000.00);

-- Insert test bills
INSERT INTO `bill` (`amount`, `type`, `category_id`, `user_id`, `date`, `remark`) VALUES
(35.50, 'expense', 1, 1, '2025-05-01', '午餐'),
(128.00, 'expense', 2, 1, '2025-05-02', '买衣服'),
(25.00, 'expense', 3, 1, '2025-05-03', '地铁'),
(200.00, 'expense', 4, 1, '2025-05-05', '电影票'),
(5000.00, 'income', 9, 1, '2025-05-01', '五月工资'),
(45.00, 'expense', 1, 1, '2025-06-01', '晚餐'),
(299.00, 'expense', 2, 1, '2025-06-03', '网购'),
(15.00, 'expense', 3, 1, '2025-06-05', '公交'),
(8000.00, 'income', 9, 1, '2025-06-01', '六月工资'),
(150.00, 'expense', 4, 1, '2025-06-08', 'KTV'),
(8200.00, 'income', 9, 1, '2026-01-05', '一月工资'),
(360.00, 'expense', 1, 1, '2026-01-08', '朋友聚餐'),
(120.00, 'expense', 3, 1, '2026-02-06', '打车和地铁'),
(8500.00, 'income', 9, 1, '2026-02-10', '二月工资'),
(560.00, 'expense', 2, 1, '2026-03-12', '春季购物'),
(180.00, 'expense', 4, 1, '2026-03-18', '演唱会门票'),
(8600.00, 'income', 9, 1, '2026-04-10', '四月工资'),
(260.00, 'expense', 6, 1, '2026-04-16', '课程资料'),
(420.00, 'expense', 7, 1, '2026-05-09', '房租水电'),
(3000.00, 'income', 12, 1, '2026-05-20', '兼职项目'),
(210.00, 'expense', 5, 1, '2026-06-04', '体检'),
(680.00, 'expense', 2, 1, '2026-06-06', '618 购物'),
(8300.00, 'income', 9, 1, '2026-06-10', '六月工资');
