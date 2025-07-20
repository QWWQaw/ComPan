package cloud.compan.servlet.web;

import cloud.compan.servlet.controller.TestController;
import cloud.compan.servlet.controller.DemoController;
import cloud.compan.servlet.annotations.enums.RequestMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Method;

/**
 * 手动路由注册测试
 * 不依赖自动扫描，手动注册路由进行测试
 */
public class ManualRouteTest extends WebTestBase {
    
    private RouteRegistry routeRegistry;
    
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        routeRegistry = injector.getInstance(RouteRegistry.class);
        
        // 手动注册路由
        registerTestRoutes();
    }
    
    private void registerTestRoutes() {
        try {
            // 获取控制器实例
            TestController testController = injector.getInstance(TestController.class);
            DemoController demoController = injector.getInstance(DemoController.class);
            
            // 获取方法
            Method testMethod = TestController.class.getMethod("handleTest");
            Method demoGetMethod = DemoController.class.getMethod("getExample");
            Method demoPostMethod = DemoController.class.getMethod("postExample");
            
            // 手动注册TestController的路由
            RouteInfo testRoute = new RouteInfo(
                "/test",
                RequestMethod.GET,
                TestController.class,
                testMethod,
                testController
            );
            routeRegistry.registerRoute(testRoute);
            
            // 手动注册DemoController的路由
            RouteInfo demoGetRoute = new RouteInfo(
                "/demo",
                RequestMethod.GET,
                DemoController.class,
                demoGetMethod,
                demoController
            );
            routeRegistry.registerRoute(demoGetRoute);
            
            RouteInfo demoPostRoute = new RouteInfo(
                "/demo",
                RequestMethod.POST,
                DemoController.class,
                demoPostMethod,
                demoController
            );
            routeRegistry.registerRoute(demoPostRoute);
            
            System.out.println("手动注册了 " + routeRegistry.getRouteCount() + " 个路由");
            
        } catch (Exception e) {
            System.err.println("注册路由时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test
    void testManualRouteRegistration() {
        // 验证路由被正确注册
        assertTrue(routeRegistry.hasRoute("/test", RequestMethod.GET), "TestController路由应该被注册");
        assertTrue(routeRegistry.hasRoute("/demo", RequestMethod.GET), "DemoController GET路由应该被注册");
        assertTrue(routeRegistry.hasRoute("/demo", RequestMethod.POST), "DemoController POST路由应该被注册");
        
        System.out.println("路由注册验证通过");
    }
    
    @Test
    void testRouteLookup() {
        // 测试路由查找
        var testRoute = routeRegistry.findRoute("/test", RequestMethod.GET);
        assertTrue(testRoute.isPresent(), "应该能找到TestController路由");
        
        var demoRoute = routeRegistry.findRoute("/demo", RequestMethod.GET);
        assertTrue(demoRoute.isPresent(), "应该能找到DemoController路由");
        
        System.out.println("路由查找测试通过");
    }
} 