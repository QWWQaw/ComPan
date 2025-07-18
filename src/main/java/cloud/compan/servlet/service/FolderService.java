package cloud.compan.servlet.service;

import cloud.compan.servlet.entity.Folder;
import cloud.compan.servlet.repository.FolderRepository;
import cloud.compan.servlet.annotations.component.Service;

import java.sql.*;
import java.util.*;

/**
 * 文件夹业务逻辑层
 * 处理文件夹相关的业务逻辑
 */
@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private DatabaseService databaseService;

    public FolderService() {
        this.folderRepository = new FolderRepository();
        this.databaseService = DatabaseService.getInstance();
    }

    /**
     * 创建文件夹
     */
    public Map<String, Object> createFolder(String folderName, Long parentId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证输入参数
            if (folderName == null || folderName.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件夹名称不能为空");
                return result;
            }

            // 2. 检查同级目录下是否存在同名文件夹
            if (isFolderNameExists(folderName, parentId, userId)) {
                result.put("success", false);
                result.put("message", "文件夹名称已存在");
                return result;
            }

            String sql = "INSERT INTO folder (folder_name, parent_id, user_id, created_at, updated_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, folderName);
                if (parentId != null) {
                    stmt.setLong(2, parentId);
                } else {
                    stmt.setNull(2, Types.BIGINT);
                }
                stmt.setLong(3, userId);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        Long folderId = rs.getLong(1);

                        Map<String, Object> folderData = new HashMap<>();
                        folderData.put("folder_id", folderId);
                        folderData.put("folder_name", folderName);
                        folderData.put("parent_id", parentId);
                        folderData.put("user_id", userId);

                        result.put("success", true);
                        result.put("message", "文件夹创建成功");
                        result.put("data", folderData);
                    }
                } else {
                    result.put("success", false);
                    result.put("message", "文件夹创建失败");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("创建文件夹失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 获取文件夹列表
     */
    public Map<String, Object> getFolderList(Long parentId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT folder_id, folder_name, parent_id, user_id, created_at, updated_at FROM folder WHERE parent_id = ? AND user_id = ? ORDER BY folder_name";
            if (parentId == null) {
                sql = "SELECT folder_id, folder_name, parent_id, user_id, created_at, updated_at FROM folder WHERE parent_id IS NULL AND user_id = ? ORDER BY folder_name";
            }

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);

                if (parentId == null) {
                    stmt.setLong(1, userId);
                } else {
                    stmt.setLong(1, parentId);
                    stmt.setLong(2, userId);
                }

                rs = stmt.executeQuery();

                List<Map<String, Object>> folders = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> folder = new HashMap<>();
                    folder.put("folder_id", rs.getLong("folder_id"));
                    folder.put("folder_name", rs.getString("folder_name"));
                    folder.put("parent_id", rs.getLong("parent_id"));
                    folder.put("user_id", rs.getLong("user_id"));
                    folder.put("created_at", rs.getTimestamp("created_at"));
                    folder.put("updated_at", rs.getTimestamp("updated_at"));
                    folders.add(folder);
                }

                result.put("success", true);
                result.put("message", "获取文件夹列表成功");
                result.put("data", folders);

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取文件夹列表失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 获取文件夹详情
     */
    public Map<String, Object> getFolderInfo(Long folderId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Folder folder = folderRepository.getFolderById(folderId, userId);

            if (folder != null) {
                result.put("success", true);
                result.put("message", "获取文件夹详情成功");
                result.put("data", folder);
            } else {
                result.put("success", false);
                result.put("message", "文件夹不存在或无权限访问");
            }

        } catch (Exception e) {
            System.err.println("获取文件夹详情业务逻辑异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取文件夹详情失败");
        }

        return result;
    }

    /**
     * 重命名文件夹
     */
    public Map<String, Object> renameFolder(Long folderId, String newName, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证输入参数
            if (newName == null || newName.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件夹名称不能为空");
                return result;
            }

            // 2. 获取原文件夹信息
            Map<String, Object> folderInfo = getFolderById(folderId, userId);
            if (folderInfo == null) {
                result.put("success", false);
                result.put("message", "文件夹不存在");
                return result;
            }

            Long parentId = (Long) folderInfo.get("parent_id");

            // 3. 检查新名称是否与同级文件夹冲突
            if (isFolderNameExists(newName, parentId, userId)) {
                result.put("success", false);
                result.put("message", "文件夹名称已存在");
                return result;
            }

            String sql = "UPDATE folder SET folder_name = ?, updated_at = CURRENT_TIMESTAMP WHERE folder_id = ? AND user_id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, newName);
                stmt.setLong(2, folderId);
                stmt.setLong(3, userId);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    result.put("success", true);
                    result.put("message", "文件夹重命名成功");
                } else {
                    result.put("success", false);
                    result.put("message", "文件夹重命名失败");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("重命名文件夹失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 移动文件夹
     */
    public Map<String, Object> moveFolder(Long folderId, Long targetParentId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 获取原文件夹信息
            Folder folder = folderRepository.getFolderById(folderId, userId);
            if (folder == null) {
                result.put("success", false);
                result.put("message", "文件夹不存在或无权限访问");
                return result;
            }

            // 2. 检查目标文件夹是否存在（如果不是移动到根目录）
            if (targetParentId != null) {
                Folder targetFolder = folderRepository.getFolderById(targetParentId, userId);
                if (targetFolder == null) {
                    result.put("success", false);
                    result.put("message", "目标文件夹不存在");
                    return result;
                }
            }

            // 3. 检查目标位置是否有同名文件夹
            if (folderRepository.isFolderNameExists(folder.getFolderName(), targetParentId, userId)) {
                result.put("success", false);
                result.put("message", "目标位置已存在同名文件夹");
                return result;
            }

            // 4. 执行移动
            boolean success = folderRepository.moveFolder(folderId, targetParentId, userId);

            if (success) {
                result.put("success", true);
                result.put("message", "文件夹移动成功");
                result.put("data", Map.of(
                    "folder_id", folderId,
                    "old_parent_id", folder.getParentFolderId(),
                    "new_parent_id", targetParentId
                ));
            } else {
                result.put("success", false);
                result.put("message", "文件夹移动失败");
            }

        } catch (Exception e) {
            System.err.println("移动文件夹业务逻辑异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "移动文件夹失败");
        }

        return result;
    }

    /**
     * 删除文件夹
     */
    public Map<String, Object> deleteFolder(Long folderId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 获取文件夹信息
            Folder folder = folderRepository.getFolderById(folderId, userId);
            if (folder == null) {
                result.put("success", false);
                result.put("message", "文件夹不存在或无权限访问");
                return result;
            }

            // 2. 检查文件夹是否为空
            if (!isFolderEmpty(folderId, userId)) {
                result.put("success", false);
                result.put("message", "文件夹不为空，无法删除");
                return result;
            }

            // 3. 执行删除（软删除）
            boolean success = folderRepository.deleteFolder(folderId, userId);

            if (success) {
                result.put("success", true);
                result.put("message", "文件夹已移入回收站");
                result.put("data", Map.of(
                    "folder_id", folderId,
                    "folder_name", folder.getFolderName()
                ));
            } else {
                result.put("success", false);
                result.put("message", "删除文件夹失败");
            }

        } catch (Exception e) {
            System.err.println("删除文件夹业务逻辑异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除文件夹失败");
        }

        return result;
    }

    // ================== 私有辅助方法 ==================

    /**
     * 检查文件夹名称是否已存在
     */
    private boolean isFolderNameExists(String folderName, Long parentId, Long userId) throws SQLException {
        String sql;
        if (parentId == null) {
            sql = "SELECT COUNT(*) FROM folder WHERE folder_name = ? AND parent_id IS NULL AND user_id = ?";
        } else {
            sql = "SELECT COUNT(*) FROM folder WHERE folder_name = ? AND parent_id = ? AND user_id = ?";
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

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 根据ID获取文件夹信息
     */
    private Map<String, Object> getFolderById(Long folderId, Long userId) throws SQLException {
        String sql = "SELECT folder_id, folder_name, parent_id, user_id, created_at, updated_at FROM folder WHERE folder_id = ? AND user_id = ?";
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
                Map<String, Object> folder = new HashMap<>();
                folder.put("folder_id", rs.getLong("folder_id"));
                folder.put("folder_name", rs.getString("folder_name"));
                folder.put("parent_id", rs.getLong("parent_id"));
                folder.put("user_id", rs.getLong("user_id"));
                folder.put("created_at", rs.getTimestamp("created_at"));
                folder.put("updated_at", rs.getTimestamp("updated_at"));
                return folder;
            }
            return null;

        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 检查文件夹是否为空
     */
    private boolean isFolderEmpty(Long folderId, Long userId) throws SQLException {
        // 检查是否有子文件夹
        String folderSql = "SELECT COUNT(*) FROM folder WHERE parent_id = ? AND user_id = ?";
        // 检查是否有文件
        String fileSql = "SELECT COUNT(*) FROM file_entity WHERE folder_id = ? AND user_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();

            // 检查子文件夹
            stmt = conn.prepareStatement(folderSql);
            stmt.setLong(1, folderId);
            stmt.setLong(2, userId);
            rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return false;
            }

            rs.close();
            stmt.close();

            // 检查文件
            stmt = conn.prepareStatement(fileSql);
            stmt.setLong(1, folderId);
            stmt.setLong(2, userId);
            rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return false;
            }

            return true;

        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 验证用户是否有权限访问文件夹
     */
    public boolean hasAccessToFolder(Long folderId, Long userId) {
        try {
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

        } catch (Exception e) {
            System.err.println("检查文件夹权限失败: " + e.getMessage());
            return false;
        }
    }
}
