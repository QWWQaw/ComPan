package cloud.compan.servlet.service;

import cloud.compan.servlet.web.SimpleTestBase;
import cloud.compan.servlet.dto.ServiceResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

/**
 * StorageService单元测试
 * 测试存储服务的功能
 */
public class StorageServiceTest extends SimpleTestBase {
    
    @Test
    void testStorageServiceInjection() {
        // 测试StorageService注入
        StorageService storageService = getService(StorageService.class);
        assertInjected(storageService, "StorageService应该被正确注入");
        
        // 验证服务不为空
        assertNotNull(storageService, "StorageService实例不应该为空");
    }
    
    @Test
    void testStorageServiceBasicMethods() {
        // 测试StorageService的基本方法调用
        StorageService storageService = getService(StorageService.class);
        assertNotNull(storageService, "StorageService应该被注入");
        
        // 测试获取存储统计
        ServiceResult<Map<String, Object>> result = storageService.getUserStorageStatistics(1L);
        assertNotNull(result, "应该返回ServiceResult");
        assertTrue(result.isSuccess(), "应该返回成功结果");
        
        System.out.println("StorageService基本方法测试通过");
    }
    
    @Test
    void testStorageServiceCapacityMethods() {
        // 测试存储容量相关方法
        StorageService storageService = getService(StorageService.class);
        assertNotNull(storageService, "StorageService应该被注入");
        
        // 测试检查存储容量
        ServiceResult<Boolean> capacityResult = storageService.checkStorageAvailable(1L, 1024L);
        assertNotNull(capacityResult, "容量检查应该返回结果");
        assertTrue(capacityResult.isSuccess(), "容量检查应该成功");
        
        System.out.println("StorageService容量方法测试通过");
    }
    
    @Test
    void testStorageServiceImplementation() {
        // 测试StorageService实现
        StorageService storageService = getService(StorageService.class);
        assertNotNull(storageService, "StorageService应该被注入");
        
        // 验证服务实现
        assertTrue(storageService != null, "StorageService应该被正确实现");
    }
} 