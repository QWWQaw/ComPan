package cloud.compan.servlet.dto.file;

import java.sql.Timestamp;

/**
 * 文件信息DTO
 * 用于Service层返回文件详细信息
 */
public class FileInfoDTO {
    private Long fileId;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private Long folderId;
    private String folderName; // 可选，文件夹名称
    private Long userId;
    private String fileHash;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String downloadUrl; // 下载链接

    // 构造函数
    public FileInfoDTO() {}

    // Getters and Setters
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }

    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    /**
     * 格式化文件大小为人类可读格式
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";

        String[] units = {"B", "KB", "MB", "GB", "TB"};
        long size = fileSize;
        int unitIndex = 0;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return size + " " + units[unitIndex];
    }

    /**
     * 获取文件扩展名
     */
    public String getFileExtension() {
        if (fileName == null) return "";
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1).toLowerCase() : "";
    }
}
