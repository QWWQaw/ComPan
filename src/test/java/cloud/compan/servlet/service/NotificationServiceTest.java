package cloud.compan.servlet.service;

import cloud.compan.servlet.web.SimpleTestBase;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.model.Notification;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * NotificationService单元测试
 * 测试通知服务的功能
 */
public class NotificationServiceTest extends SimpleTestBase {
    
    @Test
    void testNotificationServiceInjection() {
        // 测试NotificationService注入
        NotificationService notificationService = getService(NotificationService.class);
        assertInjected(notificationService, "NotificationService应该被正确注入");
        
        // 验证服务不为空
        assertNotNull(notificationService, "NotificationService实例不应该为空");
    }
    
    @Test
    void testNotificationServiceBasicMethods() {
        // 测试NotificationService的基本方法调用
        NotificationService notificationService = getService(NotificationService.class);
        assertNotNull(notificationService, "NotificationService应该被注入");
        
        // 测试获取用户通知列表
        ServiceResult<PageResultDTO<Notification>> result = notificationService.getUserNotifications(1L, 1, 10, null, null);
        assertNotNull(result, "应该返回ServiceResult");
        assertTrue(result.isSuccess(), "应该返回成功结果");
        
        System.out.println("NotificationService基本方法测试通过");
    }
    
    @Test
    void testNotificationServiceCreateMethods() {
        // 测试创建通知方法
        NotificationService notificationService = getService(NotificationService.class);
        assertNotNull(notificationService, "NotificationService应该被注入");
        
        // 测试创建通知
        ServiceResult<Notification> createResult = notificationService.createNotification(1L, "test", "test message", "info", "normal");
        assertNotNull(createResult, "创建通知应该返回结果");
        assertTrue(createResult.isSuccess(), "创建通知应该成功");
        
        System.out.println("NotificationService创建方法测试通过");
    }
    
    @Test
    void testNotificationServiceImplementation() {
        // 测试NotificationService实现
        NotificationService notificationService = getService(NotificationService.class);
        assertNotNull(notificationService, "NotificationService应该被注入");
        
        // 验证服务实现
        assertTrue(notificationService != null, "NotificationService应该被正确实现");
    }
} 