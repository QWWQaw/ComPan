package cloud.compan.servlet.entity;

import java.sql.Timestamp;

/**
 * 分享实体类
 * 对应数据库表：share
 */
public class Share {
    private Integer id;               // id INT PRIMARY KEY AUTO_INCREMENT
    private String shareLink;         // share_link VARCHAR(255) UNIQUE NOT NULL
    private Long fileId;              // file_id BIGINT UNSIGNED DEFAULT NULL
    private Long folderId;            // folder_id BIGINT UNSIGNED DEFAULT NULL
    private Long createdBy;           // created_by BIGINT UNSIGNED NOT NULL
    private String password;          // password VARCHAR(255) DEFAULT NULL
    private Timestamp expireAt;       // expire_at TIMESTAMP DEFAULT NULL
    private Timestamp createdAt;      // created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

    // 构造函数
    public Share() {}

    public Share(String shareLink, Long createdBy) {
        this.shareLink = shareLink;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getShareLink() { return shareLink; }
    public void setShareLink(String shareLink) { this.shareLink = shareLink; }

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Timestamp getExpireAt() { return expireAt; }
    public void setExpireAt(Timestamp expireAt) { this.expireAt = expireAt; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Share{" +
                "id=" + id +
                ", shareLink='" + shareLink + '\'' +
                ", fileId=" + fileId +
                ", folderId=" + folderId +
                ", createdBy=" + createdBy +
                ", expireAt=" + expireAt +
                '}';
    }
}
