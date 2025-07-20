package cloud.compan.servlet.service;

import cloud.compan.servlet.web.SimpleTestBase;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.model.Share;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ShareService单元测试
 * 测试分享服务的功能
 */
public class ShareServiceTest extends SimpleTestBase {
    
    @Test
    void testShareServiceInjection() {
        // 测试ShareService注入
        ShareService shareService = getService(ShareService.class);
        assertInjected(shareService, "ShareService应该被正确注入");
        
        // 验证服务不为空
        assertNotNull(shareService, "ShareService实例不应该为空");
    }
    
    @Test
    void testShareServiceBasicMethods() {
        // 测试ShareService的基本方法调用
        ShareService shareService = getService(ShareService.class);
        assertNotNull(shareService, "ShareService应该被注入");
        
        // 测试获取用户分享列表
        ServiceResult<PageResultDTO<Share>> result = shareService.getUserShares(1L, 1, 10, null, null);
        assertNotNull(result, "应该返回ServiceResult");
        assertTrue(result.isSuccess(), "应该返回成功结果");
        
        System.out.println("ShareService基本方法测试通过");
    }
    
    @Test
    void testShareServiceCreateMethods() {
        // 测试创建分享方法
        ShareService shareService = getService(ShareService.class);
        assertNotNull(shareService, "ShareService应该被注入");
        
        // 测试创建分享
        ServiceResult<Share> createResult = shareService.createFileShare(1L, "public", null, null, null, 1L);
        assertNotNull(createResult, "创建分享应该返回结果");
        assertTrue(createResult.isSuccess(), "创建分享应该成功");
        
        System.out.println("ShareService创建方法测试通过");
    }
    
    @Test
    void testShareServiceImplementation() {
        // 测试ShareService实现
        ShareService shareService = getService(ShareService.class);
        assertNotNull(shareService, "ShareService应该被注入");
        
        // 验证服务实现
        assertTrue(shareService != null, "ShareService应该被正确实现");
    }
} 