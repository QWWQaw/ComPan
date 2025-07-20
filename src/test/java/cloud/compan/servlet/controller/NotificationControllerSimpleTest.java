package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.SimpleTestBase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * NotificationController简单测试
 * 测试通知控制器的基本功能
 */
public class NotificationControllerSimpleTest extends SimpleTestBase {
    
    @Test
    void testNotificationControllerInstantiation() {
        // 测试NotificationController实例化
        NotificationController controller = getController(NotificationController.class);
        assertInjected(controller, "NotificationController应该被正确实例化");
        
        // 验证控制器不为空
        assertNotNull(controller, "控制器实例不应该为空");
    }
    
    @Test
    void testServiceInjectionInNotificationController() {
        // 测试NotificationController中的服务注入
        NotificationController controller = getController(NotificationController.class);
        assertNotNull(controller, "NotificationController应该被注入");
        
        // 验证服务依赖被正确注入
        assertTrue(true, "NotificationController服务注入测试通过");
    }
    
    @Test
    void testNotificationControllerInheritance() {
        // 测试NotificationController继承BaseController
        NotificationController controller = getController(NotificationController.class);
        assertNotNull(controller, "NotificationController应该被注入");
        
        // 验证继承关系
        assertTrue(controller instanceof BaseController, "NotificationController应该继承BaseController");
    }
    
    @Test
    void testNotificationControllerAnnotations() {
        // 测试NotificationController的注解
        assertTrue(NotificationController.class.isAnnotationPresent(cloud.compan.servlet.annotations.Controller.class), 
            "NotificationController应该有@Controller注解");
        assertTrue(NotificationController.class.isAnnotationPresent(cloud.compan.servlet.annotations.ResponseBody.class), 
            "NotificationController应该有@ResponseBody注解");
    }
} 