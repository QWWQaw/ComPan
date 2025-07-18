package cloud.compan.servlet.service;

import java.sql.*;
import java.util.*;
import java.security.SecureRandom;

/**
 * 分享管理服务类
 * 根据RESTFUL API文档实现分享相关的业务逻辑
 */
public class ShareService {

    private final DatabaseService databaseService;
    private static final String SHARE_LINK_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHARE_LINK_LENGTH = 16;

    public ShareService() {
        this.databaseService = DatabaseService.getInstance();
    }

    /**
     * 创建分享链接
     */
    public Map<String, Object> createShare(Long userId, Long fileId, Long folderId,
                                          String password, String expireAt,
                                          boolean allowDownload, boolean allowPreview) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证参数
            if (fileId == null && folderId == null) {
                result.put("success", false);
                result.put("message", "必须指定要分享的文件或文件夹");
                return result;
            }

            if (fileId != null && folderId != null) {
                result.put("success", false);
                result.put("message", "不能同时分享文件和文件夹");
                return result;
            }

            // 2. 验证用户是否拥有该文件/文件夹
            if (!validateOwnership(userId, fileId, folderId)) {
                result.put("success", false);
                result.put("message", "您没有权限分享该文件或文件夹");
                return result;
            }

            // 3. 生成唯一的分享链接
            String shareLink = generateUniqueShareLink();

            // 4. 解析过期时间
            Timestamp expireTimestamp = null;
            if (expireAt != null && !expireAt.trim().isEmpty()) {
                try {
                    expireTimestamp = Timestamp.valueOf(expireAt);
                } catch (Exception e) {
                    result.put("success", false);
                    result.put("message", "无效的过期时间格式");
                    return result;
                }
            }

            // 5. 创建分享记录
            String sql = "INSERT INTO share (share_link, file_id, folder_id, created_by, password, expire_at) VALUES (?, ?, ?, ?, ?, ?)";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                stmt.setString(1, shareLink);
                stmt.setObject(2, fileId);
                stmt.setObject(3, folderId);
                stmt.setLong(4, userId);
                stmt.setString(5, password);
                stmt.setTimestamp(6, expireTimestamp);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        Long shareId = rs.getLong(1);

                        // 获取分享的项目信息
                        Map<String, Object> itemInfo = getShareItemInfo(fileId, folderId, conn);

                        Map<String, Object> shareData = new HashMap<>();
                        shareData.put("share_id", shareId);
                        shareData.put("share_link", shareLink);
                        shareData.put("share_url", "https://kepan.com/s/" + shareLink);
                        shareData.put("item_type", fileId != null ? "file" : "folder");
                        shareData.put("item_info", itemInfo);

                        Map<String, Object> settings = new HashMap<>();
                        settings.put("password_protected", password != null);
                        settings.put("expire_at", expireAt);
                        settings.put("allow_download", allowDownload);
                        settings.put("allow_preview", allowPreview);
                        shareData.put("settings", settings);

                        shareData.put("created_at", new Timestamp(System.currentTimeMillis()));

                        result.put("success", true);
                        result.put("message", "分享链接创建成功");
                        result.put("data", shareData);
                    }
                } else {
                    result.put("success", false);
                    result.put("message", "创建分享链接失败");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("创建分享链接失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 创建分享 - ShareHandler需要的方法签名
     */
    public Map<String, Object> createShare(Long userId, String resourceType, Long resourceId,
                                          Integer expiryDays, String password, String description) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证参数
            if (resourceType == null || (!resourceType.equals("file") && !resourceType.equals("folder"))) {
                result.put("success", false);
                result.put("message", "无效的资源类型");
                result.put("status_code", 400);
                return result;
            }

            // 2. 生成分享链接
            String shareLink = generateShareLink();

            // 3. 计算过期时间
            java.sql.Timestamp expiryTime = null;
            if (expiryDays != null && expiryDays > 0) {
                long expiryMillis = System.currentTimeMillis() + (expiryDays * 24L * 60 * 60 * 1000);
                expiryTime = new java.sql.Timestamp(expiryMillis);
            }

            // 4. 保存分享记录
            String sql = "INSERT INTO share (id, resource_type, resource_id, share_link, password, description, expiry_time, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setLong(1, userId);
                stmt.setString(2, resourceType);
                stmt.setLong(3, resourceId);
                stmt.setString(4, shareLink);
                stmt.setString(5, password);
                stmt.setString(6, description);
                if (expiryTime != null) {
                    stmt.setTimestamp(7, expiryTime);
                } else {
                    stmt.setNull(7, Types.TIMESTAMP);
                }

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        Long shareId = rs.getLong(1);

                        result.put("success", true);
                        result.put("message", "分享创建成功");
                        result.put("data", Map.of(
                            "share_id", shareId,
                            "share_link", shareLink,
                            "resource_type", resourceType,
                            "resource_id", resourceId,
                            "expiry_time", expiryTime,
                            "has_password", password != null && !password.isEmpty()
                        ));
                    }
                } else {
                    result.put("success", false);
                    result.put("message", "分享创建失败");
                    result.put("status_code", 500);
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("创建分享失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "创建分享失败");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 获取我的分享列表
     */
    public Map<String, Object> getMyShares(Long userId, int page, int perPage) {
        Map<String, Object> result = new HashMap<>();

        try {
            int offset = (page - 1) * perPage;

            String sql = "SELECT s.id, s.share_link, s.file_id, s.folder_id, s.password, s.expire_at, s.created_at " +
                        "FROM share s WHERE s.created_by = ? ORDER BY s.created_at DESC LIMIT ? OFFSET ?";

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, userId);
                stmt.setInt(2, perPage);
                stmt.setInt(3, offset);

                rs = stmt.executeQuery();

                List<Map<String, Object>> shares = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> share = new HashMap<>();
                    share.put("share_id", rs.getLong("id"));
                    share.put("share_link", rs.getString("share_link"));
                    share.put("share_url", "https://kepan.com/s/" + rs.getString("share_link"));

                    Long fileId = rs.getObject("file_id", Long.class);
                    Long folderId = rs.getObject("folder_id", Long.class);

                    share.put("item_type", fileId != null ? "file" : "folder");
                    share.put("item_info", getShareItemInfo(fileId, folderId, conn));

                    Map<String, Object> settings = new HashMap<>();
                    settings.put("password_protected", rs.getString("password") != null);
                    settings.put("expire_at", rs.getTimestamp("expire_at"));
                    settings.put("allow_download", true);
                    settings.put("allow_preview", true);
                    share.put("settings", settings);

                    share.put("created_at", rs.getTimestamp("created_at"));

                    shares.add(share);
                }

                // 获取总数
                long totalCountLong = getTotalShareCount(userId);
                int totalCount = (int) totalCountLong;

                Map<String, Object> pagination = new HashMap<>();
                pagination.put("current_page", page);
                pagination.put("per_page", perPage);
                pagination.put("total", totalCount);
                pagination.put("total_pages", (totalCount + perPage - 1) / perPage);
                pagination.put("has_next", offset + perPage < totalCount);
                pagination.put("has_prev", page > 1);

                Map<String, Object> shareData = new HashMap<>();
                shareData.put("items", shares);
                shareData.put("pagination", pagination);

                result.put("success", true);
                result.put("message", "获取分享列表成功");
                result.put("data", shareData);

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取分享列表失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 获取用户分享列表 - ShareHandler需要的方法
     */
    public Map<String, Object> getUserShares(Long userId, int page, int size) {
        Map<String, Object> result = new HashMap<>();

        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM shares WHERE user_id = ?");
            sqlBuilder.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sqlBuilder.toString());
                stmt.setLong(1, userId);
                stmt.setInt(2, size);
                stmt.setInt(3, (page - 1) * size);

                rs = stmt.executeQuery();

                List<Map<String, Object>> shares = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> share = new HashMap<>();
                    share.put("share_id", rs.getLong("share_id"));
                    share.put("resource_type", rs.getString("resource_type"));
                    share.put("resource_id", rs.getLong("resource_id"));
                    share.put("share_link", rs.getString("share_link"));
                    share.put("description", rs.getString("description"));
                    share.put("expiry_time", rs.getTimestamp("expiry_time"));
                    share.put("created_at", rs.getTimestamp("created_at"));
                    share.put("has_password", rs.getString("password") != null);
                    shares.add(share);
                }

                result.put("success", true);
                result.put("data", Map.of(
                    "shares", shares,
                    "page", page,
                    "size", size,
                    "total", getTotalShareCount(userId)
                ));

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取分享列表失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "获取分享列表失败");
        }

        return result;
    }

    /**
     * 获取分享详情
     */
    public Map<String, Object> getShareDetails(String shareLink) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT * FROM share WHERE share_link = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, shareLink);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    // 检查是否过期
                    java.sql.Timestamp expiryTime = rs.getTimestamp("expiry_time");
                    if (expiryTime != null && expiryTime.before(new java.sql.Timestamp(System.currentTimeMillis()))) {
                        result.put("success", false);
                        result.put("message", "分享已过期");
                        result.put("status_code", 410);
                        return result;
                    }

                    Map<String, Object> shareData = new HashMap<>();
                    shareData.put("share_id", rs.getLong("share_id"));
                    shareData.put("resource_type", rs.getString("resource_type"));
                    shareData.put("resource_id", rs.getLong("resource_id"));
                    shareData.put("description", rs.getString("description"));
                    shareData.put("expiry_time", expiryTime);
                    shareData.put("created_at", rs.getTimestamp("created_at"));
                    shareData.put("has_password", rs.getString("password") != null);

                    result.put("success", true);
                    result.put("data", shareData);
                } else {
                    result.put("success", false);
                    result.put("message", "分享不存在");
                    result.put("status_code", 404);
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取分享详情失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "获取分享详情失败");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 访问分享内容
     */
    public Map<String, Object> accessShare(String shareLink, String password) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT * FROM share WHERE share_link = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, shareLink);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    // 检查是否过期
                    java.sql.Timestamp expiryTime = rs.getTimestamp("expiry_time");
                    if (expiryTime != null && expiryTime.before(new java.sql.Timestamp(System.currentTimeMillis()))) {
                        result.put("success", false);
                        result.put("message", "分享已过期");
                        result.put("status_code", 410);
                        return result;
                    }

                    // 检查密码
                    String storedPassword = rs.getString("password");
                    if (storedPassword != null && !storedPassword.equals(password)) {
                        result.put("success", false);
                        result.put("message", "密码错误");
                        result.put("status_code", 401);
                        return result;
                    }

                    // 返回资源信息
                    String resourceType = rs.getString("resource_type");
                    Long resourceId = rs.getLong("resource_id");

                    Map<String, Object> resourceData = getResourceData(resourceType, resourceId);
                    if (resourceData != null) {
                        result.put("success", true);
                        result.put("message", "访问成功");
                        result.put("data", resourceData);
                    } else {
                        result.put("success", false);
                        result.put("message", "资源不存在");
                        result.put("status_code", 404);
                    }
                } else {
                    result.put("success", false);
                    result.put("message", "分享不存在");
                    result.put("status_code", 404);
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("访问分享失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "访问分享失败");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 删除分享
     */
    public Map<String, Object> deleteShare(Long shareId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "DELETE FROM share WHERE id = ? AND created_by = ?";

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, shareId);
                stmt.setLong(2, userId);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    result.put("success", true);
                    result.put("message", "分享已删除");
                    result.put("data", Map.of("share_id", shareId, "deleted_at", new Timestamp(System.currentTimeMillis())));
                } else {
                    result.put("success", false);
                    result.put("message", "分享不存在或无权限删除");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("删除分享失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 生成分享链接
     */
    private String generateShareLink() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    // ==================== 辅助方法 ====================

    /**
     * 验证用户是否拥有文件/文件夹
     */
    private boolean validateOwnership(Long userId, Long fileId, Long folderId) {
        try {
            Connection conn = databaseService.getConnection();

            if (fileId != null) {
                String sql = "SELECT COUNT(*) FROM file WHERE file_id = ? AND uploader_id = ? AND status = 'active'";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, fileId);
                    stmt.setLong(2, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next() && rs.getInt(1) > 0;
                    }
                }
            } else if (folderId != null) {
                String sql = "SELECT COUNT(*) FROM folder WHERE folder_id = ? AND owner_id = ? AND status = 'active'";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, folderId);
                    stmt.setLong(2, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next() && rs.getInt(1) > 0;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("验证所有权失败: " + e.getMessage());
        }

        return false;
    }

    /**
     * 生成唯一的分享链接
     */
    private String generateUniqueShareLink() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(SHARE_LINK_LENGTH);

        for (int i = 0; i < 10; i++) { // 最多尝试10次
            sb.setLength(0);
            for (int j = 0; j < SHARE_LINK_LENGTH; j++) {
                sb.append(SHARE_LINK_CHARS.charAt(random.nextInt(SHARE_LINK_CHARS.length())));
            }

            String shareLink = sb.toString();
            if (!isShareLinkExists(shareLink)) {
                return shareLink;
            }
        }

        // 如果10次都重复了，添加时间戳确保唯一性
        return sb.toString() + System.currentTimeMillis();
    }

    /**
     * 检查分享链接是否已存在
     */
    private boolean isShareLinkExists(String shareLink) {
        try {
            String sql = "SELECT COUNT(*) FROM share WHERE share_link = ?";
            Connection conn = databaseService.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, shareLink);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            System.err.println("检查分享链接是否存在失败: " + e.getMessage());
        }
        return true; // 出错时认为存在，避免重复
    }

    /**
     * 获取分享项目信息
     */
    private Map<String, Object> getShareItemInfo(Long fileId, Long folderId, Connection conn) {
        Map<String, Object> itemInfo = new HashMap<>();

        try {
            if (fileId != null) {
                String sql = "SELECT f.file_name, f.mime_type, so.size FROM file f " +
                           "JOIN storage_object so ON f.object_hash = so.hash WHERE f.file_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, fileId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            itemInfo.put("id", fileId);
                            itemInfo.put("name", rs.getString("file_name"));
                            itemInfo.put("size", rs.getLong("size"));
                            itemInfo.put("mime_type", rs.getString("mime_type"));
                        }
                    }
                }
            } else if (folderId != null) {
                String sql = "SELECT folder_name, size FROM folder WHERE folder_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, folderId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            itemInfo.put("id", folderId);
                            itemInfo.put("name", rs.getString("folder_name"));
                            itemInfo.put("size", rs.getLong("size"));
                            itemInfo.put("mime_type", "inode/directory");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("获取分享项目信息失败: " + e.getMessage());
        }

        return itemInfo;
    }

    /**
     * 获取分享总数
     */
    private long getTotalShareCount(Long userId) {
        try {
            String sql = "SELECT COUNT(*) FROM share WHERE user_id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, userId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getLong(1);
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取分享总数失败: " + e.getMessage());
        }

        return 0;
    }

    /**
     * 获取资源数据
     */
    private Map<String, Object> getResourceData(String resourceType, Long resourceId) {
        try {
            if ("file".equals(resourceType)) {
                return getFileData(resourceId);
            } else if ("folder".equals(resourceType)) {
                return getFolderData(resourceId);
            }
        } catch (Exception e) {
            System.err.println("获取资源数据失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 获取文件数据
     */
    private Map<String, Object> getFileData(Long fileId) throws SQLException {
        String sql = "SELECT * FROM files WHERE file_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, fileId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> fileData = new HashMap<>();
                fileData.put("file_id", rs.getLong("file_id"));
                fileData.put("filename", rs.getString("filename"));
                fileData.put("file_size", rs.getLong("file_size"));
                fileData.put("file_type", rs.getString("file_type"));
                fileData.put("created_at", rs.getTimestamp("created_at"));
                return fileData;
            }

        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }

        return null;
    }

    /**
     * 获取文件夹数据
     */
    private Map<String, Object> getFolderData(Long folderId) throws SQLException {
        String sql = "SELECT * FROM folders WHERE folder_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, folderId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> folderData = new HashMap<>();
                folderData.put("folder_id", rs.getLong("folder_id"));
                folderData.put("folder_name", rs.getString("folder_name"));
                folderData.put("description", rs.getString("description"));
                folderData.put("created_at", rs.getTimestamp("created_at"));
                return folderData;
            }

        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }

        return null;
    }
}
