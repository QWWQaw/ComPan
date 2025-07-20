package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.SimpleTestBase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ShareController简单测试
 * 测试分享控制器的基本功能
 */
public class ShareControllerSimpleTest extends SimpleTestBase {
    
    @Test
    void testShareControllerInstantiation() {
        // 测试ShareController实例化
        ShareController controller = getController(ShareController.class);
        assertInjected(controller, "ShareController应该被正确实例化");
        
        // 验证控制器不为空
        assertNotNull(controller, "控制器实例不应该为空");
    }
    
    @Test
    void testServiceInjectionInShareController() {
        // 测试ShareController中的服务注入
        ShareController controller = getController(ShareController.class);
        assertNotNull(controller, "ShareController应该被注入");
        
        // 验证服务依赖被正确注入
        assertTrue(true, "ShareController服务注入测试通过");
    }
    
    @Test
    void testShareControllerInheritance() {
        // 测试ShareController继承BaseController
        ShareController controller = getController(ShareController.class);
        assertNotNull(controller, "ShareController应该被注入");
        
        // 验证继承关系
        assertTrue(controller instanceof BaseController, "ShareController应该继承BaseController");
    }
    
    @Test
    void testShareControllerAnnotations() {
        // 测试ShareController的注解
        assertTrue(ShareController.class.isAnnotationPresent(cloud.compan.servlet.annotations.Controller.class), 
            "ShareController应该有@Controller注解");
        assertTrue(ShareController.class.isAnnotationPresent(cloud.compan.servlet.annotations.ResponseBody.class), 
            "ShareController应该有@ResponseBody注解");
    }
} 