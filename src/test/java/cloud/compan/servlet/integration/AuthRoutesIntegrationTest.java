package cloud.compan.servlet.integration;

import cloud.compan.servlet.web.MockHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.HashMap;

/**
 * 认证路由集成测试
 * 测试从HTTP请求到认证控制器的完整流程
 */
@DisplayName("认证路由集成测试")
public class AuthRoutesIntegrationTest extends RouteIntegrationTestBase {
    
    @Test
    @DisplayName("测试用户注册路由")
    void testUserRegistrationRoute() {
        // 准备注册数据
        Map<String, Object> registrationData = new HashMap<>();
        registrationData.put("username", "testuser");
        registrationData.put("email", "test@example.com");
        registrationData.put("password", "password123");
        
        String jsonBody = createJsonBody(registrationData);
        
        // 执行POST请求
        MockHttpResponse response = executeRequest("POST", "/api/auth/register", 
            new HashMap<>(), new HashMap<>(), jsonBody);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(201); // 创建成功
        response.expectApiSuccess();
        
        System.out.println("用户注册路由测试通过");
    }
    
    @Test
    @DisplayName("测试用户登录路由")
    void testUserLoginRoute() {
        // 准备登录数据
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("username", "testuser");
        loginData.put("password", "password123");
        
        String jsonBody = createJsonBody(loginData);
        
        // 执行POST请求
        MockHttpResponse response = executeRequest("POST", "/api/auth/login", 
            new HashMap<>(), new HashMap<>(), jsonBody);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("用户登录路由测试通过");
    }
    
    @Test
    @DisplayName("测试获取用户信息路由")
    void testGetUserProfileRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行GET请求
        MockHttpResponse response = executeRequest("GET", "/api/me/profile", authHeaders);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("获取用户信息路由测试通过");
    }
    
    @Test
    @DisplayName("测试更新用户信息路由")
    void testUpdateUserProfileRoute() {
        // 准备更新数据
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("displayName", "Updated Name");
        updateData.put("email", "updated@example.com");
        
        String jsonBody = createJsonBody(updateData);
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行PUT请求
        MockHttpResponse response = executeRequest("PUT", "/api/me/update-profile", 
            authHeaders, new HashMap<>(), jsonBody);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("更新用户信息路由测试通过");
    }
    
    @Test
    @DisplayName("测试修改密码路由")
    void testChangePasswordRoute() {
        // 准备密码修改数据
        Map<String, Object> passwordData = new HashMap<>();
        passwordData.put("currentPassword", "oldpassword");
        passwordData.put("newPassword", "newpassword123");
        
        String jsonBody = createJsonBody(passwordData);
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行PUT请求
        MockHttpResponse response = executeRequest("PUT", "/api/me/password", 
            authHeaders, new HashMap<>(), jsonBody);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("修改密码路由测试通过");
    }
    
    @Test
    @DisplayName("测试获取存储统计路由")
    void testGetStorageStatsRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行GET请求
        MockHttpResponse response = executeRequest("GET", "/api/me/storage-stats", authHeaders);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("获取存储统计路由测试通过");
    }
    
    @Test
    @DisplayName("测试用户登出路由")
    void testUserLogoutRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行POST请求
        MockHttpResponse response = executeRequest("POST", "/api/auth/logout", authHeaders);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("用户登出路由测试通过");
    }
} 