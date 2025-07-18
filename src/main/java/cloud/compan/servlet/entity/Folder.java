package cloud.compan.servlet.entity;

import java.sql.Timestamp;

/**
 * 文件夹实体类
 * 对应数据库表：folder
 */
public class Folder {
    private Long folderId;            // folder_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT
    private Long ownerId;             // owner_id BIGINT UNSIGNED NOT NULL
    private Long parentFolderId;      // parent_folder_id BIGINT UNSIGNED NULL
    private String folderName;        // folder_name VARCHAR(255) NOT NULL
    private Long size;                // size BIGINT NOT NULL DEFAULT 0
    private String status;            // status ENUM('active', 'deleted') NOT NULL DEFAULT 'active'
    private Timestamp createdAt;      // created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    private Timestamp updatedAt;      // updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    private Timestamp deletedAt;      // deleted_at TIMESTAMP NULL

    // 构造函数
    public Folder() {}

    public Folder(String folderName, Long ownerId, Long parentFolderId) {
        this.folderName = folderName;
        this.ownerId = ownerId;
        this.parentFolderId = parentFolderId;
        this.size = 0L;
        this.status = "active";
    }

    // Getters and Setters
    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Long getParentFolderId() { return parentFolderId; }
    public void setParentFolderId(Long parentFolderId) { this.parentFolderId = parentFolderId; }

    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public Timestamp getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Timestamp deletedAt) { this.deletedAt = deletedAt; }

    @Override
    public String toString() {
        return "Folder{" +
                "folderId=" + folderId +
                ", folderName='" + folderName + '\'' +
                ", ownerId=" + ownerId +
                ", parentFolderId=" + parentFolderId +
                ", status='" + status + '\'' +
                '}';
    }
}
