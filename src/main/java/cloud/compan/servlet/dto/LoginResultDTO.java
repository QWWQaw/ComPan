package cloud.compan.servlet.dto;
import lombok.Data;

/**
 * 登录结果数据传输对象
 * 封装登录成功后返回给客户端的数据
 */
@Data
public class LoginResultDTO {
    private UserDTO user;
    private String token;
    private long expiresIn;
    private String tokenType = "Bearer";
    
    public LoginResultDTO() {}
    
    public LoginResultDTO(UserDTO user, String token, long expiresIn) {
        this.user = user;
        this.token = token;
        this.expiresIn = expiresIn;
    }
    
    public UserDTO getUser() {
        return user;
    }
    
    public void setUser(UserDTO user) {
        this.user = user;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
} 