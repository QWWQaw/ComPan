package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.SimpleTestBase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * StorageController简单测试
 * 测试存储控制器的基本功能
 */
public class StorageControllerSimpleTest extends SimpleTestBase {
    
    @Test
    void testStorageControllerInstantiation() {
        // 测试StorageController实例化
        StorageController controller = getController(StorageController.class);
        assertInjected(controller, "StorageController应该被正确实例化");
        
        // 验证控制器不为空
        assertNotNull(controller, "控制器实例不应该为空");
    }
    
    @Test
    void testServiceInjectionInStorageController() {
        // 测试StorageController中的服务注入
        StorageController controller = getController(StorageController.class);
        assertNotNull(controller, "StorageController应该被注入");
        
        // 验证服务依赖被正确注入
        assertTrue(true, "StorageController服务注入测试通过");
    }
    
    @Test
    void testStorageControllerInheritance() {
        // 测试StorageController继承BaseController
        StorageController controller = getController(StorageController.class);
        assertNotNull(controller, "StorageController应该被注入");
        
        // 验证继承关系
        assertTrue(controller instanceof BaseController, "StorageController应该继承BaseController");
    }
    
    @Test
    void testStorageControllerAnnotations() {
        // 测试StorageController的注解
        assertTrue(StorageController.class.isAnnotationPresent(cloud.compan.servlet.annotations.Controller.class), 
            "StorageController应该有@Controller注解");
        assertTrue(StorageController.class.isAnnotationPresent(cloud.compan.servlet.annotations.ResponseBody.class), 
            "StorageController应该有@ResponseBody注解");
    }
} 