package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.WebTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Map;
import java.util.HashMap;

/**
 * StorageController 单元测试
 */
@DisplayName("存储控制器测试")
class StorageControllerTest extends WebTestBase {
    
    @Test
    @DisplayName("获取用户存储统计 - 成功")
    void testGetUserStorageStats_Success() throws Exception {
        get("/api/storage/user/stats")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取用户存储统计 - 未认证")
    void testGetUserStorageStats_Unauthorized() throws Exception {
        get("/api/storage/user/stats")
            .execute();
    }
    
    @Test
    @DisplayName("获取用户存储使用详情 - 成功")
    void testGetUserStorageDetails_Success() throws Exception {
        get("/api/storage/user/details")
            .param("period", "30d")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取存储类型统计 - 成功")
    void testGetStorageTypeStats_Success() throws Exception {
        get("/api/storage/user/type-stats")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取存储增长趋势 - 成功")
    void testGetStorageGrowthTrend_Success() throws Exception {
        get("/api/storage/user/growth-trend")
            .param("period", "7d")
            .param("interval", "day")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("清理存储空间 - 成功")
    void testCleanupStorage_Success() throws Exception {
        Map<String, Object> cleanupData = new HashMap<>();
        cleanupData.put("cleanup_type", "temp_files");
        cleanupData.put("older_than_days", 30);
        
        post("/api/storage/user/cleanup")
            .contentType("application/json")
            .jsonBody(cleanupData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("清理存储空间 - 参数验证失败")
    void testCleanupStorage_ValidationFailure() throws Exception {
        Map<String, Object> cleanupData = new HashMap<>();
        cleanupData.put("cleanup_type", ""); // 空清理类型
        cleanupData.put("older_than_days", -1); // 无效天数
        
        post("/api/storage/user/cleanup")
            .contentType("application/json")
            .jsonBody(cleanupData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取存储配额信息 - 成功")
    void testGetStorageQuota_Success() throws Exception {
        get("/api/storage/user/quota")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("更新存储配额 - 成功")
    void testUpdateStorageQuota_Success() throws Exception {
        Map<String, Object> quotaData = new HashMap<>();
        quotaData.put("storage_limit", 21474836480L); // 20GB
        
        put("/api/storage/user/quota")
            .contentType("application/json")
            .jsonBody(quotaData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取存储警告设置 - 成功")
    void testGetStorageWarnings_Success() throws Exception {
        get("/api/storage/user/warnings")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("更新存储警告设置 - 成功")
    void testUpdateStorageWarnings_Success() throws Exception {
        Map<String, Object> warningData = new HashMap<>();
        warningData.put("warning_threshold", 80);
        warningData.put("critical_threshold", 95);
        warningData.put("enable_notifications", true);
        
        put("/api/storage/user/warnings")
            .contentType("application/json")
            .jsonBody(warningData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取系统存储统计 - 成功")
    void testGetSystemStorageStatistics_Success() throws Exception {
        get("/api/storage/system/statistics")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取用户存储排行榜 - 成功")
    void testGetUserStorageRanking_Success() throws Exception {
        get("/api/storage/system/user-ranking")
            .param("limit", "10")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取存储使用报告 - 成功")
    void testGetStorageUsageReport_Success() throws Exception {
        get("/api/storage/user/report")
            .param("format", "pdf")
            .param("period", "month")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("导出存储数据 - 成功")
    void testExportStorageData_Success() throws Exception {
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("export_type", "usage_history");
        exportData.put("format", "csv");
        exportData.put("date_from", "2024-01-01");
        exportData.put("date_to", "2024-12-31");
        
        post("/api/storage/user/export")
            .contentType("application/json")
            .jsonBody(exportData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取存储优化建议 - 成功")
    void testGetStorageOptimizationSuggestions_Success() throws Exception {
        get("/api/storage/user/optimization-suggestions")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("应用存储优化 - 成功")
    void testApplyStorageOptimization_Success() throws Exception {
        Map<String, Object> optimizationData = new HashMap<>();
        optimizationData.put("optimization_type", "compress_images");
        optimizationData.put("quality", 85);
        
        post("/api/storage/user/optimize")
            .contentType("application/json")
            .jsonBody(optimizationData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取存储备份状态 - 成功")
    void testGetStorageBackupStatus_Success() throws Exception {
        get("/api/storage/user/backup-status")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("创建存储备份 - 成功")
    void testCreateStorageBackup_Success() throws Exception {
        Map<String, Object> backupData = new HashMap<>();
        backupData.put("backup_type", "full");
        backupData.put("include_files", true);
        backupData.put("include_folders", true);
        
        post("/api/storage/user/backup")
            .contentType("application/json")
            .jsonBody(backupData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
} 