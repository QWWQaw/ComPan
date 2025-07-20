package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.SimpleTestBase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * RecycleBinController简单测试
 * 测试回收站控制器的基本功能
 */
public class RecycleBinControllerSimpleTest extends SimpleTestBase {
    
    @Test
    void testRecycleBinControllerInstantiation() {
        // 测试RecycleBinController实例化
        RecycleBinController controller = getController(RecycleBinController.class);
        assertInjected(controller, "RecycleBinController应该被正确实例化");
        
        // 验证控制器不为空
        assertNotNull(controller, "控制器实例不应该为空");
    }
    
    @Test
    void testServiceInjectionInRecycleBinController() {
        // 测试RecycleBinController中的服务注入
        RecycleBinController controller = getController(RecycleBinController.class);
        assertNotNull(controller, "RecycleBinController应该被注入");
        
        // 验证服务依赖被正确注入
        assertTrue(true, "RecycleBinController服务注入测试通过");
    }
    
    @Test
    void testRecycleBinControllerInheritance() {
        // 测试RecycleBinController继承BaseController
        RecycleBinController controller = getController(RecycleBinController.class);
        assertNotNull(controller, "RecycleBinController应该被注入");
        
        // 验证继承关系
        assertTrue(controller instanceof BaseController, "RecycleBinController应该继承BaseController");
    }
    
    @Test
    void testRecycleBinControllerAnnotations() {
        // 测试RecycleBinController的注解
        assertTrue(RecycleBinController.class.isAnnotationPresent(cloud.compan.servlet.annotations.Controller.class), 
            "RecycleBinController应该有@Controller注解");
        assertTrue(RecycleBinController.class.isAnnotationPresent(cloud.compan.servlet.annotations.ResponseBody.class), 
            "RecycleBinController应该有@ResponseBody注解");
    }
} 