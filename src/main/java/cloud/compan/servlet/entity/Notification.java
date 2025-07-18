package cloud.compan.servlet.entity;

import java.sql.Timestamp;

/**
 * 通知实体类
 * 对应数据库表：notification
 */
public class Notification {
    private Integer id;               // id INT PRIMARY KEY AUTO_INCREMENT
    private Long userId;              // user_id BIGINT UNSIGNED NOT NULL
    private String message;           // message VARCHAR(255) NOT NULL
    private Boolean isRead;           // is_read BOOLEAN DEFAULT FALSE
    private Timestamp createdAt;      // created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

    // 构造函数
    public Notification() {}

    public Notification(Long userId, String message) {
        this.userId = userId;
        this.message = message;
        this.isRead = false;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId=" + userId +
                ", message='" + message + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}
