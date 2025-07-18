package cloud.compan.servlet.service;

import cloud.compan.servlet.entity.FileEntity;
import cloud.compan.servlet.entity.StorageObject;
import cloud.compan.servlet.repository.FileRepository;
import cloud.compan.servlet.annotations.component.Service;

import javax.servlet.http.Part;
import java.io.*;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * 文件业务逻辑层
 * 处理文件上传、下载、管理等业务逻辑
 */
@Service
public class FileService {

    private final FileRepository fileRepository;
    private static final String UPLOAD_DIR = "C:/uploads/"; // 文件上传目录

    public FileService() {
        this.fileRepository = new FileRepository();
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

            // 2. 检查文件名是否已存在
            if (fileRepository.isFileNameExists(fileName, folderId, userId)) {
                result.put("success", false);
                result.put("message", "文件名已存在");
                return result;
            }

            // 3. 读取文件内容并计算哈希值
            InputStream inputStream = filePart.getInputStream();
            byte[] fileBytes = inputStream.readAllBytes();
            String fileHash = calculateSHA256(fileBytes);
            long fileSize = fileBytes.length;

            // 4. 检查是否可以秒传
            StorageObject existingObject = fileRepository.getStorageObjectByHash(fileHash);
            String storagePath;

            if (existingObject != null) {
                // 秒传：文件已存在，直接引用
                storagePath = existingObject.getStoragePath();
                // 增加引用计数
                existingObject.setRefCount(existingObject.getRefCount() + 1);
            } else {
                // 新文件：保存到磁盘
                storagePath = UPLOAD_DIR + UUID.randomUUID().toString() + "_" + fileName;
                saveFileToDisk(fileBytes, storagePath);

                // 创建存储对象记录
                StorageObject storageObject = new StorageObject(fileHash, fileSize, storagePath);
                fileRepository.createStorageObject(storageObject);
            }

            // 5. 创建文件记录
            String mimeType = filePart.getContentType();
            FileEntity file = new FileEntity(fileName, mimeType, userId, folderId, fileHash);
            FileEntity createdFile = fileRepository.createFile(file);

            if (createdFile != null) {
                createdFile.setFileSize(fileSize);
                createdFile.setStoragePath(storagePath);

                result.put("success", true);
                result.put("message", "文件上传成功");
                result.put("data", Map.of(
                    "file_id", createdFile.getFileId(),
                    "file_name", createdFile.getFileName(),
                    "file_size", fileSize,
                    "mime_type", mimeType,
                    "upload_type", existingObject != null ? "快传" : "普通上传"
                ));
            } else {
                result.put("success", false);
                result.put("message", "文件上传失败");
            }

        } catch (Exception e) {
            System.err.println("文件上传业务逻辑异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "文件上传失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 获取文件列表
     */
    public Map<String, Object> getFileList(Long userId, Long folderId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<FileEntity> files = fileRepository.getFilesByFolder(folderId, userId);

            result.put("success", true);
            result.put("message", "获取文件列表成功");
            result.put("data", Map.of(
                "files", files,
                "total", files.size(),
                "folder_id", folderId
            ));

        } catch (Exception e) {
            System.err.println("获取文件列表业务逻辑异常: " + e.getMessage());
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
            FileEntity file = fileRepository.getFileById(fileId, userId);

            if (file != null) {
                result.put("success", true);
                result.put("message", "获取文件详情成功");
                result.put("data", file);
            } else {
                result.put("success", false);
                result.put("message", "文件不存在或无权限访问");
            }

        } catch (Exception e) {
            System.err.println("获取文件详情业务逻辑异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取文件详情失败");
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

            // 2. 获取原文件信息
            FileEntity file = fileRepository.getFileById(fileId, userId);
            if (file == null) {
                result.put("success", false);
                result.put("message", "文件不存在或无权限访问");
                return result;
            }

            // 3. 检查新文件名是否已存在
            if (fileRepository.isFileNameExists(newName, file.getFolderId(), userId)) {
                result.put("success", false);
                result.put("message", "文件名已存在");
                return result;
            }

            // 4. 执行重命名
            boolean success = fileRepository.renameFile(fileId, newName, userId);

            if (success) {
                result.put("success", true);
                result.put("message", "文件重命名成功");
                result.put("data", Map.of(
                    "file_id", fileId,
                    "old_name", file.getFileName(),
                    "new_name", newName
                ));
            } else {
                result.put("success", false);
                result.put("message", "文件重命名失败");
            }

        } catch (Exception e) {
            System.err.println("重命名文件业务逻辑异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "重命名文件失败");
        }

        return result;
    }

    /**
     * 移动文件
     */
    public Map<String, Object> moveFile(Long fileId, Long targetFolderId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 获取原文件信息
            FileEntity file = fileRepository.getFileById(fileId, userId);
            if (file == null) {
                result.put("success", false);
                result.put("message", "文件不存在或无权限访问");
                return result;
            }

            // 2. 检查目标文件夹中是否有同名文件
            if (fileRepository.isFileNameExists(file.getFileName(), targetFolderId, userId)) {
                result.put("success", false);
                result.put("message", "目标文件夹中已存在同名文件");
                return result;
            }

            // 3. 执行移动
            boolean success = fileRepository.moveFile(fileId, targetFolderId, userId);

            if (success) {
                result.put("success", true);
                result.put("message", "文件移动成功");
                result.put("data", Map.of(
                    "file_id", fileId,
                    "old_folder_id", file.getFolderId(),
                    "new_folder_id", targetFolderId
                ));
            } else {
                result.put("success", false);
                result.put("message", "文件移动失败");
            }

        } catch (Exception e) {
            System.err.println("移动文件业务逻辑异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "移动文件失败");
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
            FileEntity file = fileRepository.getFileById(fileId, userId);
            if (file == null) {
                result.put("success", false);
                result.put("message", "文件不存在或无权限访问");
                return result;
            }

            // 2. 执行删除（软删除）
            boolean success = fileRepository.deleteFile(fileId, userId);

            if (success) {
                result.put("success", true);
                result.put("message", "文件已移入回收站");
                result.put("data", Map.of(
                    "file_id", fileId,
                    "file_name", file.getFileName()
                ));
            } else {
                result.put("success", false);
                result.put("message", "删除文件失败");
            }

        } catch (Exception e) {
            System.err.println("删除文件业务逻辑异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "删除文件失败");
        }

        return result;
    }

    /**
     * 下载文件
     */
    public FileEntity getFileForDownload(Long fileId, Long userId) {
        return fileRepository.getFileById(fileId, userId);
    }

    /**
     * 计算文件SHA256哈希值
     */
    private String calculateSHA256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
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
     * 保存文件到磁盘
     */
    private void saveFileToDisk(byte[] fileData, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(fileData);
        }
    }
}
