package cloud.compan.servlet.service;

import cloud.compan.servlet.entity.FileEntity;
import cloud.compan.servlet.repository.FileRepository;
import cloud.compan.servlet.annotations.component.Service;

import javax.servlet.http.Part;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;

/**
 * 文件业务逻辑层 - 重构版本
 * 职责：处理文件上传、下载、管理等业务逻辑
 */
@Service
public class FileService {

    private final FileRepository fileRepository;
    private final DatabaseService databaseService;
    private final UserService userService;
    private static final String UPLOAD_DIR = "C:/uploads/";

    public FileService() {
        this.fileRepository = new FileRepository();
        this.databaseService = DatabaseService.getInstance();
        this.userService = new UserService();
        ensureUploadDirectoryExists();
    }

    // ==================== 核心文件操作方法 ====================

    /**
     * 文件上传 - 主要方法
     */
    public Map<String, Object> uploadFile(Long userId, Long folderId, Part filePart, String description) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 参数验证
            String validationError = validateUploadParameters(filePart, userId);
            if (validationError != null) {
                return createErrorResult(validationError, 400);
            }

            String originalFileName = filePart.getSubmittedFileName();
            long fileSize = filePart.getSize();

            // 2. 业务规则验证
            if (!userService.hasEnoughStorage(userId, fileSize)) {
                return createErrorResult("存储空间不足", 400);
            }

            if (isFileNameExists(originalFileName, folderId, userId)) {
                return createErrorResult("文件名已存在", 409);
            }

            // 3. 文件处理
            String fileHash = calculateFileHash(filePart.getInputStream());
            String storagePath = getOrCreateStoragePath(filePart, fileHash);

            // 4. 数据库操作
            Long fileId = saveFileToDatabase(originalFileName, storagePath, fileSize,
                                           fileHash, userId, folderId, description);

            if (fileId != null) {
                userService.updateStorageUsage(userId, fileSize);
                return createSuccessResult("文件上传成功", createFileResponseData(fileId, originalFileName, fileSize, folderId));
            } else {
                return createErrorResult("文件保存失败", 500);
            }

        } catch (Exception e) {
            System.err.println("文件上传失败: " + e.getMessage());
            return createErrorResult("文件上传失败", 500);
        }
    }

    /**
     * 文件下载
     */
    public void downloadFile(Long userId, Long fileId, HttpServletResponse response) throws Exception {
        try {
            // 1. 权限验证
            if (!hasFileAccess(fileId, userId)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权限访问该文件");
                return;
            }

            // 2. 获取文件信息
            Map<String, Object> fileInfo = getFileInfoFromDatabase(fileId);
            if (fileInfo == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
                return;
            }

            // 3. 文件传输
            String fileName = (String) fileInfo.get("file_name");
            String storagePath = (String) fileInfo.get("storage_path");
            String mimeType = (String) fileInfo.get("mime_type");

            setDownloadResponseHeaders(response, fileName, mimeType);
            transferFileContent(storagePath, response.getOutputStream());

        } catch (Exception e) {
            System.err.println("文件下载失败: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件下载失败");
        }
    }

    /**
     * 获取文件列表
     */
    public Map<String, Object> getFileList(Long userId, Long folderId, int page, int size) {
        try {
            String sql = buildFileListQuery(folderId);
            List<Map<String, Object>> files = executeFileListQuery(sql, userId, folderId, page, size);
            int totalCount = getTotalFileCount(userId, folderId);

            Map<String, Object> data = new HashMap<>();
            data.put("files", files);
            data.put("pagination", createPaginationInfo(page, size, totalCount));

            return createSuccessResult("获取文件列表成功", data);

        } catch (Exception e) {
            System.err.println("获取文件列表失败: " + e.getMessage());
            return createErrorResult("获取文件列表失败", 500);
        }
    }

    /**
     * 获取文件详情
     */
    public Map<String, Object> getFileDetails(Long userId, Long fileId) {
        try {
            if (!hasFileAccess(fileId, userId)) {
                return createErrorResult("文件不存在或无权限", 404);
            }

            Map<String, Object> fileInfo = getFileInfoFromDatabase(fileId);
            if (fileInfo == null) {
                return createErrorResult("文件不存在", 404);
            }

            return createSuccessResult("获取文件详情成功", fileInfo);

        } catch (Exception e) {
            System.err.println("获取文件详情失败: " + e.getMessage());
            return createErrorResult("获取文件详情失败", 500);
        }
    }

    /**
     * 重命名文件
     */
    public Map<String, Object> renameFile(Long userId, Long fileId, String newFileName) {
        try {
            // 1. 权限验证
            if (!hasFileAccess(fileId, userId)) {
                return createErrorResult("文件不存在或无权限", 404);
            }

            // 2. 参数验证
            if (newFileName == null || newFileName.trim().isEmpty()) {
                return createErrorResult("文件名不能为空", 400);
            }

            // 3. 检查文件名冲突
            Map<String, Object> fileInfo = getFileInfoFromDatabase(fileId);
            Long folderId = (Long) fileInfo.get("folder_id");

            if (isFileNameExists(newFileName, folderId, userId)) {
                return createErrorResult("文件名已存在", 409);
            }

            // 4. 更新文件名
            if (updateFileName(fileId, newFileName)) {
                fileInfo.put("file_name", newFileName);
                return createSuccessResult("文件重命名成功", fileInfo);
            } else {
                return createErrorResult("文件重命名失败", 500);
            }

        } catch (Exception e) {
            System.err.println("文件重命名失败: " + e.getMessage());
            return createErrorResult("文件重命名失败", 500);
        }
    }

    /**
     * 更新文件信息（重命名）
     */
    public Map<String, Object> updateFile(Long userId, Long fileId, String filename, String description) {
        // 目前只支持重命名，如果传入了filename就进行重命名
        if (filename != null && !filename.trim().isEmpty()) {
            return renameFile(userId, fileId, filename);
        }

        // 如果没有提供filename，返回当前文件信息
        return getFileDetails(userId, fileId);
    }

    /**
     * 移动文件
     */
    public Map<String, Object> moveFile(Long userId, Long fileId, Long targetFolderId) {
        try {
            // 1. 权限验证
            if (!hasFileAccess(fileId, userId)) {
                return createErrorResult("文件不存在或无权限", 404);
            }

            // 2. 验证目标文件夹
            if (targetFolderId != null && !folderExistsAndBelongsToUser(targetFolderId, userId)) {
                return createErrorResult("目标文件夹不存在或无权限", 404);
            }

            // 3. 检查文件名冲突
            Map<String, Object> fileInfo = getFileInfoFromDatabase(fileId);
            String fileName = (String) fileInfo.get("file_name");

            if (isFileNameExists(fileName, targetFolderId, userId)) {
                return createErrorResult("目标文件夹中已存在同名文件", 409);
            }

            // 4. 移动文件
            if (updateFileFolder(fileId, targetFolderId)) {
                Map<String, Object> result = new HashMap<>();
                result.put("file_id", fileId);
                result.put("old_folder_id", fileInfo.get("folder_id"));
                result.put("new_folder_id", targetFolderId);
                return createSuccessResult("文件移动成功", result);
            } else {
                return createErrorResult("文件移动失败", 500);
            }

        } catch (Exception e) {
            System.err.println("文件移动失败: " + e.getMessage());
            return createErrorResult("文件移动失败", 500);
        }
    }

    /**
     * 删除文件
     */
    public Map<String, Object> deleteFile(Long userId, Long fileId) {
        try {
            // 1. 权限验证
            if (!hasFileAccess(fileId, userId)) {
                return createErrorResult("文件不存在或无权限", 404);
            }

            // 2. 获取文件信息
            Map<String, Object> fileInfo = getFileInfoFromDatabase(fileId);
            Long fileSize = (Long) fileInfo.get("file_size");

            // 3. 删除文件记录（软删除）
            if (markFileAsDeleted(fileId)) {
                // 4. 更新用户存储使用量
                userService.updateStorageUsage(userId, -fileSize);

                Map<String, Object> result = new HashMap<>();
                result.put("file_id", fileId);
                result.put("file_name", fileInfo.get("file_name"));
                return createSuccessResult("文件删除成功", result);
            } else {
                return createErrorResult("文件删除失败", 500);
            }

        } catch (Exception e) {
            System.err.println("文件删除失败: " + e.getMessage());
            return createErrorResult("文件删除失败", 500);
        }
    }

    // ==================== 数据库操作方法 ====================

    /**
     * 保存文件到数据库
     */
    private Long saveFileToDatabase(String fileName, String storagePath, long fileSize,
                                   String fileHash, Long userId, Long folderId, String description) {
        String sql = """
            INSERT INTO file (file_name, mime_type, object_hash, folder_id, uploader_id, status, created_at, updated_at) 
            VALUES (?, ?, ?, ?, ?, 'active', NOW(), NOW())
            """;

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, fileName);
            stmt.setString(2, getMimeType(fileName));
            stmt.setString(3, fileHash);
            stmt.setObject(4, folderId);
            stmt.setLong(5, userId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("保存文件到数据库失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 从数据库获取文件信息
     */
    private Map<String, Object> getFileInfoFromDatabase(Long fileId) {
        String sql = """
            SELECT f.file_id, f.file_name, f.mime_type, f.folder_id, f.uploader_id, 
                   f.created_at, f.updated_at, s.size as file_size, s.storage_path
            FROM file f 
            LEFT JOIN storage_object s ON f.object_hash = s.hash 
            WHERE f.file_id = ? AND f.status = 'active'
            """;

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, fileId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("file_id", rs.getLong("file_id"));
                    fileInfo.put("file_name", rs.getString("file_name"));
                    fileInfo.put("mime_type", rs.getString("mime_type"));
                    fileInfo.put("folder_id", rs.getLong("folder_id"));
                    fileInfo.put("uploader_id", rs.getLong("uploader_id"));
                    fileInfo.put("file_size", rs.getLong("file_size"));
                    fileInfo.put("storage_path", rs.getString("storage_path"));
                    fileInfo.put("created_at", rs.getTimestamp("created_at"));
                    fileInfo.put("updated_at", rs.getTimestamp("updated_at"));
                    return fileInfo;
                }
            }
        } catch (SQLException e) {
            System.err.println("获取文件信息失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 检查文件访问权限
     */
    private boolean hasFileAccess(Long fileId, Long userId) {
        String sql = "SELECT COUNT(*) FROM file WHERE file_id = ? AND uploader_id = ? AND status = 'active'";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, fileId);
            stmt.setLong(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查文件权限失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查文件名是否存在
     */
    private boolean isFileNameExists(String fileName, Long folderId, Long userId) {
        String sql = folderId == null
            ? "SELECT COUNT(*) FROM file WHERE file_name = ? AND folder_id IS NULL AND uploader_id = ? AND status = 'active'"
            : "SELECT COUNT(*) FROM file WHERE file_name = ? AND folder_id = ? AND uploader_id = ? AND status = 'active'";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fileName);
            if (folderId == null) {
                stmt.setLong(2, userId);
            } else {
                stmt.setLong(2, folderId);
                stmt.setLong(3, userId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查文件名失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 更新文件名
     */
    private boolean updateFileName(Long fileId, String newFileName) {
        String sql = "UPDATE file SET file_name = ?, updated_at = NOW() WHERE file_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newFileName);
            stmt.setLong(2, fileId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新文件名失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 更新文件所在文件夹
     */
    private boolean updateFileFolder(Long fileId, Long folderId) {
        String sql = "UPDATE file SET folder_id = ?, updated_at = NOW() WHERE file_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, folderId);
            stmt.setLong(2, fileId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新文件文件夹失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 标记文件为已删除
     */
    private boolean markFileAsDeleted(Long fileId) {
        String sql = "UPDATE file SET status = 'deleted', deleted_at = NOW(), updated_at = NOW() WHERE file_id = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, fileId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("删除文件失败: " + e.getMessage());
            return false;
        }
    }

    // ==================== 文件操作辅助方法 ====================

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

        byte[] hashBytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 获取或创建存储路径
     */
    private String getOrCreateStoragePath(Part filePart, String fileHash) throws Exception {
        // 检查是否已存在相同哈希的文件
        String existingPath = getExistingStoragePath(fileHash);
        if (existingPath != null) {
            return existingPath;
        }

        // 创建新的存储路径
        String fileName = fileHash + "_" + System.currentTimeMillis();
        String storagePath = UPLOAD_DIR + fileName;

        try (InputStream input = filePart.getInputStream();
             FileOutputStream output = new FileOutputStream(storagePath)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }

        // 保存存储对象记录
        saveStorageObject(fileHash, storagePath, filePart.getSize());
        return storagePath;
    }

    /**
     * 获取已存在的存储路径
     */
    private String getExistingStoragePath(String fileHash) {
        String sql = "SELECT storage_path FROM storage_object WHERE hash = ?";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fileHash);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("storage_path");
                }
            }
        } catch (SQLException e) {
            System.err.println("查询存储路径失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 保存存储对象记录
     */
    private void saveStorageObject(String hash, String storagePath, long size) {
        String sql = "INSERT INTO storage_object (hash, storage_path, size, created_at) VALUES (?, ?, ?, NOW())";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hash);
            stmt.setString(2, storagePath);
            stmt.setLong(3, size);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("保存存储对象失败: " + e.getMessage());
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 验证上传参数
     */
    private String validateUploadParameters(Part filePart, Long userId) {
        if (filePart == null) {
            return "文件不能为空";
        }
        if (userId == null) {
            return "用户ID不能为空";
        }
        if (filePart.getSize() == 0) {
            return "文件为空";
        }
        if (filePart.getSubmittedFileName() == null || filePart.getSubmittedFileName().trim().isEmpty()) {
            return "文件名不能为空";
        }
        return null;
    }

    /**
     * 创建成功结果
     */
    private Map<String, Object> createSuccessResult(String message, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        result.put("data", data);
        return result;
    }

    /**
     * 创建错误结果
     */
    private Map<String, Object> createErrorResult(String message, int statusCode) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        result.put("status_code", statusCode);
        return result;
    }

    /**
     * 创建文件响应数据
     */
    private Map<String, Object> createFileResponseData(Long fileId, String fileName, long fileSize, Long folderId) {
        Map<String, Object> data = new HashMap<>();
        data.put("file_id", fileId);
        data.put("file_name", fileName);
        data.put("file_size", fileSize);
        data.put("folder_id", folderId);
        return data;
    }

    /**
     * 获取MIME类型
     */
    private String getMimeType(String fileName) {
        String extension = "";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        }

        return switch (extension) {
            case "txt" -> "text/plain";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "mp4" -> "video/mp4";
            case "mp3" -> "audio/mpeg";
            default -> "application/octet-stream";
        };
    }

    /**
     * 确保上传目录存在
     */
    private void ensureUploadDirectoryExists() {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    /**
     * 设置下载响应头
     */
    private void setDownloadResponseHeaders(HttpServletResponse response, String fileName, String mimeType) {
        response.setContentType(mimeType != null ? mimeType : "application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    }

    /**
     * 传输文件内容
     */
    private void transferFileContent(String storagePath, OutputStream outputStream) throws IOException {
        try (FileInputStream fis = new FileInputStream(storagePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * 构建文件列表查询SQL
     */
    private String buildFileListQuery(Long folderId) {
        String baseQuery = """
            SELECT f.file_id, f.file_name, f.mime_type, f.folder_id, f.created_at, f.updated_at, s.size as file_size
            FROM file f 
            LEFT JOIN storage_object s ON f.object_hash = s.hash 
            WHERE f.uploader_id = ? AND f.status = 'active'
            """;

        if (folderId != null) {
            baseQuery += " AND f.folder_id = ?";
        } else {
            baseQuery += " AND f.folder_id IS NULL";
        }

        return baseQuery + " ORDER BY f.created_at DESC LIMIT ? OFFSET ?";
    }

    /**
     * 执行文件列表查询
     */
    private List<Map<String, Object>> executeFileListQuery(String sql, Long userId, Long folderId, int page, int size) throws SQLException {
        List<Map<String, Object>> files = new ArrayList<>();

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            stmt.setLong(paramIndex++, userId);
            if (folderId != null) {
                stmt.setLong(paramIndex++, folderId);
            }
            stmt.setInt(paramIndex++, size);
            stmt.setInt(paramIndex, (page - 1) * size);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> file = new HashMap<>();
                    file.put("file_id", rs.getLong("file_id"));
                    file.put("file_name", rs.getString("file_name"));
                    file.put("mime_type", rs.getString("mime_type"));
                    file.put("folder_id", rs.getLong("folder_id"));
                    file.put("file_size", rs.getLong("file_size"));
                    file.put("created_at", rs.getTimestamp("created_at"));
                    file.put("updated_at", rs.getTimestamp("updated_at"));
                    files.add(file);
                }
            }
        }
        return files;
    }

    /**
     * 获取文件总数
     */
    private int getTotalFileCount(Long userId, Long folderId) {
        String sql = folderId == null
            ? "SELECT COUNT(*) FROM file WHERE uploader_id = ? AND folder_id IS NULL AND status = 'active'"
            : "SELECT COUNT(*) FROM file WHERE uploader_id = ? AND folder_id = ? AND status = 'active'";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            if (folderId != null) {
                stmt.setLong(2, folderId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("获取文件总数失败: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 创建分页信息
     */
    private Map<String, Object> createPaginationInfo(int page, int size, int total) {
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("current_page", page);
        pagination.put("per_page", size);
        pagination.put("total", total);
        pagination.put("total_pages", (total + size - 1) / size);
        pagination.put("has_next", page * size < total);
        pagination.put("has_prev", page > 1);
        return pagination;
    }

    /**
     * 检查文件夹是否存在且属于用户
     */
    private boolean folderExistsAndBelongsToUser(Long folderId, Long userId) {
        String sql = "SELECT COUNT(*) FROM folder WHERE folder_id = ? AND owner_id = ? AND status = 'active'";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, folderId);
            stmt.setLong(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查文件夹权限失败: " + e.getMessage());
            return false;
        }
    }
}
