package cloud.compan.servlet.integration;

import java.lang.reflect.Method;
import com.google.inject.Injector;
import cloud.compan.servlet.annotations.enums.RequestMethod;
import cloud.compan.servlet.controller.AclController;
import cloud.compan.servlet.controller.AuthController;
import cloud.compan.servlet.controller.FileController;
import cloud.compan.servlet.controller.FolderController;
import cloud.compan.servlet.controller.NotificationController;
import cloud.compan.servlet.controller.RecycleBinController;
import cloud.compan.servlet.controller.ShareController;
import cloud.compan.servlet.controller.StorageController;
import cloud.compan.servlet.web.RouteInfo;
import cloud.compan.servlet.web.RouteRegistry;

/**
 * 硬编码路由注册器
 * 手动注册所有路由，用于测试
 */
public class HardcodedRouteRegistry {
    
    private final RouteRegistry routeRegistry;
    private final Injector injector;
    
    public HardcodedRouteRegistry(RouteRegistry routeRegistry, Injector injector) {
        this.routeRegistry = routeRegistry;
        this.injector = injector;
    }
    
    /**
     * Register all routes
     */
    public void registerAllRoutes() {
        System.out.println("Starting hardcoded registration of all routes...");
        
        try {
            // Register authentication related routes
            registerAuthRoutes();
            
            // Register file management routes
            registerFileRoutes();
            
            // Register folder management routes
            registerFolderRoutes();
            
            // Register share management routes
            registerShareRoutes();
            
            // Register storage management routes
            registerStorageRoutes();
            
            // Register notification management routes
            registerNotificationRoutes();
            
            // Register recycle bin routes
            registerRecycleBinRoutes();
            
            // Register permission control routes
            registerAclRoutes();
            
            System.out.println("All routes hardcoded registration completed");
            
        } catch (Exception e) {
            System.err.println("Route registration failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Route registration failed", e);
        }
    }
    
    /**
     * 注册认证相关路由
     */
    private void registerAuthRoutes() {
        AuthController authController = injector.getInstance(AuthController.class);
        
        // POST /api/auth/register
        registerRoute(RequestMethod.POST, "/api/auth/register", authController, "register");
        
        // POST /api/auth/login
        registerRoute(RequestMethod.POST, "/api/auth/login", authController, "login");
        
        // POST /api/auth/logout
        registerRoute(RequestMethod.POST, "/api/auth/logout", authController, "logout");
        
        // POST /api/auth/refresh
        registerRoute(RequestMethod.POST, "/api/auth/refresh", authController, "refreshToken");
        
        // GET /api/me/profile
        registerRoute(RequestMethod.GET, "/api/me/profile", authController, "getProfile");
        
        // PUT /api/me/update-profile
        registerRoute(RequestMethod.PUT, "/api/me/update-profile", authController, "updateProfile");
        
        // PUT /api/me/password
        registerRoute(RequestMethod.PUT, "/api/me/password", authController, "changePassword");
        
        // GET /api/me/storage-stats
        registerRoute(RequestMethod.GET, "/api/me/storage-stats", authController, "getStorageStats");
        
        // GET /api/me/activity-log
        registerRoute(RequestMethod.GET, "/api/me/activity-log", authController, "getActivityLog");
    }
    
    /**
     * 注册文件管理路由
     */
    private void registerFileRoutes() {
        FileController fileController = injector.getInstance(FileController.class);
        
        // POST /api/files/upload
        registerRoute(RequestMethod.POST, "/api/files/upload", fileController, "uploadFile");
        
        // GET /api/files
        registerRoute(RequestMethod.GET, "/api/files", fileController, "getFiles");
        
        // GET /api/files/{id}
        registerRoute(RequestMethod.GET, "/api/files/{id}", fileController, "getFileDetails");
        
        // PUT /api/files/{id}/rename
        registerRoute(RequestMethod.PUT, "/api/files/{id}/rename", fileController, "renameFile");
        
        // DELETE /api/files/{id}
        registerRoute(RequestMethod.DELETE, "/api/files/{id}", fileController, "deleteFile");
        
        // GET /api/files/{id}/download
        registerRoute(RequestMethod.GET, "/api/files/{id}/download", fileController, "downloadFile");
        
        // GET /api/files/search
        registerRoute(RequestMethod.GET, "/api/files/search", fileController, "searchFiles");
    }
    
    /**
     * 注册文件夹管理路由
     */
    private void registerFolderRoutes() {
        FolderController folderController = injector.getInstance(FolderController.class);
        
        // POST /api/folders
        registerRoute(RequestMethod.POST, "/api/folders", folderController, "createFolder");
        
        // GET /api/folders
        registerRoute(RequestMethod.GET, "/api/folders", folderController, "getFolders");
        
        // GET /api/folders/{id}
        registerRoute(RequestMethod.GET, "/api/folders/{id}", folderController, "getFolderDetails");
        
        // PUT /api/folders/{id}/rename
        registerRoute(RequestMethod.PUT, "/api/folders/{id}/rename", folderController, "renameFolder");
        
        // DELETE /api/folders/{id}
        registerRoute(RequestMethod.DELETE, "/api/folders/{id}", folderController, "deleteFolder");
        
        // GET /api/folders/{id}/tree
        registerRoute(RequestMethod.GET, "/api/folders/{id}/tree", folderController, "getFolderTree");
        
        // PUT /api/folders/{id}/move
        registerRoute(RequestMethod.PUT, "/api/folders/{id}/move", folderController, "moveFolder");
        
        // GET /api/folders/search
        registerRoute(RequestMethod.GET, "/api/folders/search", folderController, "searchFolders");
    }
    
    /**
     * 注册分享管理路由
     */
    private void registerShareRoutes() {
        ShareController shareController = injector.getInstance(ShareController.class);
        
        // POST /api/shares
        registerRoute(RequestMethod.POST, "/api/shares", shareController, "createShare");
        
        // GET /api/shares
        registerRoute(RequestMethod.GET, "/api/shares", shareController, "getUserShares");
        
        // GET /api/shares/{id}
        registerRoute(RequestMethod.GET, "/api/shares/{id}", shareController, "getShareDetails");
        
        // PATCH /api/shares/{id}
        registerRoute(RequestMethod.PATCH, "/api/shares/{id}", shareController, "updateShareSettings");
        
        // DELETE /api/shares/{id}
        registerRoute(RequestMethod.DELETE, "/api/shares/{id}", shareController, "deleteShare");
        
        // GET /api/shares/{id}/statistics
        registerRoute(RequestMethod.GET, "/api/shares/{id}/statistics", shareController, "getShareStatistics");
        
        // POST /api/shares/batch
        registerRoute(RequestMethod.POST, "/api/shares/batch", shareController, "createBatchShares");
        
        // 注册ShareAccessController的路由
        // 注意：ShareAccessController是一个内部类，需要特殊处理
        try {
            // 获取ShareAccessController类
            Class<?> shareAccessControllerClass = null;
            for (Class<?> innerClass : shareController.getClass().getDeclaredClasses()) {
                if ("ShareAccessController".equals(innerClass.getSimpleName())) {
                    shareAccessControllerClass = innerClass;
                    break;
                }
            }
            
            if (shareAccessControllerClass != null) {
                // 创建ShareAccessController实例
                Object shareAccessController = injector.getInstance(shareAccessControllerClass);
                
                // GET /s/{shareCode}
                registerRoute(RequestMethod.GET, "/s/{shareCode}", shareAccessController, "accessShare");
            }
        } catch (Exception e) {
            System.err.println("无法注册ShareAccessController路由: " + e.getMessage());
        }
    }
    
    /**
     * 注册存储管理路由
     */
    private void registerStorageRoutes() {
        StorageController storageController = injector.getInstance(StorageController.class);
        
        // GET /api/storage/statistics
        registerRoute(RequestMethod.GET, "/api/storage/statistics", storageController, "getUserStorageStatistics");
        
        // GET /api/storage/quota
        registerRoute(RequestMethod.GET, "/api/storage/quota", storageController, "getUserStorageQuota");
    }
    
    /**
     * 注册通知管理路由
     */
    private void registerNotificationRoutes() {
        NotificationController notificationController = injector.getInstance(NotificationController.class);
        
        // GET /api/notifications
        registerRoute(RequestMethod.GET, "/api/notifications", notificationController, "getUserNotifications");
        
        // PUT /api/notifications/{id}/read
        registerRoute(RequestMethod.PUT, "/api/notifications/{id}/read", notificationController, "markAsRead");
        
        // DELETE /api/notifications/{id}
        registerRoute(RequestMethod.DELETE, "/api/notifications/{id}", notificationController, "deleteNotification");
    }
    
    /**
     * 注册回收站路由
     */
    private void registerRecycleBinRoutes() {
        RecycleBinController recycleBinController = injector.getInstance(RecycleBinController.class);
        
        // GET /api/recycle-bin
        registerRoute(RequestMethod.GET, "/api/recycle-bin", recycleBinController, "getRecycleBinContents");
        
        // POST /api/recycle-bin/restore
        registerRoute(RequestMethod.POST, "/api/recycle-bin/restore", recycleBinController, "restoreFromRecycleBin");
        
        // DELETE /api/recycle-bin/{id}
        registerRoute(RequestMethod.DELETE, "/api/recycle-bin/{id}", recycleBinController, "permanentlyDelete");
    }
    
    /**
     * 注册权限管理路由
     */
    private void registerAclRoutes() {
        AclController aclController = injector.getInstance(AclController.class);
        
        // GET /api/acl/check
        registerRoute(RequestMethod.GET, "/api/acl/check", aclController, "checkPermission");
        
        // GET /api/acl/{resourceId}/permissions
        registerRoute(RequestMethod.GET, "/api/acl/{resourceId}/permissions", aclController, "getResourcePermissions");
        
        // POST /api/acl/{resourceId}/permissions
        registerRoute(RequestMethod.POST, "/api/acl/{resourceId}/permissions", aclController, "setPermission");
        
        // DELETE /api/acl/{resourceId}/permissions
        registerRoute(RequestMethod.DELETE, "/api/acl/{resourceId}/permissions", aclController, "removePermission");
    }
    
    /**
     * 注册单个路由
     */
    private void registerRoute(RequestMethod method, String path, Object controller, String methodName) {
        try {
            // 获取控制器类
            Class<?> controllerClass = controller.getClass();
            
            // 查找对应的方法
            Method handlerMethod = null;
            for (Method m : controllerClass.getDeclaredMethods()) {
                if (m.getName().equals(methodName)) {
                    handlerMethod = m;
                    break;
                }
            }
            
            if (handlerMethod != null) {
                RouteInfo routeInfo = new RouteInfo(path, method, controllerClass, handlerMethod, controller);
                routeRegistry.registerRoute(routeInfo);
            } else {
                System.err.println("Method not found: " + controllerClass.getSimpleName() + "." + methodName);
            }
        } catch (Exception e) {
            System.err.println("Failed to register route: " + method + " " + path + " -> " + e.getMessage());
        }
    }
} 