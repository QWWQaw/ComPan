package cloud.compan.servlet.dto;

/**
 * 用户注册请求DTO
 */
public class UserRegisterDTO {
    private String username;
    private String email;
    private String password;

    // 构造函数
    public UserRegisterDTO() {}

    public UserRegisterDTO(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
