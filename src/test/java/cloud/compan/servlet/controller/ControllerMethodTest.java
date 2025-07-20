package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.SimpleTestBase;
import cloud.compan.servlet.web.response.ApiResponseWrapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;

/**
 * 控制器方法调用测试
 * 测试控制器的具体方法调用
 */
public class ControllerMethodTest extends SimpleTestBase {
    
    @Test
    void testTestControllerMethodReflection() {
        // 使用反射测试TestController的方法
        TestController controller = getController(TestController.class);
        assertNotNull(controller, "TestController应该被注入");
        
        try {
            // 获取handleTest方法
            Method handleTestMethod = TestController.class.getMethod("handleTest", 
                Map.class, jakarta.servlet.http.HttpServletRequest.class);
            
            assertNotNull(handleTestMethod, "应该能找到handleTest方法");
            assertEquals("handleTest", handleTestMethod.getName(), "方法名应该是handleTest");
            
            System.out.println("TestController方法反射测试通过");
            
        } catch (NoSuchMethodException e) {
            fail("应该能找到handleTest方法: " + e.getMessage());
        }
    }
    
    @Test
    void testDemoControllerMethodReflection() {
        // 使用反射测试DemoController的方法
        DemoController controller = getController(DemoController.class);
        assertNotNull(controller, "DemoController应该被注入");
        
        try {
            // 获取getExample方法
            Method getExampleMethod = DemoController.class.getMethod("getExample");
            assertNotNull(getExampleMethod, "应该能找到getExample方法");
            
            // 获取postExample方法
            Method postExampleMethod = DemoController.class.getMethod("postExample", 
                Map.class);
            assertNotNull(postExampleMethod, "应该能找到postExample方法");
            
            System.out.println("DemoController方法反射测试通过");
            
        } catch (NoSuchMethodException e) {
            fail("应该能找到DemoController的方法: " + e.getMessage());
        }
    }
    
    @Test
    void testControllerAnnotations() {
        // 测试控制器的注解
        TestController testController = getController(TestController.class);
        DemoController demoController = getController(DemoController.class);
        
        // 验证@Controller注解
        assertTrue(TestController.class.isAnnotationPresent(cloud.compan.servlet.annotations.Controller.class), 
            "TestController应该有@Controller注解");
        assertTrue(DemoController.class.isAnnotationPresent(cloud.compan.servlet.annotations.Controller.class), 
            "DemoController应该有@Controller注解");
        
        // 验证@ResponseBody注解
        assertTrue(TestController.class.isAnnotationPresent(cloud.compan.servlet.annotations.ResponseBody.class), 
            "TestController应该有@ResponseBody注解");
        assertTrue(DemoController.class.isAnnotationPresent(cloud.compan.servlet.annotations.ResponseBody.class), 
            "DemoController应该有@ResponseBody注解");
        
        System.out.println("控制器注解测试通过");
    }
    
    @Test
    void testControllerMethodAnnotations() {
        // 测试控制器方法的注解
        try {
            Method handleTestMethod = TestController.class.getMethod("handleTest", 
                Map.class, jakarta.servlet.http.HttpServletRequest.class);
            
            // 验证@PutMapping注解
            assertTrue(handleTestMethod.isAnnotationPresent(cloud.compan.servlet.annotations.PutMapping.class), 
                "handleTest方法应该有@PutMapping注解");
            
            // 验证@RequestBody注解
            assertTrue(handleTestMethod.getParameters()[0].isAnnotationPresent(cloud.compan.servlet.annotations.RequestBody.class), 
                "第一个参数应该有@RequestBody注解");
            
            System.out.println("控制器方法注解测试通过");
            
        } catch (NoSuchMethodException e) {
            fail("应该能找到handleTest方法: " + e.getMessage());
        }
    }
} 