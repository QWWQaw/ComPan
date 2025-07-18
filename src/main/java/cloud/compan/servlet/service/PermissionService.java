package cloud.compan.servlet.service;

import java.sql.*;
import java.util.*;

/**
 * 权限管理服务类
 * 根据RESTFUL API文档实现权限相关的业务逻辑
 */
public class PermissionService {

    private final DatabaseService databaseService;

    public PermissionService() {
        this.databaseService = DatabaseService.getInstance();
    }

    /**
     * 设置文件/文件夹权限
     */
    public Map<String, Object> setPermission(Long userId, Long fileId, Long folderId,
                                           Long targetUserId, Long targetGroupId, String permission) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证权限设置者是否为所有者
            if (!isOwner(userId, fileId, folderId)) {
                result.put("success", false);
                result.put("message", "只有文件/文件夹所有者才能设置权限");
                return result;
            }

            // 2. 验证目标用户或用户组是否存在
            if (targetUserId != null && !isUserExists(targetUserId)) {
                result.put("success", false);
                result.put("message", "目标用户不存在");
                return result;
            }

            if (targetGroupId != null && !isGroupExists(targetGroupId)) {
                result.put("success", false);
                result.put("message", "目标用户组不存在");
                return result;
            }

            // 3. 检查是否已存在相同的权限设置
            if (isPermissionExists(fileId, folderId, targetUserId, targetGroupId)) {
                // 更新现有权限
                return updateExistingPermission(fileId, folderId, targetUserId, targetGroupId, permission);
            }

            // 4. 创建新的权限记录
            String sql = "INSERT INTO acl (file_id, folder_id, user_id, group_id, permission) VALUES (?, ?, ?, ?, ?)";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                stmt.setObject(1, fileId);
                stmt.setObject(2, folderId);
                stmt.setObject(3, targetUserId);
                stmt.setObject(4, targetGroupId);
                stmt.setString(5, permission);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        Long permissionId = rs.getLong(1);

                        Map<String, Object> permissionData = new HashMap<>();
                        permissionData.put("permission_id", permissionId);
                        permissionData.put("item_type", fileId != null ? "file" : "folder");
                        permissionData.put("item_info", getItemInfo(fileId, folderId, conn));
                        permissionData.put("granted_to", getTargetInfo(targetUserId, targetGroupId, conn));
                        permissionData.put("permission", permission);
                        permissionData.put("created_at", new Timestamp(System.currentTimeMillis()));

                        result.put("success", true);
                        result.put("message", "权限设置成功");
                        result.put("data", permissionData);
                    }
                } else {
                    result.put("success", false);
                    result.put("message", "权限设置失败");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("设置权限失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 获取权限列表
     */
    public Map<String, Object> getPermissions(Long userId, Long fileId, Long folderId, int page, int perPage) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证用户是否有权限查看
            if (!isOwner(userId, fileId, folderId)) {
                result.put("success", false);
                result.put("message", "只有文件/文件夹所有者才能查看权限");
                return result;
            }

            int offset = (page - 1) * perPage;

            StringBuilder sqlBuilder = new StringBuilder(
                "SELECT a.id, a.user_id, a.group_id, a.permission, " +
                "u.username, ug.name as group_name " +
                "FROM acl a " +
                "LEFT JOIN user u ON a.user_id = u.user_id " +
                "LEFT JOIN user_group ug ON a.group_id = ug.id " +
                "WHERE "
            );

            if (fileId != null) {
                sqlBuilder.append("a.file_id = ?");
            } else {
                sqlBuilder.append("a.folder_id = ?");
            }

            sqlBuilder.append(" ORDER BY a.id DESC LIMIT ? OFFSET ?");

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sqlBuilder.toString());

                if (fileId != null) {
                    stmt.setLong(1, fileId);
                } else {
                    stmt.setLong(1, folderId);
                }
                stmt.setInt(2, perPage);
                stmt.setInt(3, offset);

                rs = stmt.executeQuery();

                List<Map<String, Object>> permissions = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> permission = new HashMap<>();
                    permission.put("permission_id", rs.getLong("id"));
                    permission.put("permission", rs.getString("permission"));

                    if (rs.getLong("user_id") != 0) {
                        Map<String, Object> grantedTo = new HashMap<>();
                        grantedTo.put("type", "user");
                        grantedTo.put("id", rs.getLong("user_id"));
                        grantedTo.put("name", rs.getString("username"));
                        permission.put("granted_to", grantedTo);
                    } else if (rs.getLong("group_id") != 0) {
                        Map<String, Object> grantedTo = new HashMap<>();
                        grantedTo.put("type", "group");
                        grantedTo.put("id", rs.getLong("group_id"));
                        grantedTo.put("name", rs.getString("group_name"));
                        permission.put("granted_to", grantedTo);
                    }

                    permissions.add(permission);
                }

                // 获取总数
                int totalCount = getTotalPermissionCount(fileId, folderId, conn);

                Map<String, Object> pagination = new HashMap<>();
                pagination.put("current_page", page);
                pagination.put("per_page", perPage);
                pagination.put("total", totalCount);
                pagination.put("total_pages", (totalCount + perPage - 1) / perPage);
                pagination.put("has_next", offset + perPage < totalCount);
                pagination.put("has_prev", page > 1);

                Map<String, Object> permissionData = new HashMap<>();
                permissionData.put("permissions", permissions);
                permissionData.put("pagination", pagination);

                result.put("success", true);
                result.put("message", "获取权限列表成功");
                result.put("data", permissionData);

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取权限列表失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 更新权限
     */
    public Map<String, Object> updatePermission(Long permissionId, String permission, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证权限是否存在且用户有权限修改
            if (!canModifyPermission(permissionId, userId)) {
                result.put("success", false);
                result.put("message", "权限不存在或无权限修改");
                return result;
            }

            // 2. 获取原权限信息
            Map<String, Object> oldPermissionInfo = getPermissionInfo(permissionId);

            // 3. 更新权限
            String sql = "UPDATE acl SET permission = ? WHERE id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, permission);
                stmt.setLong(2, permissionId);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put("permission_id", permissionId);
                    updateData.put("old_permission", oldPermissionInfo.get("permission"));
                    updateData.put("new_permission", permission);
                    updateData.put("granted_to", oldPermissionInfo.get("granted_to"));
                    updateData.put("item_info", oldPermissionInfo.get("item_info"));
                    updateData.put("updated_at", new Timestamp(System.currentTimeMillis()));

                    result.put("success", true);
                    result.put("message", "权限更新成功");
                    result.put("data", updateData);
                } else {
                    result.put("success", false);
                    result.put("message", "权限更新失败");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("更新权限失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 删除权限
     */
    public Map<String, Object> deletePermission(Long permissionId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证权限是否存在且用户有权限删除
            if (!canModifyPermission(permissionId, userId)) {
                result.put("success", false);
                result.put("message", "权限不存在或无权限删除");
                return result;
            }

            // 2. 获取权限信息用于返回
            Map<String, Object> permissionInfo = getPermissionInfo(permissionId);

            // 3. 删除权限
            String sql = "DELETE FROM acl WHERE id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, permissionId);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    Map<String, Object> deleteData = new HashMap<>();
                    deleteData.put("permission_id", permissionId);
                    deleteData.put("revoked_permission", permissionInfo.get("permission"));
                    deleteData.put("revoked_from", permissionInfo.get("granted_to"));
                    deleteData.put("item_info", permissionInfo.get("item_info"));
                    deleteData.put("deleted_at", new Timestamp(System.currentTimeMillis()));

                    result.put("success", true);
                    result.put("message", "权限已删除");
                    result.put("data", deleteData);
                } else {
                    result.put("success", false);
                    result.put("message", "权限删除失败");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("删除权限失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    // ==================== 辅助方法 ====================

    /**
     * 检查用户是否为文件/文件夹所有者
     */
    private boolean isOwner(Long userId, Long fileId, Long folderId) {
        try {
            Connection conn = databaseService.getConnection();

            if (fileId != null) {
                String sql = "SELECT COUNT(*) FROM file WHERE file_id = ? AND uploader_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, fileId);
                    stmt.setLong(2, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next() && rs.getInt(1) > 0;
                    }
                }
            } else if (folderId != null) {
                String sql = "SELECT COUNT(*) FROM folder WHERE folder_id = ? AND owner_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, folderId);
                    stmt.setLong(2, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next() && rs.getInt(1) > 0;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("检查所有者失败: " + e.getMessage());
        }

        return false;
    }

    /**
     * 检查用户是否存在
     */
    private boolean isUserExists(Long userId) {
        try {
            String sql = "SELECT COUNT(*) FROM user WHERE user_id = ?";
            Connection conn = databaseService.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            System.err.println("检查用户是否存在失败: " + e.getMessage());
        }
        return false;
    }

    /**
     * 检查用户组是否存在
     */
    private boolean isGroupExists(Long groupId) {
        try {
            String sql = "SELECT COUNT(*) FROM user_group WHERE id = ?";
            Connection conn = databaseService.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, groupId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            System.err.println("检查用户组是否存在失败: " + e.getMessage());
        }
        return false;
    }

    /**
     * 检查权限是否已存在
     */
    private boolean isPermissionExists(Long fileId, Long folderId, Long userId, Long groupId) {
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) FROM acl WHERE ");

            if (fileId != null) {
                sqlBuilder.append("file_id = ? AND ");
            } else {
                sqlBuilder.append("folder_id = ? AND ");
            }

            if (userId != null) {
                sqlBuilder.append("user_id = ?");
            } else {
                sqlBuilder.append("group_id = ?");
            }

            Connection conn = databaseService.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
                if (fileId != null) {
                    stmt.setLong(1, fileId);
                } else {
                    stmt.setLong(1, folderId);
                }

                if (userId != null) {
                    stmt.setLong(2, userId);
                } else {
                    stmt.setLong(2, groupId);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            System.err.println("检查权限是否存在失败: " + e.getMessage());
        }
        return false;
    }

    /**
     * 更新现有权限
     */
    private Map<String, Object> updateExistingPermission(Long fileId, Long folderId, Long userId, Long groupId, String permission) {
        Map<String, Object> result = new HashMap<>();

        try {
            StringBuilder sqlBuilder = new StringBuilder("UPDATE acl SET permission = ? WHERE ");

            if (fileId != null) {
                sqlBuilder.append("file_id = ? AND ");
            } else {
                sqlBuilder.append("folder_id = ? AND ");
            }

            if (userId != null) {
                sqlBuilder.append("user_id = ?");
            } else {
                sqlBuilder.append("group_id = ?");
            }

            Connection conn = databaseService.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
                stmt.setString(1, permission);

                if (fileId != null) {
                    stmt.setLong(2, fileId);
                } else {
                    stmt.setLong(2, folderId);
                }

                if (userId != null) {
                    stmt.setLong(3, userId);
                } else {
                    stmt.setLong(3, groupId);
                }

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    result.put("success", true);
                    result.put("message", "权限更新成功");
                } else {
                    result.put("success", false);
                    result.put("message", "权限更新失败");
                }
            }
        } catch (Exception e) {
            System.err.println("更新现有权限失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 获取项目信息
     */
    private Map<String, Object> getItemInfo(Long fileId, Long folderId, Connection conn) {
        Map<String, Object> itemInfo = new HashMap<>();

        try {
            if (fileId != null) {
                String sql = "SELECT file_name FROM file WHERE file_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, fileId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            itemInfo.put("id", fileId);
                            itemInfo.put("name", rs.getString("file_name"));
                            itemInfo.put("type", "file");
                        }
                    }
                }
            } else if (folderId != null) {
                String sql = "SELECT folder_name FROM folder WHERE folder_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, folderId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            itemInfo.put("id", folderId);
                            itemInfo.put("name", rs.getString("folder_name"));
                            itemInfo.put("type", "folder");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("获取项目信息失败: " + e.getMessage());
        }

        return itemInfo;
    }

    /**
     * 获取目标信息（用户或用户组）
     */
    private Map<String, Object> getTargetInfo(Long userId, Long groupId, Connection conn) {
        Map<String, Object> targetInfo = new HashMap<>();

        try {
            if (userId != null) {
                String sql = "SELECT username FROM user WHERE user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, userId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            targetInfo.put("type", "user");
                            targetInfo.put("id", userId);
                            targetInfo.put("name", rs.getString("username"));
                        }
                    }
                }
            } else if (groupId != null) {
                String sql = "SELECT name FROM user_group WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, groupId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            targetInfo.put("type", "group");
                            targetInfo.put("id", groupId);
                            targetInfo.put("name", rs.getString("name"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("获取目标信息失败: " + e.getMessage());
        }

        return targetInfo;
    }

    /**
     * 检查用户是否可以修改权限
     */
    private boolean canModifyPermission(Long permissionId, Long userId) {
        try {
            String sql = "SELECT a.file_id, a.folder_id FROM acl a WHERE a.id = ?";
            Connection conn = databaseService.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, permissionId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Long fileId = rs.getObject("file_id", Long.class);
                        Long folderId = rs.getObject("folder_id", Long.class);
                        return isOwner(userId, fileId, folderId);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("检查修改权限失败: " + e.getMessage());
        }
        return false;
    }

    /**
     * 获取权限信息
     */
    private Map<String, Object> getPermissionInfo(Long permissionId) {
        Map<String, Object> permissionInfo = new HashMap<>();

        try {
            String sql = "SELECT a.file_id, a.folder_id, a.user_id, a.group_id, a.permission, " +
                        "u.username, ug.name as group_name " +
                        "FROM acl a " +
                        "LEFT JOIN user u ON a.user_id = u.user_id " +
                        "LEFT JOIN user_group ug ON a.group_id = ug.id " +
                        "WHERE a.id = ?";

            Connection conn = databaseService.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, permissionId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        permissionInfo.put("permission", rs.getString("permission"));

                        Long fileId = rs.getObject("file_id", Long.class);
                        Long folderId = rs.getObject("folder_id", Long.class);
                        permissionInfo.put("item_info", getItemInfo(fileId, folderId, conn));

                        Long userId = rs.getObject("user_id", Long.class);
                        Long groupId = rs.getObject("group_id", Long.class);
                        permissionInfo.put("granted_to", getTargetInfo(userId, groupId, conn));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("获取权限信息失败: " + e.getMessage());
        }

        return permissionInfo;
    }

    /**
     * 获取权限总数
     */
    private int getTotalPermissionCount(Long fileId, Long folderId, Connection conn) throws SQLException {
        String sql;
        if (fileId != null) {
            sql = "SELECT COUNT(*) FROM acl WHERE file_id = ?";
        } else {
            sql = "SELECT COUNT(*) FROM acl WHERE folder_id = ?";
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (fileId != null) {
                stmt.setLong(1, fileId);
            } else {
                stmt.setLong(1, folderId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
