package cloud.compan.servlet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;

/**
 * 用户登录数据传输对象
 * 用于接收用户登录请求的数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;

    private boolean rememberMe;

    // 验证方法
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               password != null && !password.isEmpty();
    }
}
