package cloud.compan.servlet.dto.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

/**
 * 用户信息更新请求DTO
 */
public class UserUpdateDTO {
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    // 构造函数
    public UserUpdateDTO() {}

    public UserUpdateDTO(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // 验证方法
    public boolean hasUpdates() {
        return (username != null && !username.trim().isEmpty()) ||
               (email != null && !email.trim().isEmpty());
    }
}

/**
 * 用户密码修改请求DTO
 */
class UserPasswordChangeDTO {
    private String oldPassword;
    private String newPassword;

    // 构造函数
    public UserPasswordChangeDTO() {}

    public UserPasswordChangeDTO(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    // Getters and Setters
    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    // 验证方法
    public boolean isValid() {
        return oldPassword != null && !oldPassword.trim().isEmpty() &&
               newPassword != null && !newPassword.trim().isEmpty() &&
               newPassword.length() >= 6 && newPassword.length() <= 50;
    }

    @Override
    public String toString() {
        return "UserPasswordChangeDTO{" +
                "oldPassword='[PROTECTED]'" +
                ", newPassword='[PROTECTED]'" +
                '}';
    }
}

/**
 * 用户存储统计DTO
 */
class UserStorageStatsDTO {
    private Long storageLimit;
    private Long storageUsed;
    private Long storageAvailable;
    private Double usagePercentage;
    private Long fileCount;
    private Long folderCount;

    // 构造函数
    public UserStorageStatsDTO() {}

    public UserStorageStatsDTO(Long storageLimit, Long storageUsed) {
        this.storageLimit = storageLimit;
        this.storageUsed = storageUsed;
        this.storageAvailable = storageLimit - storageUsed;
        this.usagePercentage = storageLimit > 0 ? (double) storageUsed / storageLimit * 100 : 0;
    }

    // Getters and Setters
    public Long getStorageLimit() { return storageLimit; }
    public void setStorageLimit(Long storageLimit) { this.storageLimit = storageLimit; }

    public Long getStorageUsed() { return storageUsed; }
    public void setStorageUsed(Long storageUsed) { this.storageUsed = storageUsed; }

    public Long getStorageAvailable() { return storageAvailable; }
    public void setStorageAvailable(Long storageAvailable) { this.storageAvailable = storageAvailable; }

    public Double getUsagePercentage() { return usagePercentage; }
    public void setUsagePercentage(Double usagePercentage) { this.usagePercentage = usagePercentage; }

    public Long getFileCount() { return fileCount; }
    public void setFileCount(Long fileCount) { this.fileCount = fileCount; }

    public Long getFolderCount() { return folderCount; }
    public void setFolderCount(Long folderCount) { this.folderCount = folderCount; }

    /**
     * 格式化存储大小
     */
    public String getFormattedStorageLimit() {
        return formatBytes(storageLimit);
    }

    public String getFormattedStorageUsed() {
        return formatBytes(storageUsed);
    }

    public String getFormattedStorageAvailable() {
        return formatBytes(storageAvailable);
    }

    private String formatBytes(Long bytes) {
        if (bytes == null) return "0 B";

        String[] units = {"B", "KB", "MB", "GB", "TB"};
        long size = bytes;
        int unitIndex = 0;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return size + " " + units[unitIndex];
    }
}

/**
 * 用户活动日志DTO
 */
class UserActivityLogDTO {
    private Long logId;
    private String action;
    private String details;
    private String ipAddress;
    private String userAgent;
    private Timestamp createdAt;

    // 构造函数
    public UserActivityLogDTO() {}

    // Getters and Setters
    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
