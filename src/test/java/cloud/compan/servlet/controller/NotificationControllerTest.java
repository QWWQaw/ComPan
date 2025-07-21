package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.WebTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Map;
import java.util.HashMap;

/**
 * NotificationController 单元测试
 */
@DisplayName("通知控制器测试")
class NotificationControllerTest extends WebTestBase {
    
    @Test
    @DisplayName("获取用户通知列表 - 成功")
    void testGetUserNotifications_Success() throws Exception {
        get("/api/notifications")
            .param("type", "system")
            .param("status", "unread")
            .param("page", "1")
            .param("size", "20")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取用户通知列表 - 未认证")
    void testGetUserNotifications_Unauthorized() throws Exception {
        get("/api/notifications")
            .param("page", "1")
            .param("size", "20")
            .execute();
    }
    
    @Test
    @DisplayName("获取未读通知数量 - 成功")
    void testGetUnreadNotificationCount_Success() throws Exception {
        get("/api/notifications/unread-count")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("标记通知为已读 - 成功")
    void testMarkNotificationAsRead_Success() throws Exception {
        put("/api/notifications/123/read")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("标记通知为已读 - 不存在")
    void testMarkNotificationAsRead_NotFound() throws Exception {
        put("/api/notifications/999999/read")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("标记所有通知为已读 - 成功")
    void testMarkAllAsRead_Success() throws Exception {
        patch("/api/notifications/read-all")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("删除通知 - 成功")
    void testDeleteNotification_Success() throws Exception {
        delete("/api/notifications/123")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("删除通知 - 无效ID")
    void testDeleteNotification_InvalidId() throws Exception {
        delete("/api/notifications/abc")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("批量标记通知为已读 - 成功")
    void testBatchMarkAsRead_Success() throws Exception {
        Map<String, Object> batchData = new HashMap<>();
        batchData.put("notification_ids", new Long[]{1L, 2L, 3L});
        
        patch("/api/notifications/batch-read")
            .contentType("application/json")
            .jsonBody(batchData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("批量删除通知 - 成功")
    void testBatchDeleteNotifications_Success() throws Exception {
        Map<String, Object> batchData = new HashMap<>();
        batchData.put("notification_ids", new Long[]{1L, 2L, 3L});
        
        delete("/api/notifications/batch")
            .contentType("application/json")
            .jsonBody(batchData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取通知设置 - 成功")
    void testGetNotificationSettings_Success() throws Exception {
        get("/api/notifications/settings")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("更新通知设置 - 成功")
    void testUpdateNotificationSettings_Success() throws Exception {
        Map<String, Object> settingsData = new HashMap<>();
        settingsData.put("email_notifications", true);
        settingsData.put("push_notifications", false);
        settingsData.put("system_notifications", true);
        
        put("/api/notifications/settings")
            .contentType("application/json")
            .jsonBody(settingsData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取通知模板 - 成功")
    void testGetNotificationTemplates_Success() throws Exception {
        get("/api/notifications/templates")
            .param("type", "system")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("发送测试通知 - 成功")
    void testSendTestNotification_Success() throws Exception {
        Map<String, Object> testData = new HashMap<>();
        testData.put("type", "email");
        testData.put("message", "This is a test notification");
        
        post("/api/notifications/test")
            .contentType("application/json")
            .jsonBody(testData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("获取通知统计 - 成功")
    void testGetNotificationStatistics_Success() throws Exception {
        get("/api/notifications/statistics")
            .param("period", "7d")
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("订阅通知频道 - 成功")
    void testSubscribeNotificationChannel_Success() throws Exception {
        Map<String, Object> subscribeData = new HashMap<>();
        subscribeData.put("channel", "file_updates");
        subscribeData.put("enabled", true);
        
        post("/api/notifications/subscribe")
            .contentType("application/json")
            .jsonBody(subscribeData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
    
    @Test
    @DisplayName("取消订阅通知频道 - 成功")
    void testUnsubscribeNotificationChannel_Success() throws Exception {
        Map<String, Object> unsubscribeData = new HashMap<>();
        unsubscribeData.put("channel", "file_updates");
        
        post("/api/notifications/unsubscribe")
            .contentType("application/json")
            .jsonBody(unsubscribeData)
            .bearerToken("valid-jwt-token")
            .execute();
    }
} 