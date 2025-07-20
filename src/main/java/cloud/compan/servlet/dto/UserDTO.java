package cloud.compan.servlet.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户数据传输对象
 * 用于控制层和服务层之间的用户数据传输
 */
@Data
public class UserDTO {
    private Long userId;
    private String username;
    private String email;
    private Long storageLimit;
    private Long storageUsed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 构造函数
    public UserDTO() {}
    
    public UserDTO(Long userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Long getStorageLimit() {
        return storageLimit;
    }
    
    public void setStorageLimit(Long storageLimit) {
        this.storageLimit = storageLimit;
    }
    
    public Long getStorageUsed() {
        return storageUsed;
    }
    
    public void setStorageUsed(Long storageUsed) {
        this.storageUsed = storageUsed;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // 工具方法
    public long getAvailableStorage() {
        if (storageLimit == null || storageUsed == null) {
            return 0;
        }
        return storageLimit - storageUsed;
    }
    
    public double getUsagePercentage() {
        if (storageLimit == null || storageUsed == null || storageLimit == 0) {
            return 0.0;
        }
        return (double) storageUsed / storageLimit * 100.0;
    }
} 