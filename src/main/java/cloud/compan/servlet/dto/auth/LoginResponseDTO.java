package cloud.compan.servlet.dto.auth;

import java.util.Map;

/**
 * 登录响应DTO
 * 用于Service层返回登录成功后的用户信息和令牌
 */
public class LoginResponseDTO {
    private String token;
    private long expiresIn;
    private UserInfoDTO user;

    // 构造函数
    public LoginResponseDTO() {}

    public LoginResponseDTO(String token, long expiresIn, UserInfoDTO user) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

    public UserInfoDTO getUser() { return user; }
    public void setUser(UserInfoDTO user) { this.user = user; }

    /**
     * 用户信息内嵌DTO
     */
    public static class UserInfoDTO {
        private Long userId;
        private String username;
        private String email;
        private Long storageLimit;
        private Long storageUsed;

        // 构造函数
        public UserInfoDTO() {}

        public UserInfoDTO(Long userId, String username, String email, Long storageLimit, Long storageUsed) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.storageLimit = storageLimit;
            this.storageUsed = storageUsed;
        }

        // 从Map创建
        public static UserInfoDTO fromMap(Map<String, Object> userMap) {
            UserInfoDTO dto = new UserInfoDTO();
            dto.setUserId((Long) userMap.get("user_id"));
            dto.setUsername((String) userMap.get("username"));
            dto.setEmail((String) userMap.get("email"));
            dto.setStorageLimit((Long) userMap.get("storage_limit"));
            dto.setStorageUsed((Long) userMap.get("storage_used"));
            return dto;
        }

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public Long getStorageLimit() { return storageLimit; }
        public void setStorageLimit(Long storageLimit) { this.storageLimit = storageLimit; }

        public Long getStorageUsed() { return storageUsed; }
        public void setStorageUsed(Long storageUsed) { this.storageUsed = storageUsed; }
    }
}
