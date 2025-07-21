package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.WebTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Map;
import java.util.HashMap;

/**
 * RecycleBinController 单元测试
 */
@DisplayName("回收站控制器测试")
class RecycleBinControllerTest extends WebTestBase {
    
    @Test
    @DisplayName("获取回收站列表 - 成功")
    void testGetRecycleBinList_Success() throws Exception {
        get("/api/recycle-bin")
            .param("type", "file")
            .param("page", "1")
            .param("size", "20")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取回收站列表 - 未认证")
    void testGetRecycleBinList_Unauthorized() throws Exception {
        get("/api/recycle-bin")
            .param("page", "1")
            .param("size", "20")
            .execute();
    }
    
    @Test
    @DisplayName("恢复文件 - 成功")
    void testRestoreFile_Success() throws Exception {
        Map<String, Object> restoreData = new HashMap<>();
        restoreData.put("target_folder_id", 1L);
        restoreData.put("new_name", "restored_file.pdf");
        
        post("/api/recycle-bin/123/restore")
            .contentType("application/json")
            .jsonBody(restoreData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("恢复文件夹 - 成功")
    void testRestoreFolder_Success() throws Exception {
        Map<String, Object> restoreData = new HashMap<>();
        restoreData.put("target_folder_id", 1L);
        restoreData.put("new_name", "restored_folder");
        
        post("/api/recycle-bin/456/restore")
            .contentType("application/json")
            .jsonBody(restoreData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("恢复项目 - 参数验证失败")
    void testRestoreItem_ValidationFailure() throws Exception {
        Map<String, Object> restoreData = new HashMap<>();
        restoreData.put("target_folder_id", -1L); // 无效文件夹ID
        restoreData.put("new_name", ""); // 空名称
        
        post("/api/recycle-bin/123/restore")
            .contentType("application/json")
            .jsonBody(restoreData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("永久删除项目 - 成功")
    void testPermanentlyDeleteItem_Success() throws Exception {
        delete("/api/recycle-bin/123")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("永久删除项目 - 不存在")
    void testPermanentlyDeleteItem_NotFound() throws Exception {
        delete("/api/recycle-bin/999999")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("批量恢复项目 - 成功")
    void testBatchRestoreItems_Success() throws Exception {
        Map<String, Object> batchData = new HashMap<>();
        batchData.put("item_ids", new Long[]{1L, 2L, 3L});
        batchData.put("target_folder_id", 1L);
        
        post("/api/recycle-bin/batch-restore")
            .contentType("application/json")
            .jsonBody(batchData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("批量永久删除项目 - 成功")
    void testBatchPermanentlyDeleteItems_Success() throws Exception {
        Map<String, Object> batchData = new HashMap<>();
        batchData.put("item_ids", new Long[]{1L, 2L, 3L});
        
        delete("/api/recycle-bin/batch")
            .contentType("application/json")
            .jsonBody(batchData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("清空回收站 - 成功")
    void testEmptyRecycleBin_Success() throws Exception {
        delete("/api/recycle-bin/empty")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取回收站统计 - 成功")
    void testGetRecycleBinStatistics_Success() throws Exception {
        get("/api/recycle-bin/statistics")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取项目详情 - 成功")
    void testGetItemDetails_Success() throws Exception {
        get("/api/recycle-bin/123/details")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("搜索回收站项目 - 成功")
    void testSearchRecycleBinItems_Success() throws Exception {
        get("/api/recycle-bin/search")
            .param("keyword", "important")
            .param("type", "file")
            .param("page", "1")
            .param("size", "20")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取自动清理策略 - 成功")
    void testGetAutoCleanPolicy_Success() throws Exception {
        get("/api/recycle-bin/auto-clean-policy")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("更新自动清理策略 - 成功")
    void testUpdateAutoCleanPolicy_Success() throws Exception {
        Map<String, Object> policyData = new HashMap<>();
        policyData.put("enabled", true);
        policyData.put("retention_days", 30);
        policyData.put("cleanup_schedule", "weekly");
        
        put("/api/recycle-bin/auto-clean-policy")
            .contentType("application/json")
            .jsonBody(policyData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("手动执行清理 - 成功")
    void testManualCleanup_Success() throws Exception {
        Map<String, Object> cleanupData = new HashMap<>();
        cleanupData.put("older_than_days", 30);
        cleanupData.put("item_types", new String[]{"file", "folder"});
        
        post("/api/recycle-bin/manual-cleanup")
            .contentType("application/json")
            .jsonBody(cleanupData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取清理历史 - 成功")
    void testGetCleanupHistory_Success() throws Exception {
        get("/api/recycle-bin/cleanup-history")
            .param("page", "1")
            .param("size", "20")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("导出回收站报告 - 成功")
    void testExportRecycleBinReport_Success() throws Exception {
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("format", "csv");
        exportData.put("include_details", true);
        exportData.put("date_from", "2024-01-01");
        exportData.put("date_to", "2024-12-31");
        
        post("/api/recycle-bin/export-report")
            .contentType("application/json")
            .jsonBody(exportData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取回收站容量 - 成功")
    void testGetRecycleBinCapacity_Success() throws Exception {
        get("/api/recycle-bin/capacity")
            .bearerToken("valid-jwt-token")
            .execute();
    }
} 