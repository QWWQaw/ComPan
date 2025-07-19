package cloud.compan.servlet.dto.auth;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

/**
 * 登录响应DTO
 * 用于Service层返回登录成功后的用户信息和令牌
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    @NotBlank(message = "令牌不能为空")
    private String token;

    @Min(value = 1, message = "过期时间必须大于0")
    private long expiresIn;

    @Valid
    @NotNull(message = "用户信息不能为空")
    private UserInfoDTO user;

    /**
     * 用户信息内嵌DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDTO {
        @NotNull(message = "用户ID不能为空")
        @Min(value = 1, message = "用户ID必须大于0")
        private Long userId;

        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名必须是3-20位字符")
        private String username;

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        @NotNull(message = "存储限制不能为空")
        @Min(value = 0, message = "存储限制不能为负数")
        private Long storageLimit;

        @NotNull(message = "已用存储不能为空")
        @Min(value = 0, message = "已用存储不能为负数")
        private Long storageUsed;

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
    }
}
