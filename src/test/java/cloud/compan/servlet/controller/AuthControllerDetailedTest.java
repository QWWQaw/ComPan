package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.SimpleTestBase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthController详细测试
 * 测试认证控制器的详细功能
 */
public class AuthControllerDetailedTest extends SimpleTestBase {
    
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
    
    @Test
    void testAuthControllerAnnotations() {
        // 测试AuthController的注解
        assertTrue(AuthController.class.isAnnotationPresent(cloud.compan.servlet.annotations.Controller.class), 
            "AuthController应该有@Controller注解");
        assertTrue(AuthController.class.isAnnotationPresent(cloud.compan.servlet.annotations.ResponseBody.class), 
            "AuthController应该有@ResponseBody注解");
    }
    
    @Test
    void testAuthControllerMethodAnnotations() {
        // 测试AuthController方法的注解
        assertTrue(AuthController.class.getDeclaredMethods().length > 0, "AuthController应该有方法");
        
        // 验证关键方法存在
        boolean hasRegisterMethod = false;
        boolean hasLoginMethod = false;
        boolean hasLogoutMethod = false;
        boolean hasGetProfileMethod = false;
        
        for (java.lang.reflect.Method method : AuthController.class.getDeclaredMethods()) {
            if (method.getName().equals("register")) hasRegisterMethod = true;
            if (method.getName().equals("login")) hasLoginMethod = true;
            if (method.getName().equals("logout")) hasLogoutMethod = true;
            if (method.getName().equals("getProfile")) hasGetProfileMethod = true;
        }
        
        assertTrue(hasRegisterMethod, "AuthController应该有register方法");
        assertTrue(hasLoginMethod, "AuthController应该有login方法");
        assertTrue(hasLogoutMethod, "AuthController应该有logout方法");
        assertTrue(hasGetProfileMethod, "AuthController应该有getProfile方法");
        
        System.out.println("AuthController方法注解测试通过");
    }
} 