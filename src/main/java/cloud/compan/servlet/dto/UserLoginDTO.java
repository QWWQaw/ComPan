package cloud.compan.servlet.dto;

import javax.validation.constraints.NotBlank;

/**
 * 用户登录请求DTO
 * 用于Handler层接收登录请求参数
 */
public class UserLoginDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    // 构造函数
    public UserLoginDTO() {}

    public UserLoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // 验证方法
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               password != null && !password.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "UserLoginDTO{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
