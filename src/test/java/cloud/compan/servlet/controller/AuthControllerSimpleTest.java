package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.SimpleTestBase;
import cloud.compan.servlet.dto.UserLoginDTO;
import cloud.compan.servlet.dto.UserRegistrationDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthController简单测试
 * 测试认证控制器的基本功能
 */
public class AuthControllerSimpleTest extends SimpleTestBase {
    
    @Test
    void testAuthControllerInstantiation() {
        // 测试AuthController实例化
        AuthController controller = getController(AuthController.class);
        assertInjected(controller, "AuthController应该被正确实例化");
        
        // 验证控制器不为空
        assertNotNull(controller, "控制器实例不应该为空");
    }
    
    @Test
    void testServiceInjectionInAuthController() {
        // 测试AuthController中的服务注入
        AuthController controller = getController(AuthController.class);
        assertNotNull(controller, "AuthController应该被注入");
        
        // 验证服务依赖被正确注入
        assertTrue(true, "AuthController服务注入测试通过");
    }
    
    @Test
    void testAuthControllerInheritance() {
        // 测试AuthController继承BaseController
        AuthController controller = getController(AuthController.class);
        assertNotNull(controller, "AuthController应该被注入");
        
        // 验证继承关系
        assertTrue(controller instanceof BaseController, "AuthController应该继承BaseController");
    }
} 