-- mysql or mariadb
CREATE DATABASE IF NOT EXISTS kepan
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE kepan;

-- 正确的删除顺序：先删除视图，再按依赖关系删除表
DROP VIEW IF EXISTS v_user_recycle_bin;
DROP VIEW IF EXISTS v_full_path;
DROP VIEW IF EXISTS v_shared_with_me;
DROP VIEW IF EXISTS v_user_storage_summary;
DROP VIEW IF EXISTS v_user_permissions;
DROP VIEW IF EXISTS v_file_folder_details;

-- 删除表：按依赖关系从子表到父表
DROP TABLE IF EXISTS `notification`;      -- 引用 user
DROP TABLE IF EXISTS `log`;               -- 引用 user
DROP TABLE IF EXISTS `share`;             -- 引用 file, folder, user
DROP TABLE IF EXISTS `acl`;               -- 引用 file, folder, user, user_group
DROP TABLE IF EXISTS `file`;              -- 引用 user, folder, storage_object
DROP TABLE IF EXISTS `folder`;            -- 引用 user，自引用
DROP TABLE IF EXISTS `user_group_member`; -- 引用 user, user_group
DROP TABLE IF EXISTS `user_group`;        -- 被 user_group_member 引用
DROP TABLE IF EXISTS `storage_object`;    -- 被 file 引用
DROP TABLE IF EXISTS `user`;              -- 被多个表引用

-- 1. 用户表
CREATE TABLE `user` (  
  user_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',  
  username VARCHAR(100) NOT NULL COMMENT '用户名',  
  email VARCHAR(255) NOT NULL COMMENT '邮箱',  
  password_hash VARCHAR(255) NOT NULL COMMENT 'hash密码',  
  storage_limit BIGINT NOT NULL DEFAULT 10737418240 COMMENT '存储空间上限(10GB)',  
  storage_used BIGINT NOT NULL DEFAULT 0 COMMENT '已用存储空间(由应用层维护)',  
  `status` ENUM('active', 'inactive', 'banned') NOT NULL DEFAULT 'active' COMMENT '用户状态',  
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',  
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',  
  PRIMARY KEY (user_id),  
  UNIQUE KEY uk_username (username),  
  UNIQUE KEY uk_email (email)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 2. 用户组与成员表 
CREATE TABLE IF NOT EXISTS user_group (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 3.用户组与成员表
CREATE TABLE IF NOT EXISTS user_group_member (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    group_id BIGINT UNSIGNED NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'member',
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES user_group(id) ON DELETE CASCADE,
    UNIQUE KEY (user_id, group_id)
);

-- 4. 存储对象表：用于物理存储文件的去重和秒传
CREATE TABLE `storage_object` (  
  `hash` VARCHAR(255) NOT NULL COMMENT '哈希 (SHA256), 作为物理文件的唯一标识',  
  `size` BIGINT NOT NULL COMMENT '文件大小(Bytes)',  
  storage_path VARCHAR(1024) NOT NULL COMMENT '文件在服务器上的物理存储路径',  
  ref_count INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '引用计数',  
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次上传时间',  
  PRIMARY KEY (`hash`)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物理存储对象表 (用于去重和秒传)';  

    
-- 5.文件夹表：增加软删除
CREATE TABLE `folder` (  
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

-- 6.文件表
CREATE TABLE `file` (  
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

-- 7.ACL表：访问控制列表
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

-- 8.分享表
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

-- 9.日志表
CREATE TABLE IF NOT EXISTS `log` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL, 
    operation VARCHAR(255) NOT NULL,
    details TEXT, -- 记录更详细的信息，如移动的源和目标路径
    ip_address VARCHAR(45),
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES `user`(user_id) ON DELETE CASCADE 
);

-- 10.通知表
-- 通知表 (已修正)
CREATE TABLE IF NOT EXISTS `notification` (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL, 
    message VARCHAR(255) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES `user`(user_id) ON DELETE CASCADE 
);


-- create views
CREATE VIEW `v_file_folder_details` AS  
SELECT  
  'file' AS item_type,  
  f.file_id AS id,  
  f.file_name AS name,  
  so.size AS size, -- Get size from storage_object  
  f.mime_type,  
  f.folder_id AS parent_id,  
  f.uploader_id AS owner_id,  
  u.username AS owner_name,  
  f.created_at,  
  f.updated_at  
FROM file f  
JOIN user u ON f.uploader_id = u.user_id  
JOIN storage_object so ON f.object_hash = so.hash -- Join to get physical file info  
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

-- “哪个用户”对“哪个文件/文件夹”拥有“什么权限”
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

-- 用户存储空间视图
CREATE OR REPLACE VIEW `v_user_storage_summary` AS  
SELECT  
  `user_id`,  
  `username`,  
  `storage_limit`,  
  `storage_used`  
FROM `user`;  

-- “与我共享”视图
CREATE OR REPLACE VIEW v_shared_with_me AS
SELECT
    vffd.item_type,
    vffd.id AS item_id,
    vffd.name AS item_name,
    vffd.owner_name AS shared_by,
    acl.permission,
    ugm.user_id AS shared_to_user_id
FROM acl
JOIN v_file_folder_details vffd ON (acl.file_id = vffd.id OR acl.folder_id = vffd.id)
-- 通过用户组共享
JOIN user_group_member ugm ON acl.group_id = ugm.group_id
WHERE acl.group_id IS NOT NULL
UNION
SELECT
    vffd.item_type,
    vffd.id AS item_id,
    vffd.name AS item_name,
    vffd.owner_name AS shared_by,
    acl.permission,
    acl.user_id AS shared_to_user_id
FROM acl
JOIN v_file_folder_details vffd ON (acl.file_id = vffd.id OR acl.folder_id = vffd.id)
-- 直接共享给用户
WHERE acl.user_id IS NOT NULL;

-- 完整路径视图 (使用递归查询)
CREATE OR REPLACE VIEW v_full_path AS
WITH RECURSIVE folder_path (id, name, path) AS (
  SELECT folder_id AS id, folder_name AS name, CAST(folder_name AS CHAR(2048)) AS path
  FROM folder
  WHERE parent_folder_id IS NULL
  UNION ALL
  SELECT f.folder_id AS id, f.folder_name AS name, CONCAT(fp.path, '/', f.folder_name) AS path
  FROM folder_path AS fp JOIN folder AS f ON fp.id = f.parent_folder_id
)
SELECT id, path FROM folder_path;

-- 用户回收站视图
CREATE OR REPLACE VIEW v_user_recycle_bin AS
SELECT 
    'file' AS item_type,
    f.file_id AS id,
    f.file_name AS name,
    f.folder_id AS parent_id,
    f.uploader_id AS owner_id,
    u.username AS owner_name,
    f.deleted_at
FROM file f
JOIN user u ON f.uploader_id = u.user_id
WHERE f.status = 'deleted' AND f.deleted_at IS NOT NULL
UNION ALL
SELECT 
    'folder' AS item_type,
    fo.folder_id AS id,
    fo.folder_name AS name,
    fo.parent_folder_id AS parent_id,
    fo.owner_id AS owner_id,
    u.username AS owner_name,
    fo.deleted_at
FROM folder fo
JOIN user u ON fo.owner_id = u.user_id
WHERE fo.status = 'deleted' AND fo.deleted_at IS NOT NULL;