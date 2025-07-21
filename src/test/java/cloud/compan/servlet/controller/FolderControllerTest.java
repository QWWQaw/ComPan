package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.WebTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Map;
import java.util.HashMap;

/**
 * FolderController 单元测试
 */
@DisplayName("文件夹控制器测试")
class FolderControllerTest extends WebTestBase {
    
    @Test
    @DisplayName("创建文件夹 - 成功")
    void testCreateFolder_Success() throws Exception {
        Map<String, Object> folderData = new HashMap<>();
        folderData.put("name", "New Folder");
        folderData.put("parent_id", 1L);
        folderData.put("description", "Test folder description");
        
        post("/api/folders")
            .contentType("application/json")
            .jsonBody(folderData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("创建文件夹 - 参数验证失败")
    void testCreateFolder_ValidationFailure() throws Exception {
        Map<String, Object> folderData = new HashMap<>();
        folderData.put("name", ""); // 空名称
        folderData.put("parent_id", -1L); // 无效父文件夹ID
        
        post("/api/folders")
            .contentType("application/json")
            .jsonBody(folderData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("创建文件夹 - 未认证")
    void testCreateFolder_Unauthorized() throws Exception {
        Map<String, Object> folderData = new HashMap<>();
        folderData.put("name", "New Folder");
        
        post("/api/folders")
            .contentType("application/json")
            .jsonBody(folderData)
            .execute();
    }
    
    @Test
    @DisplayName("获取文件夹列表 - 成功")
    void testGetFolderList_Success() throws Exception {
        get("/api/folders")
            .param("parent_id", "1")
            .param("page", "1")
            .param("size", "20")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取文件夹详情 - 成功")
    void testGetFolderDetails_Success() throws Exception {
        get("/api/folders/123")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取文件夹详情 - 不存在")
    void testGetFolderDetails_NotFound() throws Exception {
        get("/api/folders/999999")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("更新文件夹 - 成功")
    void testUpdateFolder_Success() throws Exception {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", "Updated Folder Name");
        updateData.put("description", "Updated description");
        
        put("/api/folders/123")
            .contentType("application/json")
            .jsonBody(updateData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("删除文件夹 - 成功")
    void testDeleteFolder_Success() throws Exception {
        delete("/api/folders/123")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("移动文件夹 - 成功")
    void testMoveFolder_Success() throws Exception {
        Map<String, Object> moveData = new HashMap<>();
        moveData.put("new_parent_id", 2L);
        
        patch("/api/folders/123/move")
            .contentType("application/json")
            .jsonBody(moveData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("复制文件夹 - 成功")
    void testCopyFolder_Success() throws Exception {
        Map<String, Object> copyData = new HashMap<>();
        copyData.put("target_parent_id", 2L);
        copyData.put("new_name", "Copied Folder");
        
        post("/api/folders/123/copy")
            .contentType("application/json")
            .jsonBody(copyData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取文件夹树结构 - 成功")
    void testGetFolderTree_Success() throws Exception {
        get("/api/folders/tree")
            .param("root_id", "1")
            .param("depth", "3")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("搜索文件夹 - 成功")
    void testSearchFolders_Success() throws Exception {
        get("/api/folders/search")
            .param("keyword", "important")
            .param("page", "1")
            .param("size", "20")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取文件夹统计信息 - 成功")
    void testGetFolderStatistics_Success() throws Exception {
        get("/api/folders/123/statistics")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取用户根目录 - 成功")
    void testGetRootFolder_Success() throws Exception {
        get("/api/folders/root")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("批量操作文件夹 - 成功")
    void testBatchOperation_Success() throws Exception {
        Map<String, Object> batchData = new HashMap<>();
        batchData.put("operation", "delete");
        batchData.put("folder_ids", new Long[]{1L, 2L, 3L});
        
        post("/api/folders/batch")
            .contentType("application/json")
            .jsonBody(batchData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
} 