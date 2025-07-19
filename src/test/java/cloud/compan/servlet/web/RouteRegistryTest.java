package cloud.compan.servlet.web;

import cloud.compan.servlet.annotations.enums.RequestMethod;
import cloud.compan.servlet.controller.TestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RouteRegistry 路由注册表测试
 */
@DisplayName("RouteRegistry 路由注册表测试")
class RouteRegistryTest {
    
    private RouteRegistry routeRegistry;
    private TestController testController;
    
    @BeforeEach
    void setUp() {
        routeRegistry = new RouteRegistry();
        testController = new TestController();
    }
    
    @Test
    @DisplayName("测试基本路由注册")
    void testBasicRouteRegistration() throws Exception {
        // 创建路由信息
        Method helloMethod = TestController.class.getDeclaredMethod("hello");
        RouteInfo routeInfo = new RouteInfo("/api/hello", RequestMethod.GET, 
                                          TestController.class, helloMethod, testController);
        
        // 注册路由
        routeRegistry.registerRoute(routeInfo);
        
        // 验证路由被注册
        assertEquals(1, routeRegistry.getRouteCount());
        assertTrue(routeRegistry.hasRoute("/api/hello", RequestMethod.GET));
        
        // 查找路由
        var foundRoute = routeRegistry.findRoute("/api/hello", RequestMethod.GET);
        assertTrue(foundRoute.isPresent());
        assertEquals("/api/hello", foundRoute.get().getPath());
        assertEquals(RequestMethod.GET, foundRoute.get().getHttpMethod());
    }
    
    @Test
    @DisplayName("测试路由查找")
    void testRouteFinding() throws Exception {
        // 注册多个路由
        Method helloMethod = TestController.class.getDeclaredMethod("hello");
        Method healthMethod = TestController.class.getDeclaredMethod("health");
        
        RouteInfo helloRoute = new RouteInfo("/api/hello", RequestMethod.GET, 
                                           TestController.class, helloMethod, testController);
        RouteInfo healthRoute = new RouteInfo("/api/health", RequestMethod.GET, 
                                            TestController.class, healthMethod, testController);
        
        routeRegistry.registerRoute(helloRoute);
        routeRegistry.registerRoute(healthRoute);
        
        // 测试查找存在的路由
        assertTrue(routeRegistry.findRoute("/api/hello", RequestMethod.GET).isPresent());
        assertTrue(routeRegistry.findRoute("/api/health", RequestMethod.GET).isPresent());
        
        // 测试查找不存在的路由
        assertFalse(routeRegistry.findRoute("/api/nonexistent", RequestMethod.GET).isPresent());
        assertFalse(routeRegistry.findRoute("/api/hello", RequestMethod.POST).isPresent());
    }
    
    @Test
    @DisplayName("测试路由冲突检测")
    void testRouteConflictDetection() throws Exception {
        // 注册第一个路由
        Method helloMethod = TestController.class.getDeclaredMethod("hello");
        RouteInfo routeInfo1 = new RouteInfo("/api/test", RequestMethod.GET, 
                                           TestController.class, helloMethod, testController);
        routeRegistry.registerRoute(routeInfo1);
        
        // 尝试注册冲突的路由
        RouteInfo routeInfo2 = new RouteInfo("/api/test", RequestMethod.GET, 
                                           TestController.class, helloMethod, testController);
        
        assertThrows(IllegalArgumentException.class, () -> {
            routeRegistry.registerRoute(routeInfo2);
        });
    }
    
    @Test
    @DisplayName("测试参数化路由注册")
    void testParameterizedRouteRegistration() throws Exception {
        // 创建参数化路由
        Method helloMethod = TestController.class.getDeclaredMethod("hello");
        ParameterizedRouteInfo paramRoute = new ParameterizedRouteInfo("/api/users/{id}", RequestMethod.GET,
                                                                      TestController.class, helloMethod, testController);
        
        // 注册路由
        routeRegistry.registerRoute(paramRoute);
        
        // 验证注册
        assertEquals(1, routeRegistry.getRouteCount());
        
        // 测试参数化路由匹配
        var foundRoute = routeRegistry.findRoute("/api/users/123", RequestMethod.GET);
        assertTrue(foundRoute.isPresent());
        assertTrue(foundRoute.get() instanceof ParameterizedRouteInfo);
        
        ParameterizedRouteInfo foundParamRoute = (ParameterizedRouteInfo) foundRoute.get();
        assertTrue(foundParamRoute.hasPathVariables());
        
        // 测试路径变量提取
        Map<String, String> variables = foundParamRoute.extractPathVariables("/api/users/123");
        assertEquals("123", variables.get("id"));
    }
    
    @Test
    @DisplayName("测试路由清空")
    void testClearRoutes() throws Exception {
        // 注册一些路由
        Method helloMethod = TestController.class.getDeclaredMethod("hello");
        RouteInfo routeInfo = new RouteInfo("/api/test", RequestMethod.GET, 
                                          TestController.class, helloMethod, testController);
        routeRegistry.registerRoute(routeInfo);
        
        assertEquals(1, routeRegistry.getRouteCount());
        
        // 清空路由
        routeRegistry.clearRoutes();
        
        assertEquals(0, routeRegistry.getRouteCount());
        assertFalse(routeRegistry.findRoute("/api/test", RequestMethod.GET).isPresent());
    }
    
    @Test
    @DisplayName("测试HTTP方法字符串查找")
    void testHttpMethodStringLookup() throws Exception {
        // 注册路由
        Method helloMethod = TestController.class.getDeclaredMethod("hello");
        RouteInfo routeInfo = new RouteInfo("/api/hello", RequestMethod.GET, 
                                          TestController.class, helloMethod, testController);
        routeRegistry.registerRoute(routeInfo);
        
        // 使用字符串方法查找
        var foundRoute = routeRegistry.findRoute("/api/hello", "GET");
        assertTrue(foundRoute.isPresent());
        
        // 测试无效的HTTP方法
        var notFoundRoute = routeRegistry.findRoute("/api/hello", "INVALID");
        assertFalse(notFoundRoute.isPresent());
    }
} 