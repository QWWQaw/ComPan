package cloud.compan.servlet.service;

import cloud.compan.servlet.web.SimpleTestBase;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.PageResultDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

/**
 * RecycleBinService单元测试
 * 测试回收站服务的功能
 */
public class RecycleBinServiceTest extends SimpleTestBase {
    
    @Test
    void testRecycleBinServiceInjection() {
        // 测试RecycleBinService注入
        RecycleBinService recycleBinService = getService(RecycleBinService.class);
        assertInjected(recycleBinService, "RecycleBinService应该被正确注入");
        
        // 验证服务不为空
        assertNotNull(recycleBinService, "RecycleBinService实例不应该为空");
    }
    
    @Test
    void testRecycleBinServiceBasicMethods() {
        // 测试RecycleBinService的基本方法调用
        RecycleBinService recycleBinService = getService(RecycleBinService.class);
        assertNotNull(recycleBinService, "RecycleBinService应该被注入");
        
        // 测试获取回收站项目列表
        ServiceResult<PageResultDTO<Map<String, Object>>> result = recycleBinService.getRecycleBinContents(1L, 1, 10, "all", null, null);
        assertNotNull(result, "应该返回ServiceResult");
        assertTrue(result.isSuccess(), "应该返回成功结果");
        
        System.out.println("RecycleBinService基本方法测试通过");
    }
    
    @Test
    void testRecycleBinServiceRestoreMethods() {
        // 测试恢复方法
        RecycleBinService recycleBinService = getService(RecycleBinService.class);
        assertNotNull(recycleBinService, "RecycleBinService应该被注入");
        
        // 测试恢复项目
        ServiceResult<Map<String, Object>> restoreResult = recycleBinService.restoreFromRecycleBin("file", 1L, null, 1L);
        assertNotNull(restoreResult, "恢复项目应该返回结果");
        assertTrue(restoreResult.isSuccess(), "恢复项目应该成功");
        
        System.out.println("RecycleBinService恢复方法测试通过");
    }
    
    @Test
    void testRecycleBinServiceImplementation() {
        // 测试RecycleBinService实现
        RecycleBinService recycleBinService = getService(RecycleBinService.class);
        assertNotNull(recycleBinService, "RecycleBinService应该被注入");
        
        // 验证服务实现
        assertTrue(recycleBinService != null, "RecycleBinService应该被正确实现");
    }
} 