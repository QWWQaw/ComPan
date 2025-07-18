package cloud.compan.servlet.entity;

import java.sql.Timestamp;

/**
 * 用户实体类
 * 对应数据库表：user
 */
public class User {
    private Long userId;              // user_id BIGINT UNSIGNED
    private String username;          // username VARCHAR(100)
    private String email;             // email VARCHAR(255)
    private String passwordHash;      // password_hash VARCHAR(255)
    private Long storageLimit;        // storage_limit BIGINT DEFAULT 10737418240
    private Long storageUsed;         // storage_used BIGINT DEFAULT 0
    private String status;            // status ENUM('active', 'inactive', 'banned') DEFAULT 'active'
    private Timestamp createdAt;      // created_at TIMESTAMP
    private Timestamp updatedAt;      // updated_at TIMESTAMP

    // 构造函数
    public User() {}

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.storageLimit = 10737418240L; // 10GB 默认
        this.storageUsed = 0L;
        this.status = "active";
    }

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Long getStorageLimit() { return storageLimit; }
    public void setStorageLimit(Long storageLimit) { this.storageLimit = storageLimit; }

    public Long getStorageUsed() { return storageUsed; }
    public void setStorageUsed(Long storageUsed) { this.storageUsed = storageUsed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
