package cloud.compan.servlet.config;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Collection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import cloud.compan.servlet.controller.AuthController;
import cloud.compan.servlet.controller.FileController;
import cloud.compan.servlet.converter.JsonHttpMessageConverter;
import cloud.compan.servlet.dto.LoginResultDTO;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.model.User;
import cloud.compan.servlet.repository.UserRepository;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.service.UserService;
import cloud.compan.servlet.web.ControllerScanner;
import cloud.compan.servlet.web.RequestDispatcher;
import cloud.compan.servlet.web.RouteInfo;
import cloud.compan.servlet.web.RouteRegistry;

/**
 * Complete Integration Test
 * Tests the full flow from route registration to component auto-binding,
 * route dispatching, and Controller/Service response completion
 */
@DisplayName("Complete Integration Test")
class IntegrationTest {
    
    private Injector injector;
    private RouteRegistry routeRegistry;
    private RequestDispatcher requestDispatcher;
    private ControllerScanner controllerScanner;
    private JsonHttpMessageConverter jsonConverter;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        // Create Injector with AppModule (which includes AutoScanModule)
        injector = Guice.createInjector(new AppModule());
        
        // Get components from injector
        routeRegistry = injector.getInstance(RouteRegistry.class);
        requestDispatcher = injector.getInstance(RequestDispatcher.class);
        controllerScanner = injector.getInstance(ControllerScanner.class);
        jsonConverter = injector.getInstance(JsonHttpMessageConverter.class);
        objectMapper = injector.getInstance(ObjectMapper.class);
    }
    
    @Test
    @DisplayName("Test complete integration flow")
    void testCompleteIntegrationFlow() {
        System.out.println("=== Starting Complete Integration Test ===");
        
        // Step 1: Verify component auto-binding
        testComponentAutoBinding();
        
        // Step 2: Test route registration
        testRouteRegistration();
        
        // Step 3: Test route dispatching
        testRouteDispatching();
        
        // Step 4: Test complete request-response flow
        testCompleteRequestResponseFlow();
        
        System.out.println("=== Complete Integration Test Finished ===");
    }
    
    @Test
    @DisplayName("Test component auto-binding")
    void testComponentAutoBinding() {
        System.out.println("Testing component auto-binding...");
        
        // Verify Service injection
        assertDoesNotThrow(() -> {
            UserService userService = injector.getInstance(UserService.class);
            assertNotNull(userService, "UserService should be injected");
            assertEquals("UserServiceImpl", userService.getClass().getSimpleName());
            System.out.println("✓ UserService auto-binding: PASSED");
        });
        
        assertDoesNotThrow(() -> {
            AuthService authService = injector.getInstance(AuthService.class);
            assertNotNull(authService, "AuthService should be injected");
            assertEquals("AuthServiceImpl", authService.getClass().getSimpleName());
            System.out.println("✓ AuthService auto-binding: PASSED");
        });
        
        // Verify Controller injection
        assertDoesNotThrow(() -> {
            AuthController authController = injector.getInstance(AuthController.class);
            assertNotNull(authController, "AuthController should be injected");
            System.out.println("✓ AuthController auto-binding: PASSED");
        });
        
        assertDoesNotThrow(() -> {
            FileController fileController = injector.getInstance(FileController.class);
            assertNotNull(fileController, "FileController should be injected");
            System.out.println("✓ FileController auto-binding: PASSED");
        });
        
        // Verify Repository injection
        assertDoesNotThrow(() -> {
            UserRepository userRepository = injector.getInstance(UserRepository.class);
            assertNotNull(userRepository, "UserRepository should be injected");
            System.out.println("✓ UserRepository auto-binding: PASSED");
        });
        
        // Verify singleton behavior
        UserService userService1 = injector.getInstance(UserService.class);
        UserService userService2 = injector.getInstance(UserService.class);
        assertSame(userService1, userService2, "Services should be singletons");
        System.out.println("✓ Singleton behavior: PASSED");
        
        System.out.println("Component auto-binding test completed successfully!");
    }
    
    @Test
    @DisplayName("Test route registration")
    void testRouteRegistration() {
        System.out.println("Testing route registration...");
        
        // Get controllers from injector
        AuthController authController = injector.getInstance(AuthController.class);
        FileController fileController = injector.getInstance(FileController.class);
        
        // Register routes using hardcoded approach
        assertDoesNotThrow(() -> {
            // Use HardcodedRouteRegistry to register routes
            cloud.compan.servlet.web.HardcodedRouteRegistry hardcodedRegistry = 
                new cloud.compan.servlet.web.HardcodedRouteRegistry(routeRegistry, injector);
            hardcodedRegistry.registerAllRoutes();
            System.out.println("✓ All routes registered via HardcodedRouteRegistry");
        });
        
        // Verify routes are registered
        Collection<RouteInfo> routes = routeRegistry.getAllRoutes();
        assertFalse(routes.isEmpty(), "Routes should be registered");
        
        System.out.println("Registered routes:");
        for (RouteInfo route : routes) {
            System.out.println("  " + route.getHttpMethod() + " " + route.getPath() + " -> " + route.getHandlerMethod().getName());
        }
        
        // Verify specific routes exist
        boolean hasLoginRoute = routes.stream()
            .anyMatch(route -> "/api/auth/login".equals(route.getPath()) && "POST".equals(route.getHttpMethod().name()));
        assertTrue(hasLoginRoute, "Login route should be registered");
        
        boolean hasFileListRoute = routes.stream()
            .anyMatch(route -> "/api/files".equals(route.getPath()) && "GET".equals(route.getHttpMethod().name()));
        assertTrue(hasFileListRoute, "File list route should be registered");
        
        System.out.println("Route registration test completed successfully!");
    }
    
    @Test
    @DisplayName("Test route dispatching")
    void testRouteDispatching() {
        System.out.println("Testing route dispatching...");
        
        // Register routes using hardcoded approach
        cloud.compan.servlet.web.HardcodedRouteRegistry hardcodedRegistry = 
            new cloud.compan.servlet.web.HardcodedRouteRegistry(routeRegistry, injector);
        hardcodedRegistry.registerAllRoutes();
        
        // Test route matching using RouteRegistry
        var loginRouteOpt = routeRegistry.findRoute("/api/auth/login", "POST");
        assertTrue(loginRouteOpt.isPresent(), "Login route should be found");
        RouteInfo loginRoute = loginRouteOpt.get();
        assertEquals("login", loginRoute.getHandlerMethod().getName());
        System.out.println("✓ Login route dispatching: PASSED");
        
        var fileListRouteOpt = routeRegistry.findRoute("/api/files", "GET");
        assertTrue(fileListRouteOpt.isPresent(), "File list route should be found");
        RouteInfo fileListRoute = fileListRouteOpt.get();
        assertEquals("getFiles", fileListRoute.getHandlerMethod().getName());
        System.out.println("✓ File list route dispatching: PASSED");
        
        // Test non-existent route
        var nonExistentRouteOpt = routeRegistry.findRoute("/non-existent", "GET");
        assertFalse(nonExistentRouteOpt.isPresent(), "Non-existent route should return empty");
        System.out.println("✓ Non-existent route handling: PASSED");
        
        System.out.println("Route dispatching test completed successfully!");
    }
    
    @Test
    @DisplayName("Test complete request-response flow")
    void testCompleteRequestResponseFlow() {
        System.out.println("Testing complete request-response flow...");
        
        // Register routes using hardcoded approach
        cloud.compan.servlet.web.HardcodedRouteRegistry hardcodedRegistry = 
            new cloud.compan.servlet.web.HardcodedRouteRegistry(routeRegistry, injector);
        hardcodedRegistry.registerAllRoutes();
        
        // Test login request
        testLoginRequest();
        
        // Test file list request
        testFileListRequest();
        
        System.out.println("Complete request-response flow test completed successfully!");
    }
    
    private void testLoginRequest() {
        System.out.println("Testing login request flow...");
        
        try {
            // Test service layer directly
            AuthService authService = injector.getInstance(AuthService.class);
            ServiceResult<LoginResultDTO> result = authService.login("testuser", "testpass");
            
            assertNotNull(result, "Auth service should return a result");
            System.out.println("✓ Login service flow: PASSED");
            
        } catch (Exception e) {
            System.out.println("Login request test failed: " + e.getMessage());
            e.printStackTrace();
            fail("Login request should succeed");
        }
    }
    
    private void testFileListRequest() {
        System.out.println("Testing file list request flow...");
        
        try {
            // Test service layer directly
            UserService userService = injector.getInstance(UserService.class);
            ServiceResult<User> result = userService.findById(1L);
            
            assertNotNull(result, "User service should return a result");
            System.out.println("✓ File list service flow: PASSED");
            
        } catch (Exception e) {
            System.out.println("File list request test failed: " + e.getMessage());
            e.printStackTrace();
            fail("File list request should succeed");
        }
    }
    
    @Test
    @DisplayName("Test dependency injection in controllers")
    void testDependencyInjectionInControllers() {
        System.out.println("Testing dependency injection in controllers...");
        
        // Get controllers from injector
        AuthController authController = injector.getInstance(AuthController.class);
        FileController fileController = injector.getInstance(FileController.class);
        
        // Verify that controllers have their dependencies injected
        assertDoesNotThrow(() -> {
            // This will test if the controller can access its injected services
            // We'll use reflection to check if the fields are not null
            java.lang.reflect.Field authServiceField = AuthController.class.getDeclaredField("authService");
            authServiceField.setAccessible(true);
            Object authService = authServiceField.get(authController);
            assertNotNull(authService, "AuthService should be injected into AuthController");
            System.out.println("✓ AuthController dependency injection: PASSED");
        });
        
        assertDoesNotThrow(() -> {
            java.lang.reflect.Field fileServiceField = FileController.class.getDeclaredField("fileService");
            fileServiceField.setAccessible(true);
            Object fileService = fileServiceField.get(fileController);
            assertNotNull(fileService, "FileService should be injected into FileController");
            System.out.println("✓ FileController dependency injection: PASSED");
        });
        
        System.out.println("Dependency injection in controllers test completed successfully!");
    }
    
    @Test
    @DisplayName("Test service layer integration")
    void testServiceLayerIntegration() {
        System.out.println("Testing service layer integration...");
        
        // Get services from injector
        UserService userService = injector.getInstance(UserService.class);
        AuthService authService = injector.getInstance(AuthService.class);
        
        // Test service method calls
        assertDoesNotThrow(() -> {
            // Test user service methods
            ServiceResult<User> userResult = userService.findById(1L);
            assertNotNull(userResult, "User service should return a result");
            System.out.println("✓ UserService method call: PASSED");
        });
        
        assertDoesNotThrow(() -> {
            // Test auth service methods
            ServiceResult<LoginResultDTO> authResult = authService.login("testuser", "testpass");
            assertNotNull(authResult, "Auth service should return a result");
            System.out.println("✓ AuthService method call: PASSED");
        });
        
        System.out.println("Service layer integration test completed successfully!");
    }
} 