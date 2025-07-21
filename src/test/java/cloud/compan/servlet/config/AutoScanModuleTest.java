package cloud.compan.servlet.config;

import static org.junit.jupiter.api.Assertions.*;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import cloud.compan.servlet.controller.AclController;
import cloud.compan.servlet.controller.AuthController;
import cloud.compan.servlet.controller.FileController;
import cloud.compan.servlet.controller.FolderController;
import cloud.compan.servlet.controller.NotificationController;
import cloud.compan.servlet.controller.RecycleBinController;
import cloud.compan.servlet.controller.ShareController;
import cloud.compan.servlet.controller.StorageController;
import cloud.compan.servlet.repository.AclRepository;
import cloud.compan.servlet.repository.FileRepository;
import cloud.compan.servlet.repository.FolderRepository;
import cloud.compan.servlet.repository.LogRepository;
import cloud.compan.servlet.repository.NotificationRepository;
import cloud.compan.servlet.repository.ShareRepository;
import cloud.compan.servlet.repository.StorageObjectRepository;
import cloud.compan.servlet.repository.UserGroupMemberRepository;
import cloud.compan.servlet.repository.UserGroupRepository;
import cloud.compan.servlet.repository.UserRepository;
import cloud.compan.servlet.service.AclService;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.service.FileService;
import cloud.compan.servlet.service.FolderService;
import cloud.compan.servlet.service.NotificationService;
import cloud.compan.servlet.service.RecycleBinService;
import cloud.compan.servlet.service.ShareService;
import cloud.compan.servlet.service.StorageService;
import cloud.compan.servlet.service.UserService;

/**
 * AutoScanModule测试类
 * 验证自动扫描和注册功能
 */
@DisplayName("自动扫描模块测试")
class AutoScanModuleTest {
    
    @Test
    @DisplayName("测试Service接口自动绑定")
    void testServiceInterfaceAutoBinding() {
        // 创建包含AutoScanModule的Injector
        Injector injector = Guice.createInjector(new AutoScanModule("cloud.compan.servlet"));
        
        // 验证所有Service接口都可以被注入
        assertDoesNotThrow(() -> {
            UserService userService = injector.getInstance(UserService.class);
            assertNotNull(userService, "UserService应该被正确注入");
            assertEquals("UserServiceImpl", userService.getClass().getSimpleName(), "应该注入正确的实现类");
        });
        
        assertDoesNotThrow(() -> {
            AuthService authService = injector.getInstance(AuthService.class);
            assertNotNull(authService, "AuthService应该被正确注入");
            assertEquals("AuthServiceImpl", authService.getClass().getSimpleName(), "应该注入正确的实现类");
        });
        
        assertDoesNotThrow(() -> {
            FileService fileService = injector.getInstance(FileService.class);
            assertNotNull(fileService, "FileService应该被正确注入");
            assertEquals("FileServiceImpl", fileService.getClass().getSimpleName(), "应该注入正确的实现类");
        });
        
        assertDoesNotThrow(() -> {
            StorageService storageService = injector.getInstance(StorageService.class);
            assertNotNull(storageService, "StorageService应该被正确注入");
            assertEquals("StorageServiceImpl", storageService.getClass().getSimpleName(), "应该注入正确的实现类");
        });
        
        assertDoesNotThrow(() -> {
            FolderService folderService = injector.getInstance(FolderService.class);
            assertNotNull(folderService, "FolderService应该被正确注入");
            assertEquals("FolderServiceImpl", folderService.getClass().getSimpleName(), "应该注入正确的实现类");
        });
        
        assertDoesNotThrow(() -> {
            ShareService shareService = injector.getInstance(ShareService.class);
            assertNotNull(shareService, "ShareService应该被正确注入");
            assertEquals("ShareServiceImpl", shareService.getClass().getSimpleName(), "应该注入正确的实现类");
        });
        
        assertDoesNotThrow(() -> {
            NotificationService notificationService = injector.getInstance(NotificationService.class);
            assertNotNull(notificationService, "NotificationService应该被正确注入");
            assertEquals("NotificationServiceImpl", notificationService.getClass().getSimpleName(), "应该注入正确的实现类");
        });
        
        assertDoesNotThrow(() -> {
            RecycleBinService recycleBinService = injector.getInstance(RecycleBinService.class);
            assertNotNull(recycleBinService, "RecycleBinService应该被正确注入");
            assertEquals("RecycleBinServiceImpl", recycleBinService.getClass().getSimpleName(), "应该注入正确的实现类");
        });
        
        assertDoesNotThrow(() -> {
            AclService aclService = injector.getInstance(AclService.class);
            assertNotNull(aclService, "AclService应该被正确注入");
            assertEquals("AclServiceImpl", aclService.getClass().getSimpleName(), "应该注入正确的实现类");
        });
    }
    
    @Test
    @DisplayName("测试Controller自动注册")
    void testControllerAutoRegistration() {
        // 创建包含AutoScanModule的Injector
        Injector injector = Guice.createInjector(new AutoScanModule("cloud.compan.servlet"));
        
        // 验证Controller类可以被注入
        assertDoesNotThrow(() -> {
            AuthController authController = injector.getInstance(AuthController.class);
            assertNotNull(authController, "AuthController应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            FileController fileController = injector.getInstance(FileController.class);
            assertNotNull(fileController, "FileController应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            FolderController folderController = injector.getInstance(FolderController.class);
            assertNotNull(folderController, "FolderController应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            StorageController storageController = injector.getInstance(StorageController.class);
            assertNotNull(storageController, "StorageController应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            ShareController shareController = injector.getInstance(ShareController.class);
            assertNotNull(shareController, "ShareController应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            AclController aclController = injector.getInstance(AclController.class);
            assertNotNull(aclController, "AclController应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            NotificationController notificationController = injector.getInstance(NotificationController.class);
            assertNotNull(notificationController, "NotificationController应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            RecycleBinController recycleBinController = injector.getInstance(RecycleBinController.class);
            assertNotNull(recycleBinController, "RecycleBinController应该被正确注入");
        });
    }
    
    @Test
    @DisplayName("测试Repository自动注册")
    void testRepositoryAutoRegistration() {
        // 创建包含AutoScanModule的Injector
        Injector injector = Guice.createInjector(new AutoScanModule("cloud.compan.servlet"));
        
        // 验证Repository类可以被注入
        assertDoesNotThrow(() -> {
            UserRepository userRepository = injector.getInstance(UserRepository.class);
            assertNotNull(userRepository, "UserRepository应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            FileRepository fileRepository = injector.getInstance(FileRepository.class);
            assertNotNull(fileRepository, "FileRepository应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            FolderRepository folderRepository = injector.getInstance(FolderRepository.class);
            assertNotNull(folderRepository, "FolderRepository应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            StorageObjectRepository storageObjectRepository = injector.getInstance(StorageObjectRepository.class);
            assertNotNull(storageObjectRepository, "StorageObjectRepository应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            AclRepository aclRepository = injector.getInstance(AclRepository.class);
            assertNotNull(aclRepository, "AclRepository应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            ShareRepository shareRepository = injector.getInstance(ShareRepository.class);
            assertNotNull(shareRepository, "ShareRepository应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            LogRepository logRepository = injector.getInstance(LogRepository.class);
            assertNotNull(logRepository, "LogRepository应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            NotificationRepository notificationRepository = injector.getInstance(NotificationRepository.class);
            assertNotNull(notificationRepository, "NotificationRepository应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            UserGroupRepository userGroupRepository = injector.getInstance(UserGroupRepository.class);
            assertNotNull(userGroupRepository, "UserGroupRepository应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            UserGroupMemberRepository userGroupMemberRepository = injector.getInstance(UserGroupMemberRepository.class);
            assertNotNull(userGroupMemberRepository, "UserGroupMemberRepository应该被正确注入");
        });
    }
    
    @Test
    @DisplayName("测试Service实现类直接注入")
    void testServiceImplementationDirectInjection() {
        // 创建包含AutoScanModule的Injector
        Injector injector = Guice.createInjector(new AutoScanModule("cloud.compan.servlet"));
        
        // 验证Service实现类可以直接注入
        assertDoesNotThrow(() -> {
            cloud.compan.servlet.service.impl.UserServiceImpl userServiceImpl = 
                injector.getInstance(cloud.compan.servlet.service.impl.UserServiceImpl.class);
            assertNotNull(userServiceImpl, "UserServiceImpl应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            cloud.compan.servlet.service.impl.AuthServiceImpl authServiceImpl = 
                injector.getInstance(cloud.compan.servlet.service.impl.AuthServiceImpl.class);
            assertNotNull(authServiceImpl, "AuthServiceImpl应该被正确注入");
        });
    }
    
    @Test
    @DisplayName("测试单例模式")
    void testSingletonScope() {
        // 创建包含AutoScanModule的Injector
        Injector injector = Guice.createInjector(new AutoScanModule("cloud.compan.servlet"));
        
        // 验证单例模式
        UserService userService1 = injector.getInstance(UserService.class);
        UserService userService2 = injector.getInstance(UserService.class);
        
        assertSame(userService1, userService2, "Service应该是单例");
        
        AuthController authController1 = injector.getInstance(AuthController.class);
        AuthController authController2 = injector.getInstance(AuthController.class);
        
        assertSame(authController1, authController2, "Controller应该是单例");
        
        // 验证Repository单例模式
        UserRepository userRepository1 = injector.getInstance(UserRepository.class);
        UserRepository userRepository2 = injector.getInstance(UserRepository.class);
        
        assertSame(userRepository1, userRepository2, "Repository应该是单例");
    }
    
    @Test
    @DisplayName("测试完整的AppModule集成")
    void testAppModuleIntegration() {
        // 测试完整的AppModule（包含AutoScanModule）
        Injector injector = Guice.createInjector(new AppModule());
        
        // 验证Service接口绑定
        assertDoesNotThrow(() -> {
            UserService userService = injector.getInstance(UserService.class);
            assertNotNull(userService, "UserService应该被正确注入");
            assertEquals("UserServiceImpl", userService.getClass().getSimpleName(), "应该注入正确的实现类");
        });
        
        // 验证Controller注入
        assertDoesNotThrow(() -> {
            AuthController authController = injector.getInstance(AuthController.class);
            assertNotNull(authController, "AuthController应该被正确注入");
        });
        
        // 验证Repository注入
        assertDoesNotThrow(() -> {
            UserRepository userRepository = injector.getInstance(UserRepository.class);
            assertNotNull(userRepository, "UserRepository应该被正确注入");
        });
        
        assertDoesNotThrow(() -> {
            FileRepository fileRepository = injector.getInstance(FileRepository.class);
            assertNotNull(fileRepository, "FileRepository应该被正确注入");
        });
    }
} 