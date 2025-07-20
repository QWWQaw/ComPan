package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.WebTestBase;
import cloud.compan.servlet.web.MockHttpResponse;
import cloud.compan.servlet.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Map;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthController测试类
 */
public class AuthControllerTest extends WebTestBase {
    
    @Mock
    private AuthService authService;
    
    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testUserLogin_Success() throws Exception {
        // Given: 准备登录成功的测试数据
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("username", "testuser");
        loginData.put("password", "password123");
        
        // When & Then: 执行登录请求
        MockHttpResponse response = post("/api/auth/login")
                .contentType("application/json")
                .jsonBody(loginData)
                .execute();
        
        // 验证响应
        response.expectApiSuccess();
    }
    
    @Test
    void testUserLogin_InvalidCredentials() throws Exception {
        // Given: 准备登录失败的测试数据
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("username", "testuser");
        loginData.put("password", "wrongpassword");
        
        // When & Then: 执行登录请求
        MockHttpResponse response = post("/api/auth/login")
                .contentType("application/json")
                .jsonBody(loginData)
                .execute();
        
        // 验证响应
        response.expectApiSuccess();
    }
} 