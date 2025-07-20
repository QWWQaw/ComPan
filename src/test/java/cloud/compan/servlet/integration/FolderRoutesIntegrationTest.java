package cloud.compan.servlet.integration;

import cloud.compan.servlet.web.MockHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.HashMap;

/**
 * 文件夹管理路由集成测试
 * 测试从HTTP请求到文件夹控制器的完整流程
 */
@DisplayName("文件夹管理路由集成测试")
public class FolderRoutesIntegrationTest extends RouteIntegrationTestBase {
    
    @Test
    @DisplayName("测试创建文件夹路由")
    void testCreateFolderRoute() {
        // 准备创建文件夹数据
        Map<String, Object> folderData = new HashMap<>();
        folderData.put("name", "test-folder");
        folderData.put("parentFolderId", 1);
        folderData.put("description", "Test folder description");
        
        String jsonBody = createJsonBody(folderData);
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行POST请求
        MockHttpResponse response = executeRequest("POST", "/api/folders", 
            authHeaders, new HashMap<>(), jsonBody);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(201); // 创建成功
        response.expectApiSuccess();
        
        System.out.println("创建文件夹路由测试通过");
    }
    
    @Test
    @DisplayName("测试获取文件夹列表路由")
    void testGetFolderListRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 设置查询参数
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("size", "10");
        params.put("parentFolderId", "1");
        
        // 执行GET请求
        MockHttpResponse response = executeRequest("GET", "/api/folders", authHeaders, params, "");
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("获取文件夹列表路由测试通过");
    }
    
    @Test
    @DisplayName("测试获取文件夹详情路由")
    void testGetFolderDetailsRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行GET请求
        MockHttpResponse response = executeRequest("GET", "/api/folders/1", authHeaders);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("获取文件夹详情路由测试通过");
    }
    
    @Test
    @DisplayName("测试文件夹重命名路由")
    void testRenameFolderRoute() {
        // 准备重命名数据
        Map<String, Object> renameData = new HashMap<>();
        renameData.put("newName", "renamed-folder");
        
        String jsonBody = createJsonBody(renameData);
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行PUT请求
        MockHttpResponse response = executeRequest("PUT", "/api/folders/1/rename", 
            authHeaders, new HashMap<>(), jsonBody);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("文件夹重命名路由测试通过");
    }
    
    @Test
    @DisplayName("测试文件夹删除路由")
    void testDeleteFolderRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行DELETE请求
        MockHttpResponse response = executeRequest("DELETE", "/api/folders/1", authHeaders);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("文件夹删除路由测试通过");
    }
    
    @Test
    @DisplayName("测试获取子文件夹路由")
    void testGetSubFoldersRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 设置查询参数
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("size", "10");
        
        // 执行GET请求
        MockHttpResponse response = executeRequest("GET", "/api/folders/1/subfolders", authHeaders, params, "");
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("获取子文件夹路由测试通过");
    }
    
    @Test
    @DisplayName("测试移动文件夹路由")
    void testMoveFolderRoute() {
        // 准备移动数据
        Map<String, Object> moveData = new HashMap<>();
        moveData.put("targetFolderId", 2);
        
        String jsonBody = createJsonBody(moveData);
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 执行PUT请求
        MockHttpResponse response = executeRequest("PUT", "/api/folders/1/move", 
            authHeaders, new HashMap<>(), jsonBody);
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("移动文件夹路由测试通过");
    }
    
    @Test
    @DisplayName("测试文件夹搜索路由")
    void testSearchFoldersRoute() {
        // 创建认证头
        Map<String, String> authHeaders = createAuthHeaders("mock-jwt-token");
        
        // 设置搜索参数
        Map<String, String> params = new HashMap<>();
        params.put("query", "test");
        params.put("page", "1");
        params.put("size", "10");
        
        // 执行GET请求
        MockHttpResponse response = executeRequest("GET", "/api/folders/search", authHeaders, params, "");
        
        // 验证响应
        assertResponseIsValidJson(response);
        response.expectStatus(200);
        response.expectApiSuccess();
        
        System.out.println("文件夹搜索路由测试通过");
    }
} 