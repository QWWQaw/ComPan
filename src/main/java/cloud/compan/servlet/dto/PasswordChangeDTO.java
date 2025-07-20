package cloud.compan.servlet.dto;

/**
 * 密码修改数据传输对象
 * 用于接收用户密码修改请求的数据
 */
public class PasswordChangeDTO {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
    
    public PasswordChangeDTO() {}
    
    public PasswordChangeDTO(String oldPassword, String newPassword, String confirmPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }
    
    public String getOldPassword() {
        return oldPassword;
    }
    
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
    
    public String getNewPassword() {
        return newPassword;
    }
    
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    
    // 验证方法
    public boolean isPasswordMatched() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
    
    public boolean isValid() {
        return oldPassword != null && !oldPassword.isEmpty() &&
               newPassword != null && newPassword.length() >= 6 &&
               isPasswordMatched();
    }
} 