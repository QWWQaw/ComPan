package cloud.compan.servlet.dto;

/**
 * 用户注册数据传输对象
 * 用于接收用户注册请求的数据
 */
public class UserRegistrationDTO {
    private String username;
    private String email;
    private String password;
    private String confirmPassword;
    
    public UserRegistrationDTO() {}
    
    public UserRegistrationDTO(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
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
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    
    // 验证方法
    public boolean isPasswordMatched() {
        return password != null && password.equals(confirmPassword);
    }
    
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               password != null && password.length() >= 6;
    }
} 