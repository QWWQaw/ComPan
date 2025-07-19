package cloud.compan.servlet.service;

import cloud.compan.servlet.repository.FolderRepository;
import cloud.compan.servlet.annotations.Service;

import java.sql.*;
import java.util.*;

/**
 * 文件夹业务逻辑层
 * 处理文件夹相关的业务逻辑
 */
@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final DatabaseService databaseService;

    public FolderService() {
        this.folderRepository = new FolderRepository();
        this.databaseService = DatabaseService.getInstance();
    }

    /**
     * 创建文件夹 - FolderHandler使用的方法
     */
    public Map<String, Object> createFolder(Long userId, String folderName, Long parentFolderId, String description) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证参数
            if (folderName == null || folderName.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件夹名不能为空");
                result.put("status_code", 400);
                return result;
            }

            // 2. 检查文件夹名是否已存在
            if (isFolderNameExists(folderName, parentFolderId, userId)) {
                result.put("success", false);
                result.put("message", "文件夹名已存在");
                result.put("status_code", 409);
                return result;
            }

            // 3. 创建文件夹记录
            String sql = "INSERT INTO folder (folder_name, parent_folder_id, owner_id, created_at, updated_at) " +
                        "VALUES (?, ?, ?, NOW(), NOW())";

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, folderName);
                if (parentFolderId != null) {
                    stmt.setLong(2, parentFolderId);
                } else {
                    stmt.setNull(2, Types.BIGINT);
                }
                stmt.setLong(3, userId);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        Long folderId = rs.getLong(1);

                        result.put("success", true);
                        result.put("message", "文件夹创建成功");
                        result.put("data", Map.of(
                            "folder_id", folderId,
                            "folder_name", folderName,
                            "parent_folder_id", parentFolderId,
                            "owner_id", userId
                        ));
                    }
                } else {
                    result.put("success", false);
                    result.put("message", "文件夹创建失败");
                    result.put("status_code", 500);
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("创建文件夹失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "创建文件夹失败");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 获取文件夹列表 - FolderHandler使用的方法
     */
    public Map<String, Object> getFolderList(Long userId, Long parentFolderId, int page, int size) {
        Map<String, Object> result = new HashMap<>();

        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM folder WHERE owner_id = ? AND status = 'active'");
            List<Object> params = new ArrayList<>();
            params.add(userId);

            if (parentFolderId != null) {
                sqlBuilder.append(" AND parent_folder_id = ?");
                params.add(parentFolderId);
            } else {
                sqlBuilder.append(" AND parent_folder_id IS NULL");
            }

            sqlBuilder.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
            params.add(size);
            params.add((page - 1) * size);

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sqlBuilder.toString());

                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }

                rs = stmt.executeQuery();

                List<Map<String, Object>> folders = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> folder = new HashMap<>();
                    folder.put("folder_id", rs.getLong("folder_id"));
                    folder.put("folder_name", rs.getString("folder_name"));
                    folder.put("parent_folder_id", rs.getLong("parent_folder_id"));
                    folder.put("owner_id", rs.getLong("owner_id"));
                    folder.put("size", rs.getLong("size"));
                    folder.put("status", rs.getString("status"));
                    folder.put("created_at", rs.getTimestamp("created_at"));
                    folder.put("updated_at", rs.getTimestamp("updated_at"));
                    folders.add(folder);
                }

                result.put("success", true);
                result.put("data", Map.of(
                    "folders", folders,
                    "page", page,
                    "size", size,
                    "total", getTotalFolderCount(userId, parentFolderId)
                ));

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取文件夹列表失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "获取文件夹列表失败");
        }

        return result;
    }

    /**
     * 获取文件夹详情 - FolderHandler使用的方法
     */
    public Map<String, Object> getFolderDetails(Long userId, Long folderId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT * FROM folder WHERE folder_id = ? AND owner_id = ? AND status = 'active'";
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
                    Map<String, Object> folderData = new HashMap<>();
                    folderData.put("folder_id", rs.getLong("folder_id"));
                    folderData.put("folder_name", rs.getString("folder_name"));
                    folderData.put("parent_folder_id", rs.getLong("parent_folder_id"));
                    folderData.put("owner_id", rs.getLong("owner_id"));
                    folderData.put("size", rs.getLong("size"));
                    folderData.put("status", rs.getString("status"));
                    folderData.put("created_at", rs.getTimestamp("created_at"));
                    folderData.put("updated_at", rs.getTimestamp("updated_at"));

                    result.put("success", true);
                    result.put("data", folderData);
                } else {
                    result.put("success", false);
                    result.put("message", "文件夹不存在或无权限");
                    result.put("status_code", 404);
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取文件夹详情失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "获取文件夹详情失败");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 重命名文件夹 - FolderHandler使用的方法
     */
    public Map<String, Object> renameFolder(Long userId, Long folderId, String newFolderName, String description) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证参数
            if (newFolderName == null || newFolderName.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件夹名不能为空");
                result.put("status_code", 400);
                return result;
            }

            // 2. 获取当前文件夹信息
            Map<String, Object> folderInfo = getFolderDetails(userId, folderId);
            if (!(Boolean) folderInfo.get("success")) {
                return folderInfo;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> folderData = (Map<String, Object>) folderInfo.get("data");
            Long parentFolderId = (Long) folderData.get("parent_folder_id");

            // 3. 检查新文件夹名是否冲突
            if (isFolderNameExistsExcludeCurrent(newFolderName, parentFolderId, userId, folderId)) {
                result.put("success", false);
                result.put("message", "文件夹名已存在");
                result.put("status_code", 409);
                return result;
            }

            // 4. 更新文件夹
            String sql = "UPDATE folder SET folder_name = ?, updated_at = NOW() " +
                        "WHERE folder_id = ? AND owner_id = ? AND status = 'active'";

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, newFolderName);
                stmt.setLong(2, folderId);
                stmt.setLong(3, userId);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    result.put("success", true);
                    result.put("message", "文件夹重命名成功");

                    // 返回更新后的文件夹信息
                    Map<String, Object> updatedFolder = getFolderDetails(userId, folderId);
                    if ((Boolean) updatedFolder.get("success")) {
                        result.put("data", updatedFolder.get("data"));
                    }
                } else {
                    result.put("success", false);
                    result.put("message", "文件夹重命名失败");
                    result.put("status_code", 500);
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("重命名文件夹失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "重命名文件夹失败");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 移动文件夹 - FolderHandler使用的方法
     */
    public Map<String, Object> moveFolder(Long userId, Long folderId, Long targetParentId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证文件夹是否存在且属于用户
            Map<String, Object> folderInfo = getFolderDetails(userId, folderId);
            if (!(Boolean) folderInfo.get("success")) {
                return folderInfo;
            }

            // 2. 检查是否是移动到自己或子文件夹（防止循环引用）
            if (targetParentId != null && isCircularReference(folderId, targetParentId)) {
                result.put("success", false);
                result.put("message", "不能移动到自己的子文件夹");
                result.put("status_code", 400);
                return result;
            }

            // 3. 检查目标位置是否有同名文件夹
            @SuppressWarnings("unchecked")
            Map<String, Object> folderData = (Map<String, Object>) folderInfo.get("data");
            String folderName = (String) folderData.get("folder_name");

            if (isFolderNameExists(folderName, targetParentId, userId)) {
                result.put("success", false);
                result.put("message", "目标位置已存在同名文件夹");
                result.put("status_code", 409);
                return result;
            }

            // 4. 移动文件夹
            String sql = "UPDATE folder SET parent_folder_id = ?, updated_at = NOW() " +
                        "WHERE folder_id = ? AND owner_id = ? AND status = 'active'";

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                if (targetParentId != null) {
                    stmt.setLong(1, targetParentId);
                } else {
                    stmt.setNull(1, Types.BIGINT);
                }
                stmt.setLong(2, folderId);
                stmt.setLong(3, userId);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    result.put("success", true);
                    result.put("message", "文件夹移动成功");
                    result.put("data", Map.of(
                        "folder_id", folderId,
                        "old_parent_id", folderData.get("parent_folder_id"),
                        "new_parent_id", targetParentId
                    ));
                } else {
                    result.put("success", false);
                    result.put("message", "文件夹移动失败");
                    result.put("status_code", 500);
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("移动文件夹失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "移动文件夹失败");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 删除文件夹 - FolderHandler使用的方法
     */
    public Map<String, Object> deleteFolder(Long userId, Long folderId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证文件夹是否存在且属于用户
            Map<String, Object> folderInfo = getFolderDetails(userId, folderId);
            if (!(Boolean) folderInfo.get("success")) {
                return folderInfo;
            }

            // 2. 检查文件夹是否为空
            if (!isFolderEmpty(folderId)) {
                result.put("success", false);
                result.put("message", "文件夹不为空，无法删除");
                result.put("status_code", 400);
                return result;
            }

            // 3. 软删除文件夹
            String sql = "UPDATE folder SET status = 'deleted', deleted_at = NOW(), updated_at = NOW() " +
                        "WHERE folder_id = ? AND owner_id = ? AND status = 'active'";

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, folderId);
                stmt.setLong(2, userId);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    result.put("success", true);
                    result.put("message", "文件夹删除成功");
                } else {
                    result.put("success", false);
                    result.put("message", "文件夹删除失败");
                    result.put("status_code", 500);
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("删除文件夹失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "删除文件夹失败");
            result.put("status_code", 500);
        }

        return result;
    }

    // ===== 辅助方法 =====

    /**
     * 检查文件夹名是否存在
     */
    private boolean isFolderNameExists(String folderName, Long parentId, Long userId) {
        String sql;
        if (parentId == null) {
            sql = "SELECT COUNT(*) FROM folder WHERE folder_name = ? AND parent_folder_id IS NULL AND owner_id = ? AND status = 'active'";
        } else {
            sql = "SELECT COUNT(*) FROM folder WHERE folder_name = ? AND parent_folder_id = ? AND owner_id = ? AND status = 'active'";
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, folderName);

            if (parentId == null) {
                stmt.setLong(2, userId);
            } else {
                stmt.setLong(2, parentId);
                stmt.setLong(3, userId);
            }

            rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (Exception e) {
            System.err.println("检查文件夹名称失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 检查文件夹名是否存在（排除当前文件夹）
     */
    private boolean isFolderNameExistsExcludeCurrent(String folderName, Long parentId, Long userId, Long currentFolderId) {
        String sql;
        if (parentId == null) {
            sql = "SELECT COUNT(*) FROM folder WHERE folder_name = ? AND parent_folder_id IS NULL AND owner_id = ? AND folder_id != ? AND status = 'active'";
        } else {
            sql = "SELECT COUNT(*) FROM folder WHERE folder_name = ? AND parent_folder_id = ? AND owner_id = ? AND folder_id != ? AND status = 'active'";
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, folderName);

            if (parentId == null) {
                stmt.setLong(2, userId);
                stmt.setLong(3, currentFolderId);
            } else {
                stmt.setLong(2, parentId);
                stmt.setLong(3, userId);
                stmt.setLong(4, currentFolderId);
            }

            rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (Exception e) {
            System.err.println("检查文件夹名称失败: " + e.getMessage());
            return false;
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 检查是否存在循环引用
     */
    private boolean isCircularReference(Long folderId, Long targetParentId) {
        if (folderId.equals(targetParentId)) {
            return true;
        }

        String sql = "SELECT parent_folder_id FROM folder WHERE folder_id = ? AND status = 'active'";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);

            Long currentId = targetParentId;
            while (currentId != null) {
                if (currentId.equals(folderId)) {
                    return true;
                }

                stmt.setLong(1, currentId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    currentId = rs.getLong("parent_folder_id");
                    if (rs.wasNull()) {
                        currentId = null;
                    }
                } else {
                    break;
                }
            }

            return false;

        } catch (Exception e) {
            System.err.println("检查循环引用失败: " + e.getMessage());
            return true; // 出错时保守处理，认为存在循环引用
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 获取文件夹总数
     */
    private int getTotalFolderCount(Long userId, Long parentFolderId) {
        String sql;
        if (parentFolderId == null) {
            sql = "SELECT COUNT(*) FROM folder WHERE owner_id = ? AND parent_folder_id IS NULL AND status = 'active'";
        } else {
            sql = "SELECT COUNT(*) FROM folder WHERE owner_id = ? AND parent_folder_id = ? AND status = 'active'";
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);

            if (parentFolderId != null) {
                stmt.setLong(2, parentFolderId);
            }

            rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;

        } catch (Exception e) {
            System.err.println("获取文件夹总数失败: " + e.getMessage());
            return 0;
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 检查文件夹是否为空
     */
    private boolean isFolderEmpty(Long folderId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();

            // 检查子文件夹
            String folderSql = "SELECT COUNT(*) FROM folder WHERE parent_folder_id = ? AND status = 'active'";
            stmt = conn.prepareStatement(folderSql);
            stmt.setLong(1, folderId);
            rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return false;
            }

            // 检查文件（使用正确的表名：file）
            String fileSql = "SELECT COUNT(*) FROM file WHERE folder_id = ? AND status = 'active'";
            stmt = conn.prepareStatement(fileSql);
            stmt.setLong(1, folderId);
            rs = stmt.executeQuery();

            return !(rs.next() && rs.getInt(1) > 0);

        } catch (Exception e) {
            System.err.println("检查文件夹是否为空失败: " + e.getMessage());
            return false; // 出错时保守处理，认为非空
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }
}
