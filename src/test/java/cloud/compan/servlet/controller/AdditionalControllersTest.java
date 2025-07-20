package cloud.compan.servlet.controller;

import cloud.compan.servlet.web.SimpleTestBase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 新增控制器的综合测试
 * 测试AclController、NotificationController、RecycleBinController、StorageController
 */
public class AdditionalControllersTest extends SimpleTestBase {
    
    @Test
    void testAllAdditionalControllersInstantiation() {
        // 测试所有新增控制器的实例化
        AclController aclController = getController(AclController.class);
        NotificationController notificationController = getController(NotificationController.class);
        RecycleBinController recycleBinController = getController(RecycleBinController.class);
        StorageController storageController = getController(StorageController.class);
        
        // 验证所有控制器都不为空
        assertNotNull(aclController, "AclController应该被正确实例化");
        assertNotNull(notificationController, "NotificationController应该被正确实例化");
        assertNotNull(recycleBinController, "RecycleBinController应该被正确实例化");
        assertNotNull(storageController, "StorageController应该被正确实例化");
        
        System.out.println("所有新增控制器实例化测试通过");
    }
    
    @Test
    void testAllAdditionalControllersInheritance() {
        // 测试所有新增控制器都继承BaseController
        AclController aclController = getController(AclController.class);
        NotificationController notificationController = getController(NotificationController.class);
        RecycleBinController recycleBinController = getController(RecycleBinController.class);
        StorageController storageController = getController(StorageController.class);
        
        // 验证继承关系
        assertTrue(aclController instanceof BaseController, "AclController应该继承BaseController");
        assertTrue(notificationController instanceof BaseController, "NotificationController应该继承BaseController");
        assertTrue(recycleBinController instanceof BaseController, "RecycleBinController应该继承BaseController");
        assertTrue(storageController instanceof BaseController, "StorageController应该继承BaseController");
        
        System.out.println("所有新增控制器继承关系测试通过");
    }
    
    @Test
    void testAdditionalControllersDependencyInjection() {
        // 测试新增控制器的依赖注入
        AclController aclController = getController(AclController.class);
        NotificationController notificationController = getController(NotificationController.class);
        RecycleBinController recycleBinController = getController(RecycleBinController.class);
        StorageController storageController = getController(StorageController.class);
        
        // 验证依赖注入工作正常
        assertNotNull(aclController, "AclController依赖注入正常");
        assertNotNull(notificationController, "NotificationController依赖注入正常");
        assertNotNull(recycleBinController, "RecycleBinController依赖注入正常");
        assertNotNull(storageController, "StorageController依赖注入正常");
        
        System.out.println("所有新增控制器依赖注入测试通过");
    }
    
    @Test
    void testAdditionalControllersAnnotations() {
        // 测试所有新增控制器的注解
        assertTrue(AclController.class.isAnnotationPresent(cloud.compan.servlet.annotations.Controller.class), 
            "AclController应该有@Controller注解");
        assertTrue(NotificationController.class.isAnnotationPresent(cloud.compan.servlet.annotations.Controller.class), 
            "NotificationController应该有@Controller注解");
        assertTrue(RecycleBinController.class.isAnnotationPresent(cloud.compan.servlet.annotations.Controller.class), 
            "RecycleBinController应该有@Controller注解");
        assertTrue(StorageController.class.isAnnotationPresent(cloud.compan.servlet.annotations.Controller.class), 
            "StorageController应该有@Controller注解");
        
        assertTrue(AclController.class.isAnnotationPresent(cloud.compan.servlet.annotations.ResponseBody.class), 
            "AclController应该有@ResponseBody注解");
        assertTrue(NotificationController.class.isAnnotationPresent(cloud.compan.servlet.annotations.ResponseBody.class), 
            "NotificationController应该有@ResponseBody注解");
        assertTrue(RecycleBinController.class.isAnnotationPresent(cloud.compan.servlet.annotations.ResponseBody.class), 
            "RecycleBinController应该有@ResponseBody注解");
        assertTrue(StorageController.class.isAnnotationPresent(cloud.compan.servlet.annotations.ResponseBody.class), 
            "StorageController应该有@ResponseBody注解");
        
        System.out.println("所有新增控制器注解测试通过");
    }
} 