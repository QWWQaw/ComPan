package cloud.compan.servlet.web;

import cloud.compan.servlet.controller.*;
import com.google.inject.Injector;
import java.lang.reflect.Method;
import cloud.compan.servlet.annotations.enums.RequestMethod;

/**
 * Hardcoded Route Registry
 * Used to manually register all routes in production environment, avoiding reflection scanning issues
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
        System.out.println("Starting hardcoded route registration...");
        
        registerAuthRoutes();
        registerFileRoutes();
        registerFolderRoutes();
        registerShareRoutes();
        registerStorageRoutes();
        registerNotificationRoutes();
        registerRecycleBinRoutes();
        registerAclRoutes();
        
        System.out.println("All routes hardcoded registration completed");
        
        // Print all registered routes for debugging
        routeRegistry.printAllRoutes();
    }
    
    /**
     * Register authentication related routes
     */
    private void registerAuthRoutes() {
        AuthController authController = injector.getInstance(AuthController.class);
        
        registerRoute("POST", "/api/auth/register", AuthController.class, "register", authController);
        registerRoute("POST", "/api/auth/login", AuthController.class, "login", authController);
        registerRoute("POST", "/api/auth/logout", AuthController.class, "logout", authController);
        registerRoute("POST", "/api/auth/refresh", AuthController.class, "refreshToken", authController);
        registerRoute("GET", "/api/me/profile", AuthController.class, "getProfile", authController);
        registerRoute("PUT", "/api/me/update-profile", AuthController.class, "updateProfile", authController);
        registerRoute("PUT", "/api/me/password", AuthController.class, "changePassword", authController);
        registerRoute("GET", "/api/me/storage-stats", AuthController.class, "getStorageStats", authController);
        registerRoute("GET", "/api/me/activity-log", AuthController.class, "getActivityLog", authController);
    }
    
    /**
     * Register file management routes
     */
    private void registerFileRoutes() {
        FileController fileController = injector.getInstance(FileController.class);
        
        registerRoute("POST", "/api/files/upload", FileController.class, "uploadFile", fileController);
        registerRoute("GET", "/api/files", FileController.class, "getFiles", fileController);
        registerRoute("GET", "/api/files/{id}", FileController.class, "getFileDetails", fileController);
        registerRoute("PUT", "/api/files/{id}/rename", FileController.class, "renameFile", fileController);
        registerRoute("DELETE", "/api/files/{id}", FileController.class, "deleteFile", fileController);
        registerRoute("GET", "/api/files/{id}/download", FileController.class, "downloadFile", fileController);
        registerRoute("GET", "/api/files/search", FileController.class, "searchFiles", fileController);
    }
    
    /**
     * Register folder management routes
     */
    private void registerFolderRoutes() {
        FolderController folderController = injector.getInstance(FolderController.class);
        
        registerRoute("POST", "/api/folders", FolderController.class, "createFolder", folderController);
        registerRoute("GET", "/api/folders", FolderController.class, "getFolders", folderController);
        registerRoute("GET", "/api/folders/{id}", FolderController.class, "getFolderDetails", folderController);
        registerRoute("PUT", "/api/folders/{id}/rename", FolderController.class, "renameFolder", folderController);
        registerRoute("DELETE", "/api/folders/{id}", FolderController.class, "deleteFolder", folderController);
        registerRoute("GET", "/api/folders/{id}/tree", FolderController.class, "getFolderTree", folderController);
        registerRoute("PUT", "/api/folders/{id}/move", FolderController.class, "moveFolder", folderController);
        registerRoute("GET", "/api/folders/search", FolderController.class, "searchFolders", folderController);
    }
    
    /**
     * Register share management routes
     */
    private void registerShareRoutes() {
        ShareController shareController = injector.getInstance(ShareController.class);
        
        registerRoute("POST", "/api/shares", ShareController.class, "createShare", shareController);
        registerRoute("GET", "/api/shares", ShareController.class, "getUserShares", shareController);
        registerRoute("GET", "/api/shares/{id}", ShareController.class, "getShareDetails", shareController);
        registerRoute("PATCH", "/api/shares/{id}", ShareController.class, "updateShareSettings", shareController);
        registerRoute("DELETE", "/api/shares/{id}", ShareController.class, "deleteShare", shareController);
        registerRoute("GET", "/api/shares/{id}/statistics", ShareController.class, "getShareStatistics", shareController);
        registerRoute("POST", "/api/shares/batch", ShareController.class, "createBatchShares", shareController);
    }
    
    /**
     * Register storage management routes
     */
    private void registerStorageRoutes() {
        StorageController storageController = injector.getInstance(StorageController.class);
        
        registerRoute("GET", "/api/storage/statistics", StorageController.class, "getUserStorageStatistics", storageController);
        registerRoute("GET", "/api/storage/quota", StorageController.class, "getUserStorageQuota", storageController);
    }
    
    /**
     * Register notification management routes
     */
    private void registerNotificationRoutes() {
        NotificationController notificationController = injector.getInstance(NotificationController.class);
        
        registerRoute("GET", "/api/notifications", NotificationController.class, "getUserNotifications", notificationController);
        registerRoute("PUT", "/api/notifications/{id}/read", NotificationController.class, "markAsRead", notificationController);
        registerRoute("DELETE", "/api/notifications/{id}", NotificationController.class, "deleteNotification", notificationController);
    }
    
    /**
     * Register recycle bin routes
     */
    private void registerRecycleBinRoutes() {
        RecycleBinController recycleBinController = injector.getInstance(RecycleBinController.class);
        
        registerRoute("GET", "/api/recycle-bin", RecycleBinController.class, "getRecycleBinContents", recycleBinController);
        registerRoute("POST", "/api/recycle-bin/restore", RecycleBinController.class, "restoreFromRecycleBin", recycleBinController);
        registerRoute("DELETE", "/api/recycle-bin/{id}", RecycleBinController.class, "permanentlyDeleteById", recycleBinController);
    }
    
    /**
     * Register permission control routes
     */
    private void registerAclRoutes() {
        AclController aclController = injector.getInstance(AclController.class);
        
        registerRoute("GET", "/api/acl/check", AclController.class, "checkPermission", aclController);
        registerRoute("GET", "/api/acl/{resourceId}/permissions", AclController.class, "getResourcePermissions", aclController);
        registerRoute("POST", "/api/acl/{resourceId}/permissions", AclController.class, "setPermission", aclController);
        registerRoute("DELETE", "/api/acl/{resourceId}/permissions", AclController.class, "removeResourcePermission", aclController);
    }
    
    /**
     * Register single route
     */
    private void registerRoute(String httpMethod, String path, Class<?> controllerClass, 
                             String methodName, Object controllerInstance) {
        try {
            System.out.println("DEBUG: Registering route: " + httpMethod + " " + path + " -> " + controllerClass.getSimpleName() + "." + methodName);
            
            // Get method object
            Method method = findMethod(controllerClass, methodName);
            if (method == null) {
                System.err.println("Method not found: " + controllerClass.getSimpleName() + "." + methodName);
                return;
            }
            
            System.out.println("DEBUG: Found method: " + method.getName() + " with " + method.getParameterCount() + " parameters");
            
            // Convert HTTP method string to enum
            RequestMethod requestMethod = RequestMethod.valueOf(httpMethod);
            
            // Create route info - check if path contains parameters
            RouteInfo routeInfo;
            if (path.contains("{")) {
                // Parameterized route
                System.out.println("DEBUG: Creating parameterized route for: " + path);
                routeInfo = new ParameterizedRouteInfo(path, requestMethod, controllerClass, method, controllerInstance);
            } else {
                // Regular route
                System.out.println("DEBUG: Creating regular route for: " + path);
                routeInfo = new RouteInfo(path, requestMethod, controllerClass, method, controllerInstance);
            }
            
            // Register route
            routeRegistry.registerRoute(routeInfo);
            
            System.out.println("Registered route: " + httpMethod + " " + path + " -> " + controllerClass.getSimpleName() + "." + methodName);
            
        } catch (Exception e) {
            System.err.println("Failed to register route: " + httpMethod + " " + path + " -> " + controllerClass.getSimpleName() + "." + methodName);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Find method
     */
    private Method findMethod(Class<?> controllerClass, String methodName) {
        try {
            // Try to find method with HttpServletRequest only
            return controllerClass.getDeclaredMethod(methodName, 
                jakarta.servlet.http.HttpServletRequest.class);
        } catch (NoSuchMethodException e1) {
            try {
                // Try to find method with Map and HttpServletRequest
                return controllerClass.getDeclaredMethod(methodName, 
                    java.util.Map.class, jakarta.servlet.http.HttpServletRequest.class);
            } catch (NoSuchMethodException e2) {
                try {
                    // Try to find method with Map only
                    return controllerClass.getDeclaredMethod(methodName, 
                        java.util.Map.class);
                } catch (NoSuchMethodException e3) {
                    try {
                        // Try to find method with no parameters
                        return controllerClass.getDeclaredMethod(methodName);
                    } catch (NoSuchMethodException e4) {
                        try {
                            // Try to find method with @PathVariable Long and HttpServletRequest
                            return controllerClass.getDeclaredMethod(methodName, 
                                Long.class, jakarta.servlet.http.HttpServletRequest.class);
                        } catch (NoSuchMethodException e5) {
                            try {
                                // Try to find method with @PathVariable Long, Map, and HttpServletRequest
                                return controllerClass.getDeclaredMethod(methodName, 
                                    Long.class, java.util.Map.class, jakarta.servlet.http.HttpServletRequest.class);
                            } catch (NoSuchMethodException e6) {
                                try {
                                    // Try to find method with @PathVariable Long and HttpServletResponse
                                    return controllerClass.getDeclaredMethod(methodName, 
                                        Long.class, jakarta.servlet.http.HttpServletRequest.class, 
                                        jakarta.servlet.http.HttpServletResponse.class);
                                } catch (NoSuchMethodException e7) {
                                    // If all attempts fail, return null
                                    return null;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 