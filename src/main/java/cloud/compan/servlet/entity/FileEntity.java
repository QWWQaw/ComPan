package cloud.compan.servlet.entity;

import java.sql.Timestamp;

/**
 * 文件实体类
 * 对应数据库表：file
 */
public class FileEntity {
    private Long fileId;              // file_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT
    private Long uploaderId;          // uploader_id BIGINT UNSIGNED NOT NULL
    private Long folderId;            // folder_id BIGINT UNSIGNED NOT NULL
    private String fileName;          // file_name VARCHAR(255) NOT NULL
    private String mimeType;          // mime_type VARCHAR(128) NOT NULL
    private String objectHash;        // object_hash VARCHAR(255) NOT NULL
    private String status;            // status ENUM('active', 'deleted') NOT NULL DEFAULT 'active'
    private Timestamp createdAt;      // created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    private Timestamp updatedAt;      // updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    private Timestamp deletedAt;      // deleted_at TIMESTAMP NULL

    // 关联的存储对象信息（从storage_object表获取，不是数据库字段）
    private Long fileSize;            // 来自storage_object.size
    private String storagePath;       // 来自storage_object.storage_path

    // 构造函数
    public FileEntity() {}

    public FileEntity(String fileName, String mimeType, Long uploaderId, Long folderId, String objectHash) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.uploaderId = uploaderId;
        this.folderId = folderId;
        this.objectHash = objectHash;
        this.status = "active";
    }

    // Getters and Setters
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public Long getUploaderId() { return uploaderId; }
    public void setUploaderId(Long uploaderId) { this.uploaderId = uploaderId; }

    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getObjectHash() { return objectHash; }
    public void setObjectHash(String objectHash) { this.objectHash = objectHash; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Timestamp getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Timestamp deletedAt) { this.deletedAt = deletedAt; }

    // 关联字段的Getters and Setters（不是数据库字段）
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    @Override
    public String toString() {
        return "FileEntity{" +
                "fileId=" + fileId +
                ", fileName='" + fileName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", uploaderId=" + uploaderId +
                ", folderId=" + folderId +
                ", fileSize=" + fileSize +
                ", status='" + status + '\'' +
                '}';
    }
}
