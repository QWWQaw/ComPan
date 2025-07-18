package cloud.compan.servlet.service;

import cloud.compan.servlet.entity.FileEntity;
import cloud.compan.servlet.entity.StorageObject;
import cloud.compan.servlet.repository.FileRepository;
import cloud.compan.servlet.annotations.component.Service;

import javax.servlet.http.Part;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;

/**
 * 文件业务逻辑层
 * 处理文件上传、下载、管理等业务逻辑
 */
@Service
public class FileService {

    private final FileRepository fileRepository;
    private final DatabaseService databaseService;
    private final UserService userService;
    private static final String UPLOAD_DIR = "C:/uploads/"; // 文件上传目录

    public FileService() {
        this.fileRepository = new FileRepository();
        this.databaseService = DatabaseService.getInstance();
        this.userService = new UserService();
        // 确保上传目录存在
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    /**
     * 文件上传
     */
    public Map<String, Object> uploadFile(Part filePart, String fileName, Long userId, Long folderId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证参数
            if (filePart == null || fileName == null || fileName.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件或文件名不能为空");
                return result;
            }

            // 2. 获取文件大小
            long fileSize = filePart.getSize();
            if (fileSize == 0) {
                result.put("success", false);
                result.put("message", "文件为空");
                return result;
            }

            // 3. 检查用户存储空间
            if (!userService.hasEnoughStorage(userId, fileSize)) {
                result.put("success", false);
                result.put("message", "存储空间不足");
                return result;
            }

            // 4. 检查文件名是否已存在
            if (isFileNameExists(fileName, folderId, userId)) {
                result.put("success", false);
                result.put("message", "文件名已存在");
                return result;
            }

            // 5. 生成文件哈希值
            String fileHash = calculateFileHash(filePart.getInputStream());

            // 6. 检查是否已存在相同哈希的文件（去重）
            String existingStoragePath = getExistingFileByHash(fileHash);
            String storagePath;

            if (existingStoragePath != null) {
                // 文件已存在，使用现有存储路径
                storagePath = existingStoragePath;
            } else {
                // 新文件，保存到磁盘
                storagePath = saveFileToDisk(filePart, fileHash);
                if (storagePath == null) {
                    result.put("success", false);
                    result.put("message", "文件保存失败");
                    return result;
                }
            }

            // 7. 创建文件记录
            Map<String, Object> fileData = createFileRecord(fileName, storagePath, fileSize, fileHash, userId, folderId);
            if (fileData != null) {
                // 8. 更新用户存储使用量
                userService.updateStorageUsage(userId, fileSize);

                result.put("success", true);
                result.put("message", "文件上传成功");
                result.put("data", fileData);
            } else {
                result.put("success", false);
                result.put("message", "文件记录创建失败");
            }

        } catch (Exception e) {
            System.err.println("文件上传失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "文件上传失败");
        }

        return result;
    }

    /**
     * 获取文件列表
     */
    public Map<String, Object> getFileList(Long folderId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT f.file_id, f.file_name, f.file_size, f.mime_type, f.folder_id, f.user_id, f.created_at, f.updated_at, s.storage_path " +
                        "FROM file_entity f LEFT JOIN storage_object s ON f.storage_object_id = s.storage_object_id " +
                        "WHERE f.folder_id = ? AND f.user_id = ? ORDER BY f.file_name";

            if (folderId == null) {
                sql = "SELECT f.file_id, f.file_name, f.file_size, f.mime_type, f.folder_id, f.user_id, f.created_at, f.updated_at, s.storage_path " +
                     "FROM file_entity f LEFT JOIN storage_object s ON f.storage_object_id = s.storage_object_id " +
                     "WHERE f.folder_id IS NULL AND f.user_id = ? ORDER BY f.file_name";
            }

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);

                if (folderId == null) {
                    stmt.setLong(1, userId);
                } else {
                    stmt.setLong(1, folderId);
                    stmt.setLong(2, userId);
                }

                rs = stmt.executeQuery();

                List<Map<String, Object>> files = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> file = new HashMap<>();
                    file.put("file_id", rs.getLong("file_id"));
                    file.put("file_name", rs.getString("file_name"));
                    file.put("file_size", rs.getLong("file_size"));
                    file.put("mime_type", rs.getString("mime_type"));
                    file.put("folder_id", rs.getLong("folder_id"));
                    file.put("user_id", rs.getLong("user_id"));
                    file.put("created_at", rs.getTimestamp("created_at"));
                    file.put("updated_at", rs.getTimestamp("updated_at"));
                    file.put("storage_path", rs.getString("storage_path"));
                    files.add(file);
                }

                result.put("success", true);
                result.put("message", "获取文件列表成功");
                result.put("data", files);

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取文件列表失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取文件列表失败");
        }

        return result;
    }

    /**
     * 获取文件详情
     */
    public Map<String, Object> getFileInfo(Long fileId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT f.file_id, f.file_name, f.file_size, f.mime_type, f.folder_id, f.user_id, f.created_at, f.updated_at, " +
                        "s.storage_path, s.file_hash FROM file_entity f " +
                        "LEFT JOIN storage_object s ON f.storage_object_id = s.storage_object_id " +
                        "WHERE f.file_id = ? AND f.user_id = ?";

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
                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("file_id", rs.getLong("file_id"));
                    fileData.put("file_name", rs.getString("file_name"));
                    fileData.put("file_size", rs.getLong("file_size"));
                    fileData.put("mime_type", rs.getString("mime_type"));
                    fileData.put("folder_id", rs.getLong("folder_id"));
                    fileData.put("user_id", rs.getLong("user_id"));
                    fileData.put("created_at", rs.getTimestamp("created_at"));
                    fileData.put("updated_at", rs.getTimestamp("updated_at"));
                    fileData.put("storage_path", rs.getString("storage_path"));
                    fileData.put("file_hash", rs.getString("file_hash"));

                    result.put("success", true);
                    result.put("message", "获取文件信息成功");
                    result.put("data", fileData);
                } else {
                    result.put("success", false);
                    result.put("message", "文件不存在或无权限访问");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取文件信息失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取文件信息失败");
        }

        return result;
    }

    /**
     * 重命名文件
     */
    public Map<String, Object> renameFile(Long fileId, String newName, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证参数
            if (newName == null || newName.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件名不能为空");
                return result;
            }

            // 2. 获取文件信息
            Map<String, Object> fileInfo = getFileInfo(fileId, userId);
            if (!(Boolean) fileInfo.get("success")) {
                return fileInfo;
            }

            Map<String, Object> fileData = (Map<String, Object>) fileInfo.get("data");
            Long folderId = (Long) fileData.get("folder_id");

            // 3. 检查新文件名是否冲突
            if (isFileNameExists(newName, folderId, userId)) {
                result.put("success", false);
                result.put("message", "文件名已存在");
                return result;
            }

            // 4. 更新文件名
            String sql = "UPDATE file_entity SET file_name = ?, updated_at = CURRENT_TIMESTAMP WHERE file_id = ? AND user_id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, newName);
                stmt.setLong(2, fileId);
                stmt.setLong(3, userId);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    result.put("success", true);
                    result.put("message", "文件重命名成功");

                    // 返回更新后的文件信息
                    fileData.put("file_name", newName);
                    result.put("data", fileData);
                } else {
                    result.put("success", false);
                    result.put("message", "文件重命名失败");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("重命名文件失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "重命名文件失败");
        }

        return result;
    }

    /**
     * 删除文件
     */
    public Map<String, Object> deleteFile(Long fileId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 获取文件信息
            Map<String, Object> fileInfo = getFileInfo(fileId, userId);
            if (!(Boolean) fileInfo.get("success")) {
                return fileInfo;
            }

            Map<String, Object> fileData = (Map<String, Object>) fileInfo.get("data");
            Long fileSize = (Long) fileData.get("file_size");

            // 2. 删除文件记录
            String sql = "DELETE FROM file_entity WHERE file_id = ? AND user_id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, fileId);
                stmt.setLong(2, userId);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    // 3. 更新用户存储使用量
                    userService.updateStorageUsage(userId, -fileSize);

                    result.put("success", true);
                    result.put("message", "文件删除成功");
                    result.put("data", Map.of(
                        "file_id", fileId,
                        "file_name", fileData.get("file_name"),
                        "file_size", fileSize
                    ));
                } else {
                    result.put("success", false);
                    result.put("message", "文件删除失败");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("删除文件失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除文件失败");
        }

        return result;
    }

    /**
     * 获取文件用于下载
     */
    public Map<String, Object> getFileForDownload(Long fileId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证用户权限
            if (!hasAccessToFile(fileId, userId)) {
                result.put("success", false);
                result.put("message", "文件不存在或无权限访问");
                return result;
            }

            // 获取文件信息和存储路径
            String sql = "SELECT f.file_name, f.file_size, f.mime_type, s.storage_path FROM file_entity f " +
                        "LEFT JOIN storage_object s ON f.storage_object_id = s.storage_object_id " +
                        "WHERE f.file_id = ? AND f.user_id = ?";

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
                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("file_name", rs.getString("file_name"));
                    fileData.put("file_size", rs.getLong("file_size"));
                    fileData.put("mime_type", rs.getString("mime_type"));
                    fileData.put("storage_path", rs.getString("storage_path"));

                    result.put("success", true);
                    result.put("message", "获取文件下载信息成功");
                    result.put("data", fileData);
                } else {
                    result.put("success", false);
                    result.put("message", "文件不存在");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取文件下载信息失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取文件下载信息失败");
        }

        return result;
    }

    /**
     * 移动文件到指定文件夹
     */
    public Map<String, Object> moveFile(Long fileId, Long targetFolderId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证用户权限
            if (!hasAccessToFile(fileId, userId)) {
                result.put("success", false);
                result.put("message", "文件不存在或无权限访问");
                return result;
            }

            // 2. 获取原文件信息
            Map<String, Object> fileInfo = getFileInfo(fileId, userId);
            if (!(Boolean) fileInfo.get("success")) {
                return fileInfo;
            }

            Map<String, Object> fileData = (Map<String, Object>) fileInfo.get("data");
            String fileName = (String) fileData.get("file_name");
            Long oldFolderId = (Long) fileData.get("folder_id");

            // 3. 检查目标文件夹中是否有同名文件
            if (isFileNameExists(fileName, targetFolderId, userId)) {
                result.put("success", false);
                result.put("message", "目标文件夹中已存在同名文件");
                return result;
            }

            // 4. 更新文件的文件夹ID
            String sql = "UPDATE file_entity SET folder_id = ?, updated_at = CURRENT_TIMESTAMP WHERE file_id = ? AND user_id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                if (targetFolderId != null) {
                    stmt.setLong(1, targetFolderId);
                } else {
                    stmt.setNull(1, Types.BIGINT);
                }
                stmt.setLong(2, fileId);
                stmt.setLong(3, userId);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    result.put("success", true);
                    result.put("message", "文件移动成功");
                    result.put("data", Map.of(
                        "file_id", fileId,
                        "file_name", fileName,
                        "old_folder_id", oldFolderId,
                        "new_folder_id", targetFolderId
                    ));
                } else {
                    result.put("success", false);
                    result.put("message", "文件移动失败");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("移动文件失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "移动文件失败");
        }

        return result;
    }

    // ================== 私有辅助方法 ==================

    /**
     * 检查文件名是否已存在
     */
    private boolean isFileNameExists(String fileName, Long folderId, Long userId) throws SQLException {
        String sql;
        if (folderId == null) {
            sql = "SELECT COUNT(*) FROM file_entity WHERE file_name = ? AND folder_id IS NULL AND user_id = ?";
        } else {
            sql = "SELECT COUNT(*) FROM file_entity WHERE file_name = ? AND folder_id = ? AND user_id = ?";
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, fileName);

            if (folderId == null) {
                stmt.setLong(2, userId);
            } else {
                stmt.setLong(2, folderId);
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
     * 计算文件哈希值
     */
    private String calculateFileHash(InputStream inputStream) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            md.update(buffer, 0, bytesRead);
        }

        byte[] hash = md.digest();
        StringBuilder hexString = new StringBuilder();

        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    /**
     * 根据哈希值获取已存在的文件存储路径
     */
    private String getExistingFileByHash(String fileHash) throws SQLException {
        String sql = "SELECT storage_path FROM storage_object WHERE file_hash = ? LIMIT 1";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, fileHash);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("storage_path");
            }
            return null;

        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 保存文件到磁盘
     */
    private String saveFileToDisk(Part filePart, String fileHash) throws Exception {
        String fileName = fileHash + "_" + System.currentTimeMillis();
        String storagePath = UPLOAD_DIR + fileName;

        try (InputStream inputStream = filePart.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(storagePath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return storagePath;
        }
    }

    /**
     * 创建文件记录
     */
    private Map<String, Object> createFileRecord(String fileName, String storagePath, long fileSize, String fileHash, Long userId, Long folderId) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            conn.setAutoCommit(false);

            // 1. 创建或获取存储对象
            Long storageObjectId = createOrGetStorageObject(conn, storagePath, fileSize, fileHash);

            // 2. 创建文件实体记录
            String sql = "INSERT INTO file_entity (file_name, file_size, mime_type, storage_object_id, folder_id, user_id, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, fileName);
            stmt.setLong(2, fileSize);
            stmt.setString(3, getMimeType(fileName));
            stmt.setLong(4, storageObjectId);
            if (folderId != null) {
                stmt.setLong(5, folderId);
            } else {
                stmt.setNull(5, Types.BIGINT);
            }
            stmt.setLong(6, userId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    Long fileId = rs.getLong(1);

                    conn.commit();

                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("file_id", fileId);
                    fileData.put("file_name", fileName);
                    fileData.put("file_size", fileSize);
                    fileData.put("mime_type", getMimeType(fileName));
                    fileData.put("folder_id", folderId);
                    fileData.put("user_id", userId);
                    fileData.put("storage_path", storagePath);
                    fileData.put("file_hash", fileHash);

                    return fileData;
                }
            }

            conn.rollback();
            return null;

        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 创建或获取存储对象
     */
    private Long createOrGetStorageObject(Connection conn, String storagePath, long fileSize, String fileHash) throws SQLException {
        // 先查找是否存在
        String selectSql = "SELECT storage_object_id FROM storage_object WHERE file_hash = ?";
        PreparedStatement selectStmt = null;
        ResultSet rs = null;

        try {
            selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setString(1, fileHash);
            rs = selectStmt.executeQuery();

            if (rs.next()) {
                return rs.getLong("storage_object_id");
            }

        } finally {
            if (rs != null) rs.close();
            if (selectStmt != null) selectStmt.close();
        }

        // 不存在则创建
        String insertSql = "INSERT INTO storage_object (storage_path, file_size, file_hash, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        PreparedStatement insertStmt = null;
        ResultSet insertRs = null;

        try {
            insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, storagePath);
            insertStmt.setLong(2, fileSize);
            insertStmt.setString(3, fileHash);

            int affectedRows = insertStmt.executeUpdate();
            if (affectedRows > 0) {
                insertRs = insertStmt.getGeneratedKeys();
                if (insertRs.next()) {
                    return insertRs.getLong(1);
                }
            }

            throw new SQLException("创建存储对象失败");

        } finally {
            if (insertRs != null) insertRs.close();
            if (insertStmt != null) insertStmt.close();
        }
    }

    /**
     * 根据文件名获取MIME类型
     */
    private String getMimeType(String fileName) {
        String extension = "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        }

        switch (extension) {
            case "txt": return "text/plain";
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "mp4": return "video/mp4";
            case "mp3": return "audio/mpeg";
            case "zip": return "application/zip";
            case "rar": return "application/x-rar-compressed";
            default: return "application/octet-stream";
        }
    }

    /**
     * 验证用户是否有权限访问文件
     */
    public boolean hasAccessToFile(Long fileId, Long userId) {
        try {
            String sql = "SELECT COUNT(*) FROM file_entity WHERE file_id = ? AND user_id = ?";
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
            System.err.println("检查文件权限失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 处理文件上传业务逻辑
     */
    public Map<String, Object> handleFileUpload(Part filePart, String fileName, String folderIdStr, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 参数验证
            if (filePart == null) {
                result.put("success", false);
                result.put("message", "请选择要上传的文件");
                result.put("status_code", 400);
                return result;
            }

            // 获取文件大小
            long fileSize = filePart.getSize();
            if (fileSize == 0) {
                result.put("success", false);
                result.put("message", "文件为空");
                result.put("status_code", 400);
                return result;
            }

            // 如果没有指定文件名，使用原始文件名
            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = getFileName(filePart);
            }

            if (fileName == null || fileName.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件名不能为空");
                result.put("status_code", 400);
                return result;
            }

            // 解析文件夹ID
            Long folderId = null;
            if (folderIdStr != null && !folderIdStr.trim().isEmpty()) {
                try {
                    folderId = Long.parseLong(folderIdStr);
                } catch (NumberFormatException e) {
                    result.put("success", false);
                    result.put("message", "无效的文件夹ID");
                    result.put("status_code", 400);
                    return result;
                }
            }

            // 2. 调用原有的上传方法
            Map<String, Object> uploadResult = uploadFile(filePart, fileName, userId, folderId);

            if ((Boolean) uploadResult.get("success")) {
                result.put("success", true);
                result.put("message", "文件上传成功");
                result.put("status_code", 201);
                result.put("data", uploadResult.get("data"));
            } else {
                result.put("success", false);
                result.put("message", uploadResult.get("message"));
                result.put("status_code", 400);
            }

        } catch (Exception e) {
            System.err.println("文件上传业务逻辑处理失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 处理获取文件列表业务逻辑
     */
    public Map<String, Object> handleGetFileList(String folderIdStr, String pageStr, String perPageStr,
                                                  String sortBy, String sortOrder, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 参数解析和验证
            Long folderId = null;
            if (folderIdStr != null && !folderIdStr.trim().isEmpty()) {
                try {
                    folderId = Long.parseLong(folderIdStr);
                } catch (NumberFormatException e) {
                    result.put("success", false);
                    result.put("message", "无效的文件夹ID");
                    result.put("status_code", 400);
                    return result;
                }
            }

            int page = 1;
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                try {
                    page = Integer.parseInt(pageStr);
                    if (page < 1) page = 1;
                } catch (NumberFormatException e) {
                    // 使用默认值
                }
            }

            int perPage = 20;
            if (perPageStr != null && !perPageStr.trim().isEmpty()) {
                try {
                    perPage = Integer.parseInt(perPageStr);
                    if (perPage < 1) perPage = 20;
                    if (perPage > 100) perPage = 100; // 限制最大值
                } catch (NumberFormatException e) {
                    // 使用默认值
                }
            }

            // 2. 调用原有的获取文件列表方法
            Map<String, Object> listResult = getFileList(folderId, userId);

            if ((Boolean) listResult.get("success")) {
                List<Map<String, Object>> files = (List<Map<String, Object>>) listResult.get("data");

                // 3. 应用排序
                if (sortBy != null && !sortBy.trim().isEmpty()) {
                    files = sortFiles(files, sortBy, sortOrder);
                }

                // 4. 应用分页
                Map<String, Object> paginatedResult = applyPagination(files, page, perPage);

                result.put("success", true);
                result.put("message", "获取文件列表成功");
                result.put("status_code", 200);
                result.put("data", paginatedResult);
            } else {
                result.put("success", false);
                result.put("message", listResult.get("message"));
                result.put("status_code", 400);
            }

        } catch (Exception e) {
            System.err.println("获取文件列表业务逻辑处理失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 处理获取文件详情业务逻辑
     */
    public Map<String, Object> handleGetFileInfo(String fileIdStr, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 参数验证
            if (fileIdStr == null || fileIdStr.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件ID不能为空");
                result.put("status_code", 400);
                return result;
            }

            Long fileId;
            try {
                fileId = Long.parseLong(fileIdStr);
            } catch (NumberFormatException e) {
                result.put("success", false);
                result.put("message", "无效的文件ID");
                result.put("status_code", 400);
                return result;
            }

            // 2. 调用原有的获取文件信息方法
            Map<String, Object> fileResult = getFileInfo(fileId, userId);

            if ((Boolean) fileResult.get("success")) {
                result.put("success", true);
                result.put("message", "获取文件信息成功");
                result.put("status_code", 200);
                result.put("data", fileResult.get("data"));
            } else {
                result.put("success", false);
                result.put("message", fileResult.get("message"));
                result.put("status_code", 404);
            }

        } catch (Exception e) {
            System.err.println("获取文件详情业务逻辑处理失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 处理文件下载业务逻辑
     */
    public void handleFileDownload(String fileIdStr, Long userId, HttpServletResponse response) throws Exception {
        try {
            // 1. 参数验证
            if (fileIdStr == null || fileIdStr.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "文件ID不能为空");
                return;
            }

            Long fileId;
            try {
                fileId = Long.parseLong(fileIdStr);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的文件ID");
                return;
            }

            // 2. 获取文件信息
            Map<String, Object> fileResult = getFileForDownload(fileId, userId);

            if (!(Boolean) fileResult.get("success")) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, (String) fileResult.get("message"));
                return;
            }

            Map<String, Object> fileData = (Map<String, Object>) fileResult.get("data");
            String fileName = (String) fileData.get("file_name");
            Long fileSize = (Long) fileData.get("file_size");
            String mimeType = (String) fileData.get("mime_type");
            String storagePath = (String) fileData.get("storage_path");

            // 3. 设置响应头
            response.setContentType(mimeType != null ? mimeType : "application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setContentLengthLong(fileSize);

            // 4. 传输文件内容
            try (FileInputStream fis = new FileInputStream(storagePath);
                 OutputStream os = response.getOutputStream()) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

        } catch (FileNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
        } catch (Exception e) {
            System.err.println("文件下载业务逻辑处理失败: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件下载失败");
        }
    }

    /**
     * 处理文件重命名业务逻辑
     */
    public Map<String, Object> handleRenameFile(String fileIdStr, String newName, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 参数验证
            if (fileIdStr == null || fileIdStr.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件ID不能为空");
                result.put("status_code", 400);
                return result;
            }

            if (newName == null || newName.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件名不能为空");
                result.put("status_code", 400);
                return result;
            }

            Long fileId;
            try {
                fileId = Long.parseLong(fileIdStr);
            } catch (NumberFormatException e) {
                result.put("success", false);
                result.put("message", "无效的文件ID");
                result.put("status_code", 400);
                return result;
            }

            // 2. 调用原有的重命名方法
            Map<String, Object> renameResult = renameFile(fileId, newName, userId);

            if ((Boolean) renameResult.get("success")) {
                result.put("success", true);
                result.put("message", "文件重命名成功");
                result.put("status_code", 200);
                result.put("data", renameResult.get("data"));
            } else {
                result.put("success", false);
                result.put("message", renameResult.get("message"));
                result.put("status_code", 400);
            }

        } catch (Exception e) {
            System.err.println("文件重命名业务逻辑处理失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 处理移动文件业务逻辑
     */
    public Map<String, Object> handleMoveFile(String fileIdStr, String targetFolderIdStr, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 参数验证
            if (fileIdStr == null || fileIdStr.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件ID不能为空");
                result.put("status_code", 400);
                return result;
            }

            if (targetFolderIdStr == null || targetFolderIdStr.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "目标文件夹ID不能为空");
                result.put("status_code", 400);
                return result;
            }

            Long fileId;
            Long targetFolderId;
            try {
                fileId = Long.parseLong(fileIdStr);
                targetFolderId = Long.parseLong(targetFolderIdStr);
            } catch (NumberFormatException e) {
                result.put("success", false);
                result.put("message", "无效的文件ID或目标文件夹ID");
                result.put("status_code", 400);
                return result;
            }

            // 2. 调用原有的移动方法
            Map<String, Object> moveResult = moveFile(fileId, targetFolderId, userId);

            if ((Boolean) moveResult.get("success")) {
                result.put("success", true);
                result.put("message", "文件移动成功");
                result.put("status_code", 200);
                result.put("data", moveResult.get("data"));
            } else {
                result.put("success", false);
                result.put("message", moveResult.get("message"));
                result.put("status_code", 400);
            }

        } catch (Exception e) {
            System.err.println("文件移动业务逻辑处理失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 处理删除文件业务逻辑
     */
    public Map<String, Object> handleDeleteFile(String fileIdStr, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 参数验证
            if (fileIdStr == null || fileIdStr.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件ID不能为空");
                result.put("status_code", 400);
                return result;
            }

            Long fileId;
            try {
                fileId = Long.parseLong(fileIdStr);
            } catch (NumberFormatException e) {
                result.put("success", false);
                result.put("message", "无效的文件ID");
                result.put("status_code", 400);
                return result;
            }

            // 2. 调用原有的删除方法
            Map<String, Object> deleteResult = deleteFile(fileId, userId);

            if ((Boolean) deleteResult.get("success")) {
                result.put("success", true);
                result.put("message", "文件删除成功");
                result.put("status_code", 200);
                result.put("data", deleteResult.get("data"));
            } else {
                result.put("success", false);
                result.put("message", deleteResult.get("message"));
                result.put("status_code", 404);
            }

        } catch (Exception e) {
            System.err.println("文件删除业务逻辑处理失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 从文件上传请求中获取文件名
     */
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (String content : contentDisposition.split(";")) {
                if (content.trim().startsWith("filename")) {
                    return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
                }
            }
        }
        return null;
    }

    /**
     * 对文件列表进行排序
     */
    private List<Map<String, Object>> sortFiles(List<Map<String, Object>> files, String sortBy, String sortOrder) {
        try {
            boolean ascending = !"desc".equalsIgnoreCase(sortOrder);

            switch (sortBy.toLowerCase()) {
                case "name":
                    files.sort((f1, f2) -> {
                        String name1 = (String) f1.get("file_name");
                        String name2 = (String) f2.get("file_name");
                        int result = name1.compareToIgnoreCase(name2);
                        return ascending ? result : -result;
                    });
                    break;
                case "size":
                    files.sort((f1, f2) -> {
                        Long size1 = (Long) f1.get("file_size");
                        Long size2 = (Long) f2.get("file_size");
                        int result = size1.compareTo(size2);
                        return ascending ? result : -result;
                    });
                    break;
                case "created_at":
                    files.sort((f1, f2) -> {
                        Timestamp time1 = (Timestamp) f1.get("created_at");
                        Timestamp time2 = (Timestamp) f2.get("created_at");
                        int result = time1.compareTo(time2);
                        return ascending ? result : -result;
                    });
                    break;
                case "updated_at":
                    files.sort((f1, f2) -> {
                        Timestamp time1 = (Timestamp) f1.get("updated_at");
                        Timestamp time2 = (Timestamp) f2.get("updated_at");
                        int result = time1.compareTo(time2);
                        return ascending ? result : -result;
                    });
                    break;
                default:
                    // 默认按名称排序
                    files.sort((f1, f2) -> {
                        String name1 = (String) f1.get("file_name");
                        String name2 = (String) f2.get("file_name");
                        return name1.compareToIgnoreCase(name2);
                    });
            }
        } catch (Exception e) {
            System.err.println("文件排序失败: " + e.getMessage());
        }

        return files;
    }

    /**
     * 应用分页
     */
    private Map<String, Object> applyPagination(List<Map<String, Object>> files, int page, int perPage) {
        int total = files.size();
        int totalPages = (total + perPage - 1) / perPage;
        int offset = (page - 1) * perPage;

        List<Map<String, Object>> paginatedFiles;
        if (offset >= total) {
            paginatedFiles = new ArrayList<>();
        } else {
            int endIndex = Math.min(offset + perPage, total);
            paginatedFiles = files.subList(offset, endIndex);
        }

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current_page", page);
        pagination.put("per_page", perPage);
        pagination.put("total", total);
        pagination.put("total_pages", totalPages);
        pagination.put("has_next", page < totalPages);
        pagination.put("has_prev", page > 1);

        Map<String, Object> result = new HashMap<>();
        result.put("files", paginatedFiles);
        result.put("pagination", pagination);

        return result;
    }
}
