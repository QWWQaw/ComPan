package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.WebTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Map;
import java.util.HashMap;

/**
 * AuthController 单元测试
 */
@DisplayName("认证控制器测试")
class AuthControllerTest extends WebTestBase {
    
    @Test
    @DisplayName("用户注册 - 成功")
    void testRegister_Success() throws Exception {
        Map<String, Object> registerData = new HashMap<>();
        registerData.put("username", "testuser");
        registerData.put("email", "test@example.com");
        registerData.put("password", "password123");
        
        post("/api/auth/register")
            .contentType("application/json")
            .jsonBody(registerData)
            .execute();
    }
    
    @Test
    @DisplayName("用户注册 - 参数验证失败")
    void testRegister_ValidationFailure() throws Exception {
        Map<String, Object> registerData = new HashMap<>();
        registerData.put("username", ""); // 空用户名
        registerData.put("email", "invalid-email");
        registerData.put("password", "123"); // 密码太短
        
        post("/api/auth/register")
            .contentType("application/json")
            .jsonBody(registerData)
            .execute();
    }
    
    @Test
    @DisplayName("用户登录 - 成功")
    void testLogin_Success() throws Exception {
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("username", "testuser");
        loginData.put("password", "password123");
        
        post("/api/auth/login")
            .contentType("application/json")
            .jsonBody(loginData)
            .execute();
    }
    
    @Test
    @DisplayName("用户登录 - 凭据错误")
    void testLogin_InvalidCredentials() throws Exception {
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("username", "testuser");
        loginData.put("password", "wrongpassword");
        
        post("/api/auth/login")
            .contentType("application/json")
            .jsonBody(loginData)
            .execute();
    }
    
    @Test
    @DisplayName("用户登出 - 成功")
    void testLogout_Success() throws Exception {
        post("/api/auth/logout")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("用户登出 - 未认证")
    void testLogout_Unauthorized() throws Exception {
        post("/api/auth/logout")
            .execute();
    }
    
    @Test
    @DisplayName("刷新Token - 成功")
    void testRefreshToken_Success() throws Exception {
        post("/api/auth/refresh")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取用户资料 - 成功")
    void testGetProfile_Success() throws Exception {
        get("/api/me/profile")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取用户资料 - 未认证")
    void testGetProfile_Unauthorized() throws Exception {
        get("/api/me/profile")
            .execute();
    }
    
    @Test
    @DisplayName("更新用户资料 - 成功")
    void testUpdateProfile_Success() throws Exception {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("displayName", "New Display Name");
        updateData.put("email", "newemail@example.com");
        
        put("/api/me/update-profile")
            .contentType("application/json")
            .jsonBody(updateData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("修改密码 - 成功")
    void testChangePassword_Success() throws Exception {
        Map<String, Object> passwordData = new HashMap<>();
        passwordData.put("old_password", "oldpassword");
        passwordData.put("new_password", "newpassword123");
        
        put("/api/me/password")
            .contentType("application/json")
            .jsonBody(passwordData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取存储统计 - 成功")
    void testGetStorageStats_Success() throws Exception {
        get("/api/me/storage-stats")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取活动日志 - 成功")
    void testGetActivityLog_Success() throws Exception {
        get("/api/me/activity-log")
            .param("page", "1")
            .param("size", "20")
            .param("operation", "file_upload")
            .bearerToken("valid-jwt-token")
            .execute();
    }
} 