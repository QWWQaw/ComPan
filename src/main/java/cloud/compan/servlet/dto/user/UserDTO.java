package cloud.compan.servlet.dto.user;

import java.sql.Timestamp;

/**
 * 用户信息更新请求DTO
 */
class UserUpdateDTO {
    private String username;
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
    public boolean isValid() {
        if (username != null && (username.length() < 3 || username.length() > 20)) {
            return false;
        }
        if (email != null && !isValidEmail(email)) {
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    @Override
    public String toString() {
        return "UserUpdateDTO{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
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
