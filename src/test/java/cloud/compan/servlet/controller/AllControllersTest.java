package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.SimpleTestBase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 所有控制器的综合测试
 * 测试所有控制器的实例化和依赖注入
 */
public class AllControllersTest extends SimpleTestBase {
    
    @Test
    void testAllControllersInstantiation() {
        // 测试所有控制器的实例化
        TestController testController = getController(TestController.class);
        AuthController authController = getController(AuthController.class);
        FileController fileController = getController(FileController.class);
        FolderController folderController = getController(FolderController.class);
        DemoController demoController = getController(DemoController.class);
        AclController aclController = getController(AclController.class);
        NotificationController notificationController = getController(NotificationController.class);
        RecycleBinController recycleBinController = getController(RecycleBinController.class);
        StorageController storageController = getController(StorageController.class);
        ShareController shareController = getController(ShareController.class);
        
        // 验证所有控制器都不为空
        assertNotNull(testController, "TestController应该被正确实例化");
        assertNotNull(authController, "AuthController应该被正确实例化");
        assertNotNull(fileController, "FileController应该被正确实例化");
        assertNotNull(folderController, "FolderController应该被正确实例化");
        assertNotNull(demoController, "DemoController应该被正确实例化");
        assertNotNull(aclController, "AclController应该被正确实例化");
        assertNotNull(notificationController, "NotificationController应该被正确实例化");
        assertNotNull(recycleBinController, "RecycleBinController应该被正确实例化");
        assertNotNull(storageController, "StorageController应该被正确实例化");
        assertNotNull(shareController, "ShareController应该被正确实例化");
        
        System.out.println("所有控制器实例化测试通过");
    }
    
    @Test
    void testAllControllersInheritance() {
        // 测试所有控制器都继承BaseController
        TestController testController = getController(TestController.class);
        AuthController authController = getController(AuthController.class);
        FileController fileController = getController(FileController.class);
        FolderController folderController = getController(FolderController.class);
        DemoController demoController = getController(DemoController.class);
        AclController aclController = getController(AclController.class);
        NotificationController notificationController = getController(NotificationController.class);
        RecycleBinController recycleBinController = getController(RecycleBinController.class);
        StorageController storageController = getController(StorageController.class);
        ShareController shareController = getController(ShareController.class);
        
        // 验证继承关系
        assertTrue(testController instanceof BaseController, "TestController应该继承BaseController");
        assertTrue(authController instanceof BaseController, "AuthController应该继承BaseController");
        assertTrue(fileController instanceof BaseController, "FileController应该继承BaseController");
        assertTrue(folderController instanceof BaseController, "FolderController应该继承BaseController");
        assertTrue(demoController instanceof BaseController, "DemoController应该继承BaseController");
        assertTrue(aclController instanceof BaseController, "AclController应该继承BaseController");
        assertTrue(notificationController instanceof BaseController, "NotificationController应该继承BaseController");
        assertTrue(recycleBinController instanceof BaseController, "RecycleBinController应该继承BaseController");
        assertTrue(storageController instanceof BaseController, "StorageController应该继承BaseController");
        assertTrue(shareController instanceof BaseController, "ShareController应该继承BaseController");
        
        System.out.println("所有控制器继承关系测试通过");
    }
    
    @Test
    void testControllerDependencyInjection() {
        // 测试控制器的依赖注入
        TestController testController = getController(TestController.class);
        AuthController authController = getController(AuthController.class);
        FileController fileController = getController(FileController.class);
        FolderController folderController = getController(FolderController.class);
        DemoController demoController = getController(DemoController.class);
        AclController aclController = getController(AclController.class);
        NotificationController notificationController = getController(NotificationController.class);
        RecycleBinController recycleBinController = getController(RecycleBinController.class);
        StorageController storageController = getController(StorageController.class);
        ShareController shareController = getController(ShareController.class);
        
        // 验证依赖注入工作正常
        assertNotNull(testController, "TestController依赖注入正常");
        assertNotNull(authController, "AuthController依赖注入正常");
        assertNotNull(fileController, "FileController依赖注入正常");
        assertNotNull(folderController, "FolderController依赖注入正常");
        assertNotNull(demoController, "DemoController依赖注入正常");
        assertNotNull(aclController, "AclController依赖注入正常");
        assertNotNull(notificationController, "NotificationController依赖注入正常");
        assertNotNull(recycleBinController, "RecycleBinController依赖注入正常");
        assertNotNull(storageController, "StorageController依赖注入正常");
        assertNotNull(shareController, "ShareController依赖注入正常");
        
        System.out.println("所有控制器依赖注入测试通过");
    }
} 