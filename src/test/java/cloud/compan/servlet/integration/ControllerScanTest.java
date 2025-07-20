package cloud.compan.servlet.integration;

import cloud.compan.servlet.web.SimpleTestBase;
import cloud.compan.servlet.controller.AuthController;
import cloud.compan.servlet.controller.FileController;
import cloud.compan.servlet.controller.FolderController;
import cloud.compan.servlet.controller.ShareController;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 控制器扫描测试
 * 验证控制器扫描和注解识别
 */
public class ControllerScanTest extends SimpleTestBase {
    
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
    
    @Test
    void testControllerInstantiation() {
        System.out.println("开始验证控制器实例化...");
        
        // 验证控制器可以被实例化
        AuthController authController = getController(AuthController.class);
        FileController fileController = getController(FileController.class);
        FolderController folderController = getController(FolderController.class);
        ShareController shareController = getController(ShareController.class);
        
        assertNotNull(authController, "AuthController应该被正确实例化");
        assertNotNull(fileController, "FileController应该被正确实例化");
        assertNotNull(folderController, "FolderController应该被正确实例化");
        assertNotNull(shareController, "ShareController应该被正确实例化");
        
        System.out.println("控制器实例化验证完成");
    }
    
    @Test
    void testControllerMethods() {
        System.out.println("开始验证控制器方法...");
        
        // 验证控制器有正确的方法注解
        boolean hasAuthMethods = false;
        boolean hasFileMethods = false;
        boolean hasFolderMethods = false;
        boolean hasShareMethods = false;
        
        for (java.lang.reflect.Method method : AuthController.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(cloud.compan.servlet.annotations.PostMapping.class) ||
                method.isAnnotationPresent(cloud.compan.servlet.annotations.GetMapping.class) ||
                method.isAnnotationPresent(cloud.compan.servlet.annotations.PutMapping.class) ||
                method.isAnnotationPresent(cloud.compan.servlet.annotations.DeleteMapping.class)) {
                hasAuthMethods = true;
                break;
            }
        }
        
        for (java.lang.reflect.Method method : FileController.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(cloud.compan.servlet.annotations.PostMapping.class) ||
                method.isAnnotationPresent(cloud.compan.servlet.annotations.GetMapping.class) ||
                method.isAnnotationPresent(cloud.compan.servlet.annotations.PutMapping.class) ||
                method.isAnnotationPresent(cloud.compan.servlet.annotations.DeleteMapping.class)) {
                hasFileMethods = true;
                break;
            }
        }
        
        for (java.lang.reflect.Method method : FolderController.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(cloud.compan.servlet.annotations.PostMapping.class) ||
                method.isAnnotationPresent(cloud.compan.servlet.annotations.GetMapping.class) ||
                method.isAnnotationPresent(cloud.compan.servlet.annotations.PutMapping.class) ||
                method.isAnnotationPresent(cloud.compan.servlet.annotations.DeleteMapping.class)) {
                hasFolderMethods = true;
                break;
            }
        }
        
        for (java.lang.reflect.Method method : ShareController.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(cloud.compan.servlet.annotations.PostMapping.class) ||
                method.isAnnotationPresent(cloud.compan.servlet.annotations.GetMapping.class) ||
                method.isAnnotationPresent(cloud.compan.servlet.annotations.PutMapping.class) ||
                method.isAnnotationPresent(cloud.compan.servlet.annotations.DeleteMapping.class)) {
                hasShareMethods = true;
                break;
            }
        }
        
        assertTrue(hasAuthMethods, "AuthController应该有HTTP映射方法");
        assertTrue(hasFileMethods, "FileController应该有HTTP映射方法");
        assertTrue(hasFolderMethods, "FolderController应该有HTTP映射方法");
        assertTrue(hasShareMethods, "ShareController应该有HTTP映射方法");
        
        System.out.println("控制器方法验证完成");
    }
} 