package cloud.compan.servlet.service;

import cloud.compan.servlet.web.SimpleTestBase;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.model.Acl;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * AclService单元测试
 * 测试访问控制列表服务的功能
 */
public class AclServiceTest extends SimpleTestBase {
    
    @Test
    void testAclServiceInjection() {
        // 测试AclService注入
        AclService aclService = getService(AclService.class);
        assertInjected(aclService, "AclService应该被正确注入");
        
        // 验证服务不为空
        assertNotNull(aclService, "AclService实例不应该为空");
    }
    
    @Test
    void testAclServiceBasicMethods() {
        // 测试AclService的基本方法调用
        AclService aclService = getService(AclService.class);
        assertNotNull(aclService, "AclService应该被注入");
        
        // 测试基本方法调用
        ServiceResult<List<Acl>> result = aclService.getResourcePermissions("file", 1L, 1L);
        assertNotNull(result, "应该返回ServiceResult");
        assertTrue(result.isSuccess(), "应该返回成功结果");
        
        System.out.println("AclService基本方法测试通过");
    }
    
    @Test
    void testAclServicePermissionMethods() {
        // 测试权限相关方法
        AclService aclService = getService(AclService.class);
        assertNotNull(aclService, "AclService应该被注入");
        
        // 测试权限检查方法
        ServiceResult<Boolean> hasPermissionResult = aclService.checkPermission("file", 1L, 1L, "read");
        assertNotNull(hasPermissionResult, "权限检查应该返回结果");
        assertTrue(hasPermissionResult.isSuccess(), "权限检查应该成功");
        
        System.out.println("AclService权限方法测试通过");
    }
    
    @Test
    void testAclServiceImplementation() {
        // 测试AclService实现
        AclService aclService = getService(AclService.class);
        assertNotNull(aclService, "AclService应该被注入");
        
        // 验证服务实现
        assertTrue(aclService != null, "AclService应该被正确实现");
    }
} 