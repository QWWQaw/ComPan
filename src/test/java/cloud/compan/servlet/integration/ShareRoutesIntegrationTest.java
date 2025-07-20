package cloud.compan.servlet.integration;

import cloud.compan.servlet.web.MockHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.HashMap;

/**
 * 分享路由集成测试
 * 测试从HTTP请求到分享控制器的完整流程
 */
@DisplayName("分享路由集成测试")
public class ShareRoutesIntegrationTest extends RouteIntegrationTestBase {
    
    @Test
    @DisplayName("测试创建分享路由")
    void testCreateShareRoute() {
        // 准备创建分享数据
        Map<String, Object> shareData = new HashMap<>();
        shareData.put("resourceId", 1);
        shareData.put("resourceType", "FILE");
        shareData.put("permission", "READ");
        shareData.put("expiresAt", "2024-12-31T23:59:59Z");
        shareData.put("password", "share123");
        
        String jsonBody = createJsonBody(shareData);
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行POST请求
        MockHttpResponse response = executeRequest("POST", "/api/shares", 
            authHeaders, new HashMap<>(), jsonBody);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(201); // 创建成功
        response.expectApiSuccess();
        
        System.out.println("创建分享路由测试通过");
    }
    
    @Test
    @DisplayName("测试获取分享列表路由")
    void testGetShareListRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 设置查询参数
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("size", "10");
        params.put("type", "CREATED");
        
        // 执行GET请求
        MockHttpResponse response = executeRequest("GET", "/api/shares", authHeaders, params, "");
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("获取分享列表路由测试通过");
    }
    
    @Test
    @DisplayName("测试获取分享详情路由")
    void testGetShareDetailsRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行GET请求
        MockHttpResponse response = executeRequest("GET", "/api/shares/1", authHeaders);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("获取分享详情路由测试通过");
    }
    
    @Test
    @DisplayName("测试更新分享权限路由")
    void testUpdateSharePermissionRoute() {
        // 准备更新数据
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("permission", "WRITE");
        updateData.put("expiresAt", "2024-12-31T23:59:59Z");
        
        String jsonBody = createJsonBody(updateData);
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行PUT请求
        MockHttpResponse response = executeRequest("PUT", "/api/shares/1", 
            authHeaders, new HashMap<>(), jsonBody);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("更新分享权限路由测试通过");
    }
    
    @Test
    @DisplayName("测试删除分享路由")
    void testDeleteShareRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行DELETE请求
        MockHttpResponse response = executeRequest("DELETE", "/api/shares/1", authHeaders);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("删除分享路由测试通过");
    }
    
    @Test
    @DisplayName("测试访问分享资源路由")
    void testAccessSharedResourceRoute() {
        // 准备访问数据
        Map<String, Object> accessData = new HashMap<>();
        accessData.put("password", "share123");
        
        String jsonBody = createJsonBody(accessData);
        
        // 执行POST请求（不需要认证头，因为是公开访问）
        MockHttpResponse response = executeRequest("POST", "/api/shares/1/access", 
            new HashMap<>(), new HashMap<>(), jsonBody);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("访问分享资源路由测试通过");
    }
    
    @Test
    @DisplayName("测试获取分享统计路由")
    void testGetShareStatsRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行GET请求
        MockHttpResponse response = executeRequest("GET", "/api/shares/stats", authHeaders);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("获取分享统计路由测试通过");
    }
    
    @Test
    @DisplayName("测试批量分享路由")
    void testBatchShareRoute() {
        // 准备批量分享数据
        Map<String, Object> batchData = new HashMap<>();
        batchData.put("resourceIds", new int[]{1, 2, 3});
        batchData.put("resourceType", "FILE");
        batchData.put("permission", "READ");
        batchData.put("expiresAt", "2024-12-31T23:59:59Z");
        
        String jsonBody = createJsonBody(batchData);
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行POST请求
        MockHttpResponse response = executeRequest("POST", "/api/shares/batch", 
            authHeaders, new HashMap<>(), jsonBody);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(201);
        response.expectApiSuccess();
        
        System.out.println("批量分享路由测试通过");
    }
} 