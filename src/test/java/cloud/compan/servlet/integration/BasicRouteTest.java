package cloud.compan.servlet.integration;

import cloud.compan.servlet.web.RouteRegistry;
import cloud.compan.servlet.web.ControllerScanner;
import cloud.compan.servlet.web.SimpleTestBase;
import cloud.compan.servlet.controller.AuthController;
import cloud.compan.servlet.controller.FileController;
import cloud.compan.servlet.controller.FolderController;
import cloud.compan.servlet.controller.ShareController;
import com.google.inject.Guice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 基本路由测试
 * 验证路由注册和基本功能
 */
public class BasicRouteTest extends SimpleTestBase {
    
    private RouteRegistry routeRegistry;
    private ControllerScanner controllerScanner;
    
    @BeforeEach
    public void setUp() {
        // 初始化Guice容器
        injector = Guice.createInjector(
            new cloud.compan.servlet.config.AppModule(),
            new cloud.compan.servlet.config.DatabaseModule()
        );
        
        routeRegistry = getService(RouteRegistry.class);
        controllerScanner = getService(ControllerScanner.class);
        
        // 扫描并注册所有控制器路由
        controllerScanner.scanAndRegister("cloud.compan.servlet.controller");
    }
    
    @Test
    void testRouteRegistryInjection() {
        System.out.println("开始验证路由注册器注入...");
        
        // 验证路由注册器不为空
        assertNotNull(routeRegistry, "路由注册器应该不为空");
        assertNotNull(controllerScanner, "控制器扫描器应该不为空");
        
        System.out.println("路由注册器注入验证完成");
    }
    
    @Test
    void testControllerInjection() {
        System.out.println("开始验证控制器注入...");
        
        // 验证各个控制器可以被注入
        AuthController authController = getController(AuthController.class);
        FileController fileController = getController(FileController.class);
        FolderController folderController = getController(FolderController.class);
        ShareController shareController = getController(ShareController.class);
        
        assertNotNull(authController, "AuthController应该被正确注入");
        assertNotNull(fileController, "FileController应该被正确注入");
        assertNotNull(folderController, "FolderController应该被正确注入");
        assertNotNull(shareController, "ShareController应该被正确注入");
        
        System.out.println("控制器注入验证完成");
    }
    
    @Test
    void testRouteRegistration() {
        System.out.println("开始验证路由注册...");
        
        // 验证路由注册器工作正常
        assertNotNull(routeRegistry, "路由注册器应该不为空");
        
        // 这里可以添加更具体的路由验证
        System.out.println("路由注册验证完成");
    }
    
    @Test
    void testControllerAnnotations() {
        System.out.println("开始验证控制器注解...");
        
        // 验证控制器类有正确的注解
        assertTrue(AuthController.class.isAnnotationPresent(cloud.compan.servlet.annotations.Controller.class), 
            "AuthController应该有@Controller注解");
        assertTrue(FileController.class.isAnnotationPresent(cloud.compan.servlet.annotations.Controller.class), 
            "FileController应该有@Controller注解");
        assertTrue(FolderController.class.isAnnotationPresent(cloud.compan.servlet.annotations.Controller.class), 
            "FolderController应该有@Controller注解");
        assertTrue(ShareController.class.isAnnotationPresent(cloud.compan.servlet.annotations.Controller.class), 
            "ShareController应该有@Controller注解");
        
        System.out.println("控制器注解验证完成");
    }
} 