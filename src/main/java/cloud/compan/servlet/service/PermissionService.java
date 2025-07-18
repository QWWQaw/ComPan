package cloud.compan.servlet.service;

import java.sql.*;
import java.util.*;

/**
 * 权限服务类 - 处理文件和文件夹的权限管理
 */
public class PermissionService {

    private DatabaseService databaseService;

    public PermissionService() {
        this.databaseService = DatabaseService.getInstance();
    }

    /**
     * 设置权限
     */
    public Map<String, Object> setPermission(String resourceType, Long resourceId, Long userId, String permission) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证参数
            if (resourceType == null || resourceId == null || userId == null || permission == null) {
                result.put("success", false);
                result.put("message", "参数不能为空");
                result.put("status_code", 400);
                return result;
            }

            // 2. 验证权限类型
            if (!isValidPermission(permission)) {
                result.put("success", false);
                result.put("message", "无效的权限类型");
                result.put("status_code", 400);
                return result;
            }

            // 3. 检查资源是否存在
            if (!resourceExists(resourceType, resourceId)) {
                result.put("success", false);
                result.put("message", "资源不存在");
                result.put("status_code", 404);
                return result;
            }

            // 4. 插入或更新权限
            String sql = "INSERT INTO acl (resource_type, resource_id, user_id, permission, created_at) " +
                        "VALUES (?, ?, ?, ?, NOW()) " +
                        "ON DUPLICATE KEY UPDATE permission = VALUES(permission), updated_at = NOW()";

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, resourceType);
                stmt.setLong(2, resourceId);
                stmt.setLong(3, userId);
                stmt.setString(4, permission);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    result.put("success", true);
                    result.put("message", "权限设置成功");
                    result.put("data", Map.of(
                        "resource_type", resourceType,
                        "resource_id", resourceId,
                        "user_id", userId,
                        "permission", permission
                    ));
                } else {
                    result.put("success", false);
                    result.put("message", "权限设置失败");
                    result.put("status_code", 500);
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("设置权限失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "服务器内部错误");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 获取资源的权限列表
     */
    public Map<String, Object> getResourcePermissions(String resourceType, Long resourceId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT a.id, a.user_id, u.username, a.permission, a.created_at " +
                        "FROM acl a JOIN users u ON a.user_id = u.user_id " +
                        "WHERE a.resource_type = ? AND a.resource_id = ?";

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, resourceType);
                stmt.setLong(2, resourceId);
                rs = stmt.executeQuery();

                List<Map<String, Object>> permissions = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> permission = new HashMap<>();
                    permission.put("id", rs.getLong("id"));
                    permission.put("user_id", rs.getLong("user_id"));
                    permission.put("username", rs.getString("username"));
                    permission.put("permission", rs.getString("permission"));
                    permission.put("created_at", rs.getTimestamp("created_at"));
                    permissions.add(permission);
                }

                result.put("success", true);
                result.put("data", permissions);

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取资源权限失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "服务器内部错误");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 获取用户的权限列表
     */
    public Map<String, Object> getUserPermissions(Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT a.id, a.resource_type, a.resource_id, a.permission, a.created_at " +
                        "FROM acl a WHERE a.user_id = ?";

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, userId);
                rs = stmt.executeQuery();

                List<Map<String, Object>> permissions = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> permission = new HashMap<>();
                    permission.put("id", rs.getLong("id"));
                    permission.put("resource_type", rs.getString("resource_type"));
                    permission.put("resource_id", rs.getLong("resource_id"));
                    permission.put("permission", rs.getString("permission"));
                    permission.put("created_at", rs.getTimestamp("created_at"));
                    permissions.add(permission);
                }

                result.put("success", true);
                result.put("data", permissions);

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取用户权限失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "服务器内部错误");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 更新权限
     */
    public Map<String, Object> updatePermission(Long permissionId, String permission) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证权限类型
            if (!isValidPermission(permission)) {
                result.put("success", false);
                result.put("message", "无效的权限类型");
                result.put("status_code", 400);
                return result;
            }

            String sql = "UPDATE acl SET permission = ?, updated_at = NOW() WHERE id = ?";

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, permission);
                stmt.setLong(2, permissionId);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    result.put("success", true);
                    result.put("message", "权限更新成功");
                    result.put("data", Map.of("permission_id", permissionId, "permission", permission));
                } else {
                    result.put("success", false);
                    result.put("message", "权限不存在");
                    result.put("status_code", 404);
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("更新权限失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "服务器内部错误");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 删除权限
     */
    public Map<String, Object> deletePermission(Long permissionId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "DELETE FROM acl WHERE id = ?";

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, permissionId);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    result.put("success", true);
                    result.put("message", "权限删除成功");
                } else {
                    result.put("success", false);
                    result.put("message", "权限不存在");
                    result.put("status_code", 404);
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("删除权限失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "服务器内部错误");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 检查用户是否有权限访问文件
     */
    public boolean hasFilePermission(Long fileId, Long userId, String permission) {
        try {
            // 1. 检查是否为文件所有者
            if (isFileOwner(fileId, userId)) {
                return true;
            }

            // 2. 检查ACL权限
            String sql = "SELECT COUNT(*) FROM acl WHERE resource_type = 'file' AND resource_id = ? AND user_id = ? AND permission = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, fileId);
                stmt.setLong(2, userId);
                stmt.setString(3, permission);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("检查文件权限失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否有权限访问文件夹
     */
    public boolean hasFolderPermission(Long folderId, Long userId, String permission) {
        try {
            // 1. 检查是否为文件夹所有者
            if (isFolderOwner(folderId, userId)) {
                return true;
            }

            // 2. 检查ACL权限
            String sql = "SELECT COUNT(*) FROM acl WHERE resource_type = 'folder' AND resource_id = ? AND user_id = ? AND permission = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, folderId);
                stmt.setLong(2, userId);
                stmt.setString(3, permission);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("检查文件夹权限失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 为用户授予文件权限
     */
    public Map<String, Object> grantFilePermission(Long fileId, Long userId, Long targetUserId, String permission) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证是否有权限授予权限（必须是所有者或有管理权限）
            if (!isFileOwner(fileId, userId) && !hasFilePermission(fileId, userId, "admin")) {
                result.put("success", false);
                result.put("message", "无权限进行此操作");
                return result;
            }

            // 2. 检查权限是否已存在
            if (hasFilePermission(fileId, targetUserId, permission)) {
                result.put("success", false);
                result.put("message", "用户已拥有该权限");
                return result;
            }

            // 3. 创建权限记录
            String sql = "INSERT INTO acl (resource_type, resource_id, user_id, permission, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, "file");
                stmt.setLong(2, fileId);
                stmt.setLong(3, targetUserId);
                stmt.setString(4, permission);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    result.put("success", true);
                    result.put("message", "权限授予成功");
                } else {
                    result.put("success", false);
                    result.put("message", "权限授予失败");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("授予文件权限失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "授予权限失败");
        }

        return result;
    }

    /**
     * 为用户授予文件夹权限
     */
    public Map<String, Object> grantFolderPermission(Long folderId, Long userId, Long targetUserId, String permission) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证是否有权限授予权限
            if (!isFolderOwner(folderId, userId) && !hasFolderPermission(folderId, userId, "admin")) {
                result.put("success", false);
                result.put("message", "无权限进行此操作");
                return result;
            }

            // 2. 检查权限是否已存在
            if (hasFolderPermission(folderId, targetUserId, permission)) {
                result.put("success", false);
                result.put("message", "用户已拥有该权限");
                return result;
            }

            // 3. 创建权限记录
            String sql = "INSERT INTO acl (resource_type, resource_id, user_id, permission, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, "folder");
                stmt.setLong(2, folderId);
                stmt.setLong(3, targetUserId);
                stmt.setString(4, permission);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    result.put("success", true);
                    result.put("message", "权限授予成功");
                } else {
                    result.put("success", false);
                    result.put("message", "权限授予失败");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("授予文件夹权限失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "授予权限失败");
        }

        return result;
    }

    /**
     * 撤销文件权限
     */
    public Map<String, Object> revokeFilePermission(Long fileId, Long userId, Long targetUserId, String permission) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证是否有权限撤销权限
            if (!isFileOwner(fileId, userId) && !hasFilePermission(fileId, userId, "admin")) {
                result.put("success", false);
                result.put("message", "无权限进行此操作");
                return result;
            }

            // 2. 删除权限记录
            String sql = "DELETE FROM acl WHERE resource_type = 'file' AND resource_id = ? AND user_id = ? AND permission = ?";
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, fileId);
                stmt.setLong(2, targetUserId);
                stmt.setString(3, permission);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    result.put("success", true);
                    result.put("message", "权限撤销成功");
                } else {
                    result.put("success", false);
                    result.put("message", "权限不存在或撤销失败");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("撤销文件权限失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "撤销权限失败");
        }

        return result;
    }

    /**
     * 撤销文件夹权限
     */
    public Map<String, Object> revokeFolderPermission(Long folderId, Long userId, Long targetUserId, String permission) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证是否有权限撤销权限
            if (!isFolderOwner(folderId, userId) && !hasFolderPermission(folderId, userId, "admin")) {
                result.put("success", false);
                result.put("message", "无权限进行此操作");
                return result;
            }

            // 2. 删除权限记录
            String sql = "DELETE FROM acl WHERE resource_type = 'folder' AND resource_id = ? AND user_id = ? AND permission = ?";
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, folderId);
                stmt.setLong(2, targetUserId);
                stmt.setString(3, permission);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    result.put("success", true);
                    result.put("message", "权限撤销成功");
                } else {
                    result.put("success", false);
                    result.put("message", "权限不存在或撤销失败");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("撤销文件夹权限失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "撤销权限失败");
        }

        return result;
    }

    /**
     * 检查用户是否为文件所有者
     */
    private boolean isFileOwner(Long fileId, Long userId) {
        try {
            String sql = "SELECT COUNT(*) FROM files WHERE file_id = ? AND user_id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, fileId);
                stmt.setLong(2, userId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("检查文件所有者失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否为文件夹所有者
     */
    private boolean isFolderOwner(Long folderId, Long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM folder WHERE folder_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, folderId);
            stmt.setLong(2, userId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 检查资源是否存在
     */
    private boolean resourceExists(String resourceType, Long resourceId) {
        try {
            String tableName = "file".equals(resourceType) ? "files" : "folders";
            String idColumn = "file".equals(resourceType) ? "file_id" : "folder_id";

            String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + idColumn + " = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, resourceId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("检查资源存在性失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 验证权限类型是否有效
     */
    private boolean isValidPermission(String permission) {
        return Arrays.asList("read", "write", "delete", "admin").contains(permission);
    }
}
