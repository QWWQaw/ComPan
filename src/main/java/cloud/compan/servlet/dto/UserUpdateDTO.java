package cloud.compan.servlet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;

/**
 * 用户信息更新数据传输对象
 * 用于接收用户信息更新请求的数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDTO {

    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    // 验证方法
    public boolean hasValidUsername() {
        return username != null && !username.trim().isEmpty();
    }
    
    public boolean hasValidEmail() {
        return email != null && !email.trim().isEmpty();
    }
}
