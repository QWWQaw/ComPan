package cloud.compan.servlet.entity;

/**
 * 访问控制列表实体类
 * 对应数据库表：acl
 */
public class Acl {
    private Integer id;               // id INT PRIMARY KEY AUTO_INCREMENT
    private Long fileId;              // file_id BIGINT UNSIGNED DEFAULT NULL
    private Long folderId;            // folder_id BIGINT UNSIGNED DEFAULT NULL
    private Long userId;              // user_id BIGINT UNSIGNED DEFAULT NULL
    private Long groupId;             // group_id BIGINT UNSIGNED DEFAULT NULL
    private String permission;        // permission VARCHAR(100) NOT NULL

    // 构造函数
    public Acl() {}

    public Acl(String permission) {
        this.permission = permission;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    @Override
    public String toString() {
        return "Acl{" +
                "id=" + id +
                ", fileId=" + fileId +
                ", folderId=" + folderId +
                ", userId=" + userId +
                ", groupId=" + groupId +
                ", permission='" + permission + '\'' +
                '}';
    }
}
