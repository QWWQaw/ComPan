-- test1.sql
-- 确保在测试前清空表，以防上次测试残留
DROP TABLE IF EXISTS `user`;

-- 使用 H2 兼容 MySQL 模式的语法创建表
CREATE TABLE `user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, -- H2 在 MODE=MYSQL 下的 MySQL 兼容自增主键语法
  `username` VARCHAR(100) NOT NULL UNIQUE,
  `email` VARCHAR(255) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL, -- 注意：您的 User.java 中是 password，而不是 password_hash
  `storage_limit` BIGINT DEFAULT 10737418240,
  `storage_used` BIGINT DEFAULT 0,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);