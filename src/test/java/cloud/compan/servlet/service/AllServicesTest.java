package cloud.compan.servlet.service;

import cloud.compan.servlet.web.SimpleTestBase;
import cloud.compan.servlet.dto.ServiceResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

/**
 * 所有Service的综合测试
 * 测试所有服务的基本功能
 */
public class AllServicesTest extends SimpleTestBase {
    
    @Test
    void testAllServicesInjection() {
        // 测试所有服务的注入
        AclService aclService = getService(AclService.class);
        NotificationService notificationService = getService(NotificationService.class);
        RecycleBinService recycleBinService = getService(RecycleBinService.class);
        StorageService storageService = getService(StorageService.class);
        ShareService shareService = getService(ShareService.class);
        
        // 验证所有服务都不为空
        assertNotNull(aclService, "AclService应该被正确注入");
        assertNotNull(notificationService, "NotificationService应该被正确注入");
        assertNotNull(recycleBinService, "RecycleBinService应该被正确注入");
        assertNotNull(storageService, "StorageService应该被正确注入");
        assertNotNull(shareService, "ShareService应该被正确注入");
        
        System.out.println("所有Service注入测试通过");
    }
    
    @Test
    void testAllServicesBasicMethods() {
        // 测试所有服务的基本方法调用
        AclService aclService = getService(AclService.class);
        NotificationService notificationService = getService(NotificationService.class);
        RecycleBinService recycleBinService = getService(RecycleBinService.class);
        StorageService storageService = getService(StorageService.class);
        ShareService shareService = getService(ShareService.class);
        
        // 测试AclService
        ServiceResult<Boolean> aclResult = aclService.checkPermission("file", 1L, 1L, "read");
        assertNotNull(aclResult, "AclService应该返回结果");
        
        // 测试NotificationService
        ServiceResult<Long> notificationResult = notificationService.getUnreadNotificationCount(1L);
        assertNotNull(notificationResult, "NotificationService应该返回结果");
        
        // 测试RecycleBinService
        ServiceResult<Map<String, Object>> recycleResult = recycleBinService.getRecycleBinStatistics(1L);
        assertNotNull(recycleResult, "RecycleBinService应该返回结果");
        
        // 测试StorageService
        ServiceResult<Map<String, Object>> storageResult = storageService.getUserStorageStatistics(1L);
        assertNotNull(storageResult, "StorageService应该返回结果");
        
        // 测试ShareService
        ServiceResult<Map<String, Object>> shareResult = shareService.accessShare("test", null, null);
        assertNotNull(shareResult, "ShareService应该返回结果");
        
        System.out.println("所有Service基本方法测试通过");
    }
    
    @Test
    void testAllServicesImplementation() {
        // 测试所有服务的实现
        AclService aclService = getService(AclService.class);
        NotificationService notificationService = getService(NotificationService.class);
        RecycleBinService recycleBinService = getService(RecycleBinService.class);
        StorageService storageService = getService(StorageService.class);
        ShareService shareService = getService(ShareService.class);
        
        // 验证服务实现
        assertTrue(aclService != null, "AclService应该被正确实现");
        assertTrue(notificationService != null, "NotificationService应该被正确实现");
        assertTrue(recycleBinService != null, "RecycleBinService应该被正确实现");
        assertTrue(storageService != null, "StorageService应该被正确实现");
        assertTrue(shareService != null, "ShareService应该被正确实现");
        
        System.out.println("所有Service实现测试通过");
    }
} 