-- mysql or mariadb
CREATE DATABASE IF NOT EXISTS kepan
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE kepan;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (  
  user_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',  
  username VARCHAR(100) NOT NULL COMMENT '用户名',  
  email VARCHAR(255) NOT NULL COMMENT '邮箱',  
  password_hash VARCHAR(255) NOT NULL COMMENT 'hash密码',  
  storage_limit BIGINT NOT NULL DEFAULT 10737418240 COMMENT '存储空间上限(10GB)',  
  storage_used BIGINT NOT NULL DEFAULT 0 COMMENT '已用存储空间(由应用层维护)',  
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',  
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',  
  PRIMARY KEY (user_id),  
  UNIQUE KEY uk_username (username),  
  UNIQUE KEY uk_email (email)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 用户组与成员表 
CREATE TABLE IF NOT EXISTS user_group (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 用户组与成员表
CREATE TABLE IF NOT EXISTS user_group_member (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    group_id BIGINT UNSIGNED NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'member',
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES user_group(id) ON DELETE CASCADE,
    UNIQUE KEY (user_id, group_id)
);


CREATE TABLE IF NOT EXISTS `storage_object` (  
  `hash` VARCHAR(255) NOT NULL COMMENT '哈希 (SHA256), 作为物理文件的唯一标识',  
  `size` BIGINT NOT NULL COMMENT '文件大小(Bytes)',  
  storage_path VARCHAR(1024) NOT NULL COMMENT '文件在服务器上的物理存储路径',  
  ref_count INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '引用计数',  
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次上传时间',  
  PRIMARY KEY (`hash`)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物理存储对象表 (用于去重和秒传)';  

    
-- 文件夹表：增加软删除  
CREATE TABLE IF NOT EXISTS `folder` (  
  folder_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文件夹ID',  
  owner_id BIGINT UNSIGNED NOT NULL COMMENT '所有者ID',  
  parent_folder_id BIGINT UNSIGNED NULL COMMENT '父文件夹ID (NULL表示根目录)',  
  folder_name VARCHAR(255) NOT NULL COMMENT '文件夹名称',  
  `size` BIGINT NOT NULL DEFAULT 0 COMMENT '文件夹总大小(由应用层或触发器维护)',  
  `status` ENUM('active', 'deleted') NOT NULL DEFAULT 'active' COMMENT '状态',  
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',  
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',  
  deleted_at TIMESTAMP NULL COMMENT '删除时间',  
  PRIMARY KEY (folder_id),  
  FOREIGN KEY (owner_id) REFERENCES user(user_id) ON DELETE CASCADE,  
  FOREIGN KEY (parent_folder_id) REFERENCES folder(folder_id) ON DELETE CASCADE,  
  UNIQUE KEY uk_folder_name_in_parent (parent_folder_id, folder_name, owner_id)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件夹信息表';  

-- 文件表
CREATE TABLE IF NOT EXISTS `file` (  
  file_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文件ID (逻辑ID)',  
  uploader_id BIGINT UNSIGNED NOT NULL COMMENT '上传者ID',  
  folder_id BIGINT UNSIGNED NOT NULL COMMENT '所属文件夹ID',  
  file_name VARCHAR(255) NOT NULL COMMENT '文件名 (用户自定义)',  
  mime_type VARCHAR(128) NOT NULL COMMENT 'MIME类型',  
  object_hash VARCHAR(255) NOT NULL COMMENT '关联的物理文件哈希',  
  `status` ENUM('active', 'deleted') NOT NULL DEFAULT 'active' COMMENT '状态',  
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',  
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',  
  deleted_at TIMESTAMP NULL COMMENT '删除时间',  
  PRIMARY KEY (file_id),  
  FOREIGN KEY (uploader_id) REFERENCES user(user_id) ON DELETE CASCADE,  
  FOREIGN KEY (folder_id) REFERENCES folder(folder_id) ON DELETE CASCADE,  
  FOREIGN KEY (object_hash) REFERENCES storage_object(hash),  
  UNIQUE KEY uk_file_name_in_folder (folder_id, file_name, uploader_id)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件逻辑信息表';  

-- ACL表：访问控制列表
CREATE TABLE IF NOT EXISTS acl (
    id INT PRIMARY KEY AUTO_INCREMENT,
    file_id BIGINT UNSIGNED DEFAULT NULL,
    folder_id BIGINT UNSIGNED DEFAULT NULL,
    user_id BIGINT UNSIGNED DEFAULT NULL,
    group_id BIGINT UNSIGNED DEFAULT NULL,
    permission VARCHAR(100) NOT NULL,
    FOREIGN KEY (file_id) REFERENCES file(file_id) ON DELETE CASCADE,
    FOREIGN KEY (folder_id) REFERENCES folder(folder_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES user_group(id) ON DELETE CASCADE,
    CHECK ((user_id IS NOT NULL OR group_id IS NOT NULL) AND (file_id IS NOT NULL OR folder_id IS NOT NULL))
);

-- 分享表
CREATE TABLE IF NOT EXISTS share (
    id INT PRIMARY KEY AUTO_INCREMENT,
    share_link VARCHAR(255) UNIQUE NOT NULL,
    file_id BIGINT UNSIGNED DEFAULT NULL, 
    folder_id BIGINT UNSIGNED DEFAULT NULL, 
    created_by BIGINT UNSIGNED NOT NULL, 
    password VARCHAR(255) DEFAULT NULL, 
    expire_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (file_id) REFERENCES `file`(file_id) ON DELETE CASCADE, 
    FOREIGN KEY (folder_id) REFERENCES folder(folder_id) ON DELETE CASCADE, 
    FOREIGN KEY (created_by) REFERENCES `user`(user_id) ON DELETE CASCADE, 
    -- 确保分享的是文件或文件夹之一
    CHECK ((file_id IS NOT NULL AND folder_id IS NULL) OR (file_id IS NULL AND folder_id IS NOT NULL))
);

-- 日志表
CREATE TABLE IF NOT EXISTS `log` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL, 
    operation VARCHAR(255) NOT NULL,
    details TEXT, -- 记录更详细的信息，如移动的源和目标路径
    ip_address VARCHAR(45),
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES `user`(user_id) ON DELETE CASCADE 
);

-- 通知表
CREATE TABLE IF NOT EXISTS `notification` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL, 
    message VARCHAR(255) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES `user`(user_id) ON DELETE CASCADE 
);


-- ----------------------------  
-- View structure for v_file_folder_details  
-- ----------------------------  
CREATE OR REPLACE VIEW `v_file_folder_details` AS  
SELECT  
  'file' AS item_type,  
  f.file_id AS id,  
  f.file_name AS name,  
  so.size AS size, -- 从 storage_object 获取大小  
  f.mime_type,  
  f.folder_id AS parent_id,  
  f.uploader_id AS owner_id,  
  u.username AS owner_name,  
  f.created_at,  
  f.updated_at  
FROM file f  
JOIN user u ON f.uploader_id = u.user_id  
JOIN storage_object so ON f.object_hash = so.hash   
WHERE f.status = 'active'  
UNION ALL  
SELECT  
  'folder' AS item_type,  
  fo.folder_id AS id,  
  fo.folder_name AS name,  
  fo.size,  
  'inode/directory' AS mime_type,  
  fo.parent_folder_id AS parent_id,  
  fo.owner_id AS owner_id,  
  u.username AS owner_name,  
  fo.created_at,  
  fo.updated_at  
FROM folder fo  
JOIN user u ON fo.owner_id = u.user_id  
WHERE fo.status = 'active';  

-- ----------------------------  
-- View structure for v_user_permissions  
-- ----------------------------  
CREATE OR REPLACE VIEW v_user_permissions AS  
SELECT  
    acl.id AS acl_id,  
    u.user_id AS user_id,   
    u.username AS user_name,   
    COALESCE(f.file_id, fl.folder_id) AS item_id,   
    COALESCE(f.file_name, fl.folder_name) AS item_name,   
    CASE   
        WHEN f.file_id IS NOT NULL THEN 'file'   
        ELSE 'folder'   
    END AS item_type,  
    acl.permission  
FROM  
    acl  
JOIN  
    `user` u ON acl.user_id = u.user_id   
LEFT JOIN  
    `file` f ON acl.file_id = f.file_id   
LEFT JOIN  
    folder fl ON acl.folder_id = fl.folder_id   
WHERE   
    acl.user_id IS NOT NULL;  

-- ----------------------------  
-- View structure for v_user_storage_summary  
-- ----------------------------  
CREATE OR REPLACE VIEW `v_user_storage_summary` AS  
SELECT  
  `user_id`,  
  `username`,  
  `storage_limit`,  
  `storage_used`  
FROM `user`;  

-- ----------------------------  
-- View structure for v_shared_with_me (FIXED)  
-- ----------------------------  
CREATE OR REPLACE VIEW v_shared_with_me AS  
-- 通过用户组共享给我的  
SELECT  
    vffd.item_type,  
    vffd.id AS item_id,  
    vffd.name AS item_name,  
    vffd.owner_name AS shared_by,  
    acl.permission,  
    ugm.user_id AS shared_to_user_id  
FROM acl  
JOIN user_group_member ugm ON acl.group_id = ugm.group_id  
-- 修复后的 JOIN 条件: 确保 ACL 和视图中的项目类型和ID都匹配  
JOIN v_file_folder_details vffd   
    ON (acl.file_id IS NOT NULL AND vffd.item_type = 'file' AND acl.file_id = vffd.id)  
    OR (acl.folder_id IS NOT NULL AND vffd.item_type = 'folder' AND acl.folder_id = vffd.id)  
WHERE acl.group_id IS NOT NULL  
UNION  
-- 直接共享给我的  
SELECT  
    vffd.item_type,  
    vffd.id AS item_id,  
    vffd.name AS item_name,  
    vffd.owner_name AS shared_by,  
    acl.permission,  
    acl.user_id AS shared_to_user_id  
FROM acl  
-- 修复后的 JOIN 条件: 确保 ACL 和视图中的项目类型和ID都匹配  
JOIN v_file_folder_details vffd   
    ON (acl.file_id IS NOT NULL AND vffd.item_type = 'file' AND acl.file_id = vffd.id)  
    OR (acl.folder_id IS NOT NULL AND vffd.item_type = 'folder' AND acl.folder_id = vffd.id)  
WHERE acl.user_id IS NOT NULL;  


-- ----------------------------  
-- View structure for v_full_path (Requires MySQL 8.0+ or MariaDB 10.2.2+)  
-- ----------------------------  
CREATE OR REPLACE VIEW v_full_path AS  
WITH RECURSIVE folder_path (id, name, path) AS (  
  -- 根目录  
  SELECT   
    folder_id,   
    folder_name,   
    CAST(folder_name AS CHAR(2048))  
  FROM folder  
  WHERE parent_folder_id IS NULL  
  UNION ALL  
  -- 递归查找子目录  
  SELECT   
    f.folder_id,   
    f.folder_name,   
    CONCAT(fp.path, '/', f.folder_name)  
  FROM folder AS f JOIN folder_path AS fp ON f.parent_folder_id = fp.id  
)  
SELECT id, path FROM folder_path;  

-- ----------------------------  
-- View structure for v_user_recycle_bin  
-- ----------------------------  
CREATE OR REPLACE VIEW v_user_recycle_bin AS  
SELECT   
    'file' AS item_type,  
    f.file_id AS id,  
    f.file_name AS name,  
    so.size,  
    f.folder_id AS parent_id,  
    f.uploader_id AS owner_id,  
    u.username AS owner_name,  
    f.deleted_at  
FROM file f  
JOIN user u ON f.uploader_id = u.user_id  
JOIN storage_object so ON f.object_hash = so.hash  
WHERE f.status = 'deleted' AND f.deleted_at IS NOT NULL  
UNION ALL  
SELECT   
    'folder' AS item_type,  
    fo.folder_id AS id,  
    fo.folder_name AS name,  
    fo.size,  
    fo.parent_folder_id AS parent_id,  
    fo.owner_id AS owner_id,  
    u.username AS owner_name,  
    fo.deleted_at  
FROM folder fo  
JOIN user u ON fo.owner_id = u.user_id  
WHERE fo.status = 'deleted' AND fo.deleted_at IS NOT NULL;  