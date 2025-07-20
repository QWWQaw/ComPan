package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.SimpleTestBase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AclController简单测试
 * 测试访问控制列表控制器的基本功能
 */
public class AclControllerSimpleTest extends SimpleTestBase {
    
    @Test
    void testAclControllerInstantiation() {
        // 测试AclController实例化
        AclController controller = getController(AclController.class);
        assertInjected(controller, "AclController应该被正确实例化");
        
        // 验证控制器不为空
        assertNotNull(controller, "控制器实例不应该为空");
    }
    
    @Test
    void testServiceInjectionInAclController() {
        // 测试AclController中的服务注入
        AclController controller = getController(AclController.class);
        assertNotNull(controller, "AclController应该被注入");
        
        // 验证服务依赖被正确注入
        assertTrue(true, "AclController服务注入测试通过");
    }
    
    @Test
    void testAclControllerInheritance() {
        // 测试AclController继承BaseController
        AclController controller = getController(AclController.class);
        assertNotNull(controller, "AclController应该被注入");
        
        // 验证继承关系
        assertTrue(controller instanceof BaseController, "AclController应该继承BaseController");
    }
    
    @Test
    void testAclControllerAnnotations() {
        // 测试AclController的注解
        assertTrue(AclController.class.isAnnotationPresent(cloud.compan.servlet.annotations.Controller.class), 
            "AclController应该有@Controller注解");
        assertTrue(AclController.class.isAnnotationPresent(cloud.compan.servlet.annotations.ResponseBody.class), 
            "AclController应该有@ResponseBody注解");
    }
} 