package cloud.compan.servlet.web;

import cloud.compan.servlet.annotations.enums.RequestMethod;
import cloud.compan.servlet.controller.EnhancedTestController;
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
    private EnhancedTestController testController;
    
    @BeforeEach
    void setUp() {
        routeRegistry = new RouteRegistry();
        testController = new EnhancedTestController();
    }
    
    @Test
    @DisplayName("测试简单路由注册和查找")
    void testSimpleRouteRegistration() throws Exception {
        // Given
        Method method = EnhancedTestController.class.getMethod("apiInfo");
        RouteInfo routeInfo = new RouteInfo("/api/test", RequestMethod.GET, 
                                          EnhancedTestController.class, method, testController);
        
        // When
        routeRegistry.registerRoute(routeInfo);
        var foundRouteOpt = routeRegistry.findRoute("/api/test", RequestMethod.GET);
        
        // Then
        assertTrue(foundRouteOpt.isPresent());
        RouteInfo foundRoute = foundRouteOpt.get();
        assertEquals("/api/test", foundRoute.getPath());
        assertEquals(RequestMethod.GET, foundRoute.getHttpMethod());
        assertEquals(method, foundRoute.getHandlerMethod());
    }
    
    @Test
    @DisplayName("测试参数化路由注册和查找")
    void testParameterizedRouteRegistration() throws Exception {
        // Given
        Method method = EnhancedTestController.class.getMethod("getUserById", Long.class);
        ParameterizedRouteInfo routeInfo = new ParameterizedRouteInfo("/api/users/{id}", RequestMethod.GET,
                                                                     EnhancedTestController.class, method, testController);
        
        // When
        routeRegistry.registerRoute(routeInfo);
        var foundRouteOpt = routeRegistry.findRoute("/api/users/123", RequestMethod.GET);
        
        // Then
        assertTrue(foundRouteOpt.isPresent());
        RouteInfo foundRoute = foundRouteOpt.get();
        assertTrue(foundRoute instanceof ParameterizedRouteInfo);
        ParameterizedRouteInfo paramRoute = (ParameterizedRouteInfo) foundRoute;
        
        Map<String, String> pathVars = paramRoute.extractPathVariables("/api/users/123");
        assertEquals("123", pathVars.get("id"));
    }
    
    @Test
    @DisplayName("测试路由不存在的情况")
    void testRouteNotFound() {
        // When
        var foundRouteOpt = routeRegistry.findRoute("/nonexistent", RequestMethod.GET);
        
        // Then
        assertFalse(foundRouteOpt.isPresent());
    }
    
    @Test
    @DisplayName("测试不同HTTP方法的路由")
    void testDifferentHttpMethods() throws Exception {
        // Given
        Method getMethod = EnhancedTestController.class.getMethod("apiInfo");
        Method postMethod = EnhancedTestController.class.getMethod("createUser", 
                cloud.compan.servlet.model.User.class, jakarta.servlet.http.HttpServletResponse.class);
        
        RouteInfo getRoute = new RouteInfo("/api/test", RequestMethod.GET, 
                                         EnhancedTestController.class, getMethod, testController);
        RouteInfo postRoute = new RouteInfo("/api/test", RequestMethod.POST, 
                                          EnhancedTestController.class, postMethod, testController);
        
        // When
        routeRegistry.registerRoute(getRoute);
        routeRegistry.registerRoute(postRoute);
        
        // Then
        var foundGetRouteOpt = routeRegistry.findRoute("/api/test", RequestMethod.GET);
        var foundPostRouteOpt = routeRegistry.findRoute("/api/test", RequestMethod.POST);
        
        assertTrue(foundGetRouteOpt.isPresent());
        assertTrue(foundPostRouteOpt.isPresent());
        
        RouteInfo foundGetRoute = foundGetRouteOpt.get();
        RouteInfo foundPostRoute = foundPostRouteOpt.get();
        
        assertEquals(getMethod, foundGetRoute.getHandlerMethod());
        assertEquals(postMethod, foundPostRoute.getHandlerMethod());
    }
    
    @Test
    @DisplayName("测试获取所有路由")
    void testGetAllRoutes() throws Exception {
        // Given
        Method method1 = EnhancedTestController.class.getMethod("apiInfo");
        Method method2 = EnhancedTestController.class.getMethod("getUserById", Long.class);
        
        RouteInfo route1 = new RouteInfo("/api/route1", RequestMethod.GET, 
                                       EnhancedTestController.class, method1, testController);
        RouteInfo route2 = new RouteInfo("/api/route2", RequestMethod.POST, 
                                       EnhancedTestController.class, method1, testController);
        
        // When
        routeRegistry.registerRoute(route1);
        routeRegistry.registerRoute(route2);
        
        // Then
        var allRoutes = routeRegistry.getAllRoutes();
        assertTrue(allRoutes.size() >= 2);
        
        boolean foundRoute1 = allRoutes.stream()
                .anyMatch(route -> route.getPath().equals("/api/route1"));
        boolean foundRoute2 = allRoutes.stream()
                .anyMatch(route -> route.getPath().equals("/api/route2"));
        
        assertTrue(foundRoute1);
        assertTrue(foundRoute2);
    }
    
    @Test
    @DisplayName("测试路径变量提取")
    void testPathVariableExtraction() throws Exception {
        // Given
        Method method = EnhancedTestController.class.getMethod("getUserPost", Long.class, Long.class);
        ParameterizedRouteInfo routeInfo = new ParameterizedRouteInfo("/api/users/{userId}/posts/{postId}", 
                                                                     RequestMethod.GET, EnhancedTestController.class, method, testController);
        
        // When
        routeRegistry.registerRoute(routeInfo);
        var foundRouteOpt = routeRegistry.findRoute("/api/users/123/posts/456", RequestMethod.GET);
        
        // Then
        assertTrue(foundRouteOpt.isPresent());
        RouteInfo foundRoute = foundRouteOpt.get();
        assertTrue(foundRoute instanceof ParameterizedRouteInfo);
        
        ParameterizedRouteInfo paramRoute = (ParameterizedRouteInfo) foundRoute;
        Map<String, String> pathVars = paramRoute.extractPathVariables("/api/users/123/posts/456");
        
        assertEquals("123", pathVars.get("userId"));
        assertEquals("456", pathVars.get("postId"));
    }
} 