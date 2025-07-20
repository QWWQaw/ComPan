package cloud.compan.servlet.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;

/**
 * 用户数据传输对象
 * 用于控制层和服务层之间的用户数据传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    @Positive(message = "用户ID必须为正数")
    private Long userId;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Positive(message = "存储限制必须为正数")
    private Long storageLimit;

    @PositiveOrZero(message = "已使用存储不能为负数")
    private Long storageUsed;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 构造函数
    public UserDTO(Long userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
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
