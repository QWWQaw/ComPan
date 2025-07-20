package cloud.compan.servlet.dto;

/**
 * 用户登录数据传输对象
 * 用于接收用户登录请求的数据
 */
public class UserLoginDTO {
    private String username;
    private String password;
    private boolean rememberMe;
    
    public UserLoginDTO() {}
    
    public UserLoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isRememberMe() {
        return rememberMe;
    }
    
    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
    
    // 验证方法
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               password != null && !password.isEmpty();
    }
} 