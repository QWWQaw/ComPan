package cloud.compan.servlet.integration;

import cloud.compan.servlet.web.MockHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.HashMap;

/**
 * 所有路由的综合集成测试
 * 测试从HTTP请求到各个控制器的完整流程
 */
@DisplayName("所有路由综合集成测试")
public class AllRoutesIntegrationTest extends RouteIntegrationTestBase {
    
    @Test
    @DisplayName("测试所有认证相关路由")
    void testAllAuthRoutes() {
        System.out.println("开始测试认证相关路由...");
        
        // 测试用户注册
        Map<String, Object> registrationData = new HashMap<>();
        registrationData.put("username", "integration-test-user");
        registrationData.put("email", "integration@example.com");
        registrationData.put("password", "password123");
        
        MockHttpResponse registerResponse = executeRequest("POST", "/api/auth/register", 
            new HashMap<>(), new HashMap<>(), createJsonBody(registrationData));
        assertResponseIsValidJson(registerResponse);
        registerResponse.expectStatus(201);
        
        // 测试用户登录
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("username", "integration-test-user");
        loginData.put("password", "password123");
        
        MockHttpResponse loginResponse = executeRequest("POST", "/api/auth/login", 
            new HashMap<>(), new HashMap<>(), createJsonBody(loginData));
        assertResponseIsValidJson(loginResponse);
        loginResponse.expectStatus(200);
        
        System.out.println("认证相关路由测试完成");
    }
    
    @Test
    @DisplayName("测试所有文件管理路由")
    void testAllFileRoutes() {
        System.out.println("开始测试文件管理路由...");
        
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 测试文件上传
        Map<String, Object> uploadData = new HashMap<>();
        uploadData.put("fileName", "integration-test.txt");
        uploadData.put("fileSize", 1024);
        uploadData.put("parentFolderId", 1);
        
        MockHttpResponse uploadResponse = executeRequest("POST", "/api/files/upload", 
            authHeaders, new HashMap<>(), createJsonBody(uploadData));
        assertResponseIsValidJson(uploadResponse);
        uploadResponse.expectStatus(201);
        
        // 测试获取文件列表
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("size", "10");
        
        MockHttpResponse listResponse = executeRequest("GET", "/api/files", authHeaders, params, "");
        assertResponseIsValidJson(listResponse);
        listResponse.expectStatus(200);
        
        // 测试文件搜索
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("query", "integration");
        searchParams.put("page", "1");
        searchParams.put("size", "10");
        
        MockHttpResponse searchResponse = executeRequest("GET", "/api/files/search", authHeaders, searchParams, "");
        assertResponseIsValidJson(searchResponse);
        searchResponse.expectStatus(200);
        
        System.out.println("文件管理路由测试完成");
    }
    
    @Test
    @DisplayName("测试所有文件夹管理路由")
    void testAllFolderRoutes() {
        System.out.println("开始测试文件夹管理路由...");
        
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 测试创建文件夹
        Map<String, Object> folderData = new HashMap<>();
        folderData.put("name", "integration-test-folder");
        folderData.put("parentFolderId", 1);
        folderData.put("description", "Integration test folder");
        
        MockHttpResponse createResponse = executeRequest("POST", "/api/folders", 
            authHeaders, new HashMap<>(), createJsonBody(folderData));
        assertResponseIsValidJson(createResponse);
        createResponse.expectStatus(201);
        
        // 测试获取文件夹列表
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("size", "10");
        
        MockHttpResponse listResponse = executeRequest("GET", "/api/folders", authHeaders, params, "");
        assertResponseIsValidJson(listResponse);
        listResponse.expectStatus(200);
        
        // 测试文件夹搜索
        Map<String, String> searchParams = new HashMap<>();
        searchParams.put("query", "integration");
        searchParams.put("page", "1");
        searchParams.put("size", "10");
        
        MockHttpResponse searchResponse = executeRequest("GET", "/api/folders/search", authHeaders, searchParams, "");
        assertResponseIsValidJson(searchResponse);
        searchResponse.expectStatus(200);
        
        System.out.println("文件夹管理路由测试完成");
    }
    
    @Test
    @DisplayName("测试所有分享管理路由")
    void testAllShareRoutes() {
        System.out.println("开始测试分享管理路由...");
        
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 测试创建分享
        Map<String, Object> shareData = new HashMap<>();
        shareData.put("resourceId", 1);
        shareData.put("resourceType", "FILE");
        shareData.put("permission", "READ");
        shareData.put("expiresAt", "2024-12-31T23:59:59Z");
        
        MockHttpResponse createResponse = executeRequest("POST", "/api/shares", 
            authHeaders, new HashMap<>(), createJsonBody(shareData));
        assertResponseIsValidJson(createResponse);
        createResponse.expectStatus(201);
        
        // 测试获取分享列表
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("size", "10");
        
        MockHttpResponse listResponse = executeRequest("GET", "/api/shares", authHeaders, params, "");
        assertResponseIsValidJson(listResponse);
        listResponse.expectStatus(200);
        
        // 测试获取分享统计
        MockHttpResponse statsResponse = executeRequest("GET", "/api/shares/stats", authHeaders);
        assertResponseIsValidJson(statsResponse);
        statsResponse.expectStatus(200);
        
        System.out.println("分享管理路由测试完成");
    }
    
    @Test
    @DisplayName("测试所有存储管理路由")
    void testAllStorageRoutes() {
        System.out.println("开始测试存储管理路由...");
        
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 测试获取存储统计
        MockHttpResponse statsResponse = executeRequest("GET", "/api/storage/stats", authHeaders);
        assertResponseIsValidJson(statsResponse);
        statsResponse.expectStatus(200);
        
        // 测试获取存储容量
        MockHttpResponse capacityResponse = executeRequest("GET", "/api/storage/capacity", authHeaders);
        assertResponseIsValidJson(capacityResponse);
        capacityResponse.expectStatus(200);
        
        System.out.println("存储管理路由测试完成");
    }
    
    @Test
    @DisplayName("测试所有通知管理路由")
    void testAllNotificationRoutes() {
        System.out.println("开始测试通知管理路由...");
        
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 测试获取通知列表
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("size", "10");
        
        MockHttpResponse listResponse = executeRequest("GET", "/api/notifications", authHeaders, params, "");
        assertResponseIsValidJson(listResponse);
        listResponse.expectStatus(200);
        
        // 测试标记通知为已读
        MockHttpResponse markReadResponse = executeRequest("PUT", "/api/notifications/1/read", authHeaders);
        assertResponseIsValidJson(markReadResponse);
        markReadResponse.expectStatus(200);
        
        System.out.println("通知管理路由测试完成");
    }
    
    @Test
    @DisplayName("测试所有回收站路由")
    void testAllRecycleBinRoutes() {
        System.out.println("开始测试回收站路由...");
        
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 测试获取回收站内容
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("size", "10");
        
        MockHttpResponse listResponse = executeRequest("GET", "/api/recycle-bin", authHeaders, params, "");
        assertResponseIsValidJson(listResponse);
        listResponse.expectStatus(200);
        
        // 测试恢复文件
        MockHttpResponse restoreResponse = executeRequest("PUT", "/api/recycle-bin/1/restore", authHeaders);
        assertResponseIsValidJson(restoreResponse);
        restoreResponse.expectStatus(200);
        
        System.out.println("回收站路由测试完成");
    }
    
    @Test
    @DisplayName("测试所有权限管理路由")
    void testAllAclRoutes() {
        System.out.println("开始测试权限管理路由...");
        
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 测试检查权限
        Map<String, String> params = new HashMap<>();
        params.put("resourceId", "1");
        params.put("resourceType", "FILE");
        params.put("permission", "READ");
        
        MockHttpResponse checkResponse = executeRequest("GET", "/api/acl/check", authHeaders, params, "");
        assertResponseIsValidJson(checkResponse);
        checkResponse.expectStatus(200);
        
        // 测试获取资源权限
        MockHttpResponse permissionsResponse = executeRequest("GET", "/api/acl/1/permissions", authHeaders);
        assertResponseIsValidJson(permissionsResponse);
        permissionsResponse.expectStatus(200);
        
        System.out.println("权限管理路由测试完成");
    }
    
    @Test
    @DisplayName("测试路由注册完整性")
    void testRouteRegistrationCompleteness() {
        System.out.println("验证所有路由都已正确注册...");
        
        // 验证关键路由存在
        assertRouteExists("POST", "/api/auth/register");
        assertRouteExists("POST", "/api/auth/login");
        assertRouteExists("GET", "/api/me/profile");
        assertRouteExists("POST", "/api/files/upload");
        assertRouteExists("GET", "/api/files");
        assertRouteExists("POST", "/api/folders");
        assertRouteExists("GET", "/api/folders");
        assertRouteExists("POST", "/api/shares");
        assertRouteExists("GET", "/api/shares");
        
        System.out.println("路由注册完整性验证通过");
    }
} 