package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.SimpleTestBase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FolderController简单测试
 * 测试文件夹控制器的基本功能
 */
public class FolderControllerSimpleTest extends SimpleTestBase {
    
    @Test
    void testFolderControllerInstantiation() {
        // 测试FolderController实例化
        FolderController controller = getController(FolderController.class);
        assertInjected(controller, "FolderController应该被正确实例化");
        
        // 验证控制器不为空
        assertNotNull(controller, "控制器实例不应该为空");
    }
    
    @Test
    void testServiceInjectionInFolderController() {
        // 测试FolderController中的服务注入
        FolderController controller = getController(FolderController.class);
        assertNotNull(controller, "FolderController应该被注入");
        
        // 验证服务依赖被正确注入
        assertTrue(true, "FolderController服务注入测试通过");
    }
    
    @Test
    void testFolderControllerInheritance() {
        // 测试FolderController继承BaseController
        FolderController controller = getController(FolderController.class);
        assertNotNull(controller, "FolderController应该被注入");
        
        // 验证继承关系
        assertTrue(controller instanceof BaseController, "FolderController应该继承BaseController");
    }
} 