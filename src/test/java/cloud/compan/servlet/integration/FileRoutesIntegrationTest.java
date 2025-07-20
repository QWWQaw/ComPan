package cloud.compan.servlet.integration;

import cloud.compan.servlet.web.MockHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.HashMap;

/**
 * 文件管理路由集成测试
 * 测试从HTTP请求到文件控制器的完整流程
 */
@DisplayName("文件管理路由集成测试")
public class FileRoutesIntegrationTest extends RouteIntegrationTestBase {
    
    @Test
    @DisplayName("测试文件上传路由")
    void testFileUploadRoute() {
        // 准备文件上传数据
        Map<String, Object> uploadData = new HashMap<>();
        uploadData.put("fileName", "test.txt");
        uploadData.put("fileSize", 1024);
        uploadData.put("parentFolderId", 1);
        
        String jsonBody = createJsonBody(uploadData);
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行POST请求
        MockHttpResponse response = executeRequest("POST", "/api/files/upload", 
            authHeaders, new HashMap<>(), jsonBody);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(201); // 创建成功
        response.expectApiSuccess();
        
        System.out.println("文件上传路由测试通过");
    }
    
    @Test
    @DisplayName("测试获取文件列表路由")
    void testGetFileListRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 设置查询参数
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("size", "10");
        params.put("folderId", "1");
        
        // 执行GET请求
        MockHttpResponse response = executeRequest("GET", "/api/files", authHeaders, params, "");
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("获取文件列表路由测试通过");
    }
    
    @Test
    @DisplayName("测试获取文件详情路由")
    void testGetFileDetailsRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行GET请求
        MockHttpResponse response = executeRequest("GET", "/api/files/1", authHeaders);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("获取文件详情路由测试通过");
    }
    
    @Test
    @DisplayName("测试文件重命名路由")
    void testRenameFileRoute() {
        // 准备重命名数据
        Map<String, Object> renameData = new HashMap<>();
        renameData.put("newName", "renamed-file.txt");
        
        String jsonBody = createJsonBody(renameData);
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行PUT请求
        MockHttpResponse response = executeRequest("PUT", "/api/files/1/rename", 
            authHeaders, new HashMap<>(), jsonBody);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("文件重命名路由测试通过");
    }
    
    @Test
    @DisplayName("测试文件删除路由")
    void testDeleteFileRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行DELETE请求
        MockHttpResponse response = executeRequest("DELETE", "/api/files/1", authHeaders);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("文件删除路由测试通过");
    }
    
    @Test
    @DisplayName("测试文件下载路由")
    void testDownloadFileRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行GET请求
        MockHttpResponse response = executeRequest("GET", "/api/files/1/download", authHeaders);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("文件下载路由测试通过");
    }
    
    @Test
    @DisplayName("测试文件搜索路由")
    void testSearchFilesRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 设置搜索参数
        Map<String, String> params = new HashMap<>();
        params.put("query", "test");
        params.put("page", "1");
        params.put("size", "10");
        
        // 执行GET请求
        MockHttpResponse response = executeRequest("GET", "/api/files/search", authHeaders, params, "");
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("文件搜索路由测试通过");
    }
} 