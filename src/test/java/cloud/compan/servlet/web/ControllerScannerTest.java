package cloud.compan.servlet.web;

import cloud.compan.servlet.config.AppModule;
import cloud.compan.servlet.config.DatabaseModule;
import cloud.compan.servlet.controller.TestController;
import cloud.compan.servlet.annotations.enums.RequestMethod;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ControllerScanner测试类
 */
public class ControllerScannerTest {
    
    private Injector injector;
    private ControllerScanner controllerScanner;
    private RouteRegistry routeRegistry;
    
    @BeforeEach
    void setUp() {
        // 初始化Guice容器
        injector = Guice.createInjector(
            new AppModule(),
            new DatabaseModule()
        );
        
        // 获取组件
        routeRegistry = injector.getInstance(RouteRegistry.class);
        controllerScanner = injector.getInstance(ControllerScanner.class);
    }
    
    @Test
    void testScanAndRegisterControllers() {
        // 扫描控制器
        controllerScanner.scanAndRegister("cloud.compan.servlet.controller");
        
        // 打印所有注册的路由
        System.out.println("注册的路由数量: " + routeRegistry.getRouteCount());
        routeRegistry.printAllRoutes();
        
        // 验证TestController被注册
        assertTrue(routeRegistry.hasRoute("/api/test/test", RequestMethod.PUT), 
                  "TestController的路由应该被注册");
    }
    
    @Test
    void testControllerInstanceCreation() {
        // 测试能否创建TestController实例
        TestController controller = injector.getInstance(TestController.class);
        assertNotNull(controller, "应该能够创建TestController实例");
    }
} 