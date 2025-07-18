package cloud.compan.servlet.dto;

/**
 * 用户注册请求DTO
 */
public class UserRegisterDTO {

    private String username;
    private String email;
    private String password;

    public UserRegisterDTO() {}

    public UserRegisterDTO(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 验证注册数据
     */
    public boolean isValid() {
        return isValidUsername() && isValidEmail() && isValidPassword();
    }

    /**
     * 验证用户名
     */
    public boolean isValidUsername() {
        return username != null &&
               username.trim().length() >= 3 &&
               username.trim().length() <= 20 &&
               username.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * 验证邮箱
     */
    public boolean isValidEmail() {
        return email != null &&
               email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * 验证密码
     */
    public boolean isValidPassword() {
        return password != null &&
               password.length() >= 6 &&
               password.length() <= 50;
    }

    /**
     * 获取验证错误信息
     */
    public String getValidationError() {
        if (username == null || username.trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (!isValidUsername()) {
            return "用户名必须是3-20位字母、数字或下划线";
        }
        if (email == null || email.trim().isEmpty()) {
            return "邮箱不能为空";
        }
        if (!isValidEmail()) {
            return "邮箱格式不正确";
        }
        if (password == null || password.trim().isEmpty()) {
            return "密码不能为空";
        }
        if (!isValidPassword()) {
            return "密码长度必须在6-50位之间";
        }
        return null;
    }

    @Override
    public String toString() {
        return "UserRegisterDTO{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='[HIDDEN]'" +
                '}';
    }
}
