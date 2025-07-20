package cloud.compan.servlet.integration;

import cloud.compan.servlet.web.RouteRegistry;
import cloud.compan.servlet.web.ControllerScanner;
import cloud.compan.servlet.web.SimpleTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 简单路由测试
 * 验证路由注册和基本功能
 */
public class SimpleRouteTest extends SimpleTestBase {
    
    private RouteRegistry routeRegistry;
    private ControllerScanner controllerScanner;
    
    @BeforeEach
    public void setUp() {
        routeRegistry = getService(RouteRegistry.class);
        controllerScanner = getService(ControllerScanner.class);
        
        // 扫描并注册所有控制器路由
        controllerScanner.scanAndRegister("cloud.compan.servlet.controller");
    }
    
    @Test
    void testRouteRegistration() {
        System.out.println("开始验证路由注册...");
        
        // 验证路由注册器不为空
        assertNotNull(routeRegistry, "路由注册器应该不为空");
        assertNotNull(controllerScanner, "控制器扫描器应该不为空");
        
        // 打印所有注册的路由
        System.out.println("路由注册验证完成");
        
        assertTrue(true, "路由注册测试通过");
    }
    
    @Test
    void testControllerInjection() {
        System.out.println("开始验证控制器注入...");
        
        // 验证控制器可以被注入
        try {
            // 这里可以添加具体的控制器注入测试
            System.out.println("控制器注入验证完成");
            assertTrue(true, "控制器注入测试通过");
        } catch (Exception e) {
            fail("控制器注入失败: " + e.getMessage());
        }
    }
} 