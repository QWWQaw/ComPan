package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.WebTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Map;
import java.util.HashMap;

/**
 * ShareController 单元测试
 */
@DisplayName("分享控制器测试")
class ShareControllerTest extends WebTestBase {
    
    @Test
    @DisplayName("创建文件分享 - 成功")
    void testCreateFileShare_Success() throws Exception {
        Map<String, Object> shareData = new HashMap<>();
        shareData.put("resource_type", "file");
        shareData.put("resource_id", 123L);
        shareData.put("share_type", "public");
        shareData.put("expires_at", "2024-12-31T23:59:59");
        shareData.put("password", "share123");
        
        post("/api/shares")
            .contentType("application/json")
            .jsonBody(shareData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("创建文件夹分享 - 成功")
    void testCreateFolderShare_Success() throws Exception {
        Map<String, Object> shareData = new HashMap<>();
        shareData.put("resource_type", "folder");
        shareData.put("resource_id", 456L);
        shareData.put("share_type", "private");
        shareData.put("target_user_id", 789L);
        
        post("/api/shares")
            .contentType("application/json")
            .jsonBody(shareData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("创建分享 - 参数验证失败")
    void testCreateShare_ValidationFailure() throws Exception {
        Map<String, Object> shareData = new HashMap<>();
        shareData.put("resource_type", ""); // 空资源类型
        shareData.put("resource_id", -1L); // 无效资源ID
        
        post("/api/shares")
            .contentType("application/json")
            .jsonBody(shareData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("创建分享 - 未认证")
    void testCreateShare_Unauthorized() throws Exception {
        Map<String, Object> shareData = new HashMap<>();
        shareData.put("resource_type", "file");
        shareData.put("resource_id", 123L);
        shareData.put("share_type", "public");
        
        post("/api/shares")
            .contentType("application/json")
            .jsonBody(shareData)
            .execute();
    }
    
    @Test
    @DisplayName("获取分享列表 - 成功")
    void testGetShareList_Success() throws Exception {
        get("/api/shares")
            .param("type", "created")
            .param("page", "1")
            .param("size", "20")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取分享详情 - 成功")
    void testGetShareDetails_Success() throws Exception {
        get("/api/shares/123")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("更新分享 - 成功")
    void testUpdateShare_Success() throws Exception {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("expires_at", "2024-12-31T23:59:59");
        updateData.put("password", "newpassword");
        
        put("/api/shares/123")
            .contentType("application/json")
            .jsonBody(updateData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("删除分享 - 成功")
    void testDeleteShare_Success() throws Exception {
        delete("/api/shares/123")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("访问公开分享 - 成功")
    void testAccessPublicShare_Success() throws Exception {
        get("/api/shares/public/abc123")
            .execute();
    }
    
    @Test
    @DisplayName("访问密码保护分享 - 成功")
    void testAccessPasswordProtectedShare_Success() throws Exception {
        Map<String, Object> accessData = new HashMap<>();
        accessData.put("password", "share123");
        
        post("/api/shares/public/abc123/access")
            .contentType("application/json")
            .jsonBody(accessData)
            .execute();
    }
    
    @Test
    @DisplayName("访问密码保护分享 - 密码错误")
    void testAccessPasswordProtectedShare_WrongPassword() throws Exception {
        Map<String, Object> accessData = new HashMap<>();
        accessData.put("password", "wrongpassword");
        
        post("/api/shares/public/abc123/access")
            .contentType("application/json")
            .jsonBody(accessData)
            .execute();
    }
    
    @Test
    @DisplayName("获取分享统计信息 - 成功")
    void testGetShareStatistics_Success() throws Exception {
        get("/api/shares/123/statistics")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("批量删除分享 - 成功")
    void testBatchDeleteShares_Success() throws Exception {
        Map<String, Object> batchData = new HashMap<>();
        batchData.put("share_ids", new Long[]{1L, 2L, 3L});
        
        delete("/api/shares/batch")
            .contentType("application/json")
            .jsonBody(batchData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取分享访问记录 - 成功")
    void testGetShareAccessLog_Success() throws Exception {
        get("/api/shares/123/access-log")
            .param("page", "1")
            .param("size", "20")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("设置分享权限 - 成功")
    void testSetSharePermission_Success() throws Exception {
        Map<String, Object> permissionData = new HashMap<>();
        permissionData.put("permission", "download");
        permissionData.put("enabled", true);
        
        patch("/api/shares/123/permissions")
            .contentType("application/json")
            .jsonBody(permissionData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取分享二维码 - 成功")
    void testGetShareQRCode_Success() throws Exception {
        get("/api/shares/123/qr-code")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("验证分享链接 - 有效")
    void testValidateShareLink_Valid() throws Exception {
        get("/api/shares/validate/abc123")
            .execute();
    }
    
    @Test
    @DisplayName("验证分享链接 - 无效")
    void testValidateShareLink_Invalid() throws Exception {
        get("/api/shares/validate/invalid123")
            .execute();
    }
} 