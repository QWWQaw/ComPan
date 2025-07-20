package cloud.compan.servlet.dto;

/**
 * 用户信息更新数据传输对象
 * 用于接收用户信息更新请求的数据
 */
public class UserUpdateDTO {
    private String username;
    private String email;
    
    public UserUpdateDTO() {}
    
    public UserUpdateDTO(String username, String email) {
        this.username = username;
        this.email = email;
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
    
    // 验证方法
    public boolean hasValidUsername() {
        return username != null && !username.trim().isEmpty();
    }
    
    public boolean hasValidEmail() {
        return email != null && !email.trim().isEmpty();
    }
} 