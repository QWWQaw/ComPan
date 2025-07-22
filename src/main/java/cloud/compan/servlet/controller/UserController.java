package cloud.compan.servlet.controller;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.GetMapping;
import cloud.compan.servlet.annotations.PutMapping;
import cloud.compan.servlet.annotations.RequestBody;
import cloud.compan.servlet.annotations.RequestParam;
import cloud.compan.servlet.annotations.RestController;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.UserDTO;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.service.StorageService;
import cloud.compan.servlet.service.UserService;
import cloud.compan.servlet.utils.ControllerUtils;
import cloud.compan.servlet.web.response.ApiResponseWrapper;

/**
 * 用户管理控制器
 * 处理用户相关的HTTP请求，包括用户列表、用户详情、用户更新等功能
 */
@RestController("/api/v1/users")
@Singleton
public class UserController {
    
    @Inject
    private UserService userService;
    
    @Inject
    private StorageService storageService;
    @Inject
    private AuthService authService;
    
    /**
     * 获取用户列表
     * GET /api/v1/users
     */
    @GetMapping
    public ApiResponseWrapper getUsers(HttpServletRequest request,
                                     @RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "20") int size,
                                     @RequestParam(required = false) String search,
                                     @RequestParam(defaultValue = "createdAt") String sortBy,
                                     @RequestParam(defaultValue = "DESC") String sortOrder) {
        Long currentUserId = getUserIdFromToken(request);
        if (currentUserId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        // 检查当前用户是否有管理员权限
        ServiceResult<String> roleResult = userService.getUserRole(currentUserId);
        if (!roleResult.isSuccess() || !"ADMIN".equals(roleResult.getData())) {
            return ControllerUtils.error(403, "权限不足");
        }
        
        ServiceResult<PageResultDTO<UserDTO>> result = userService.getUsers(page, size, search, sortBy, sortOrder);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取用户详情
     * GET /api/v1/users/{id}
     */
    @GetMapping(path = "/{id}")
    public ApiResponseWrapper getUserDetails(@RequestParam("id") Long userId, HttpServletRequest request) {
        Long currentUserId = getUserIdFromToken(request);
        if (currentUserId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        // 用户只能查看自己的信息，或者管理员可以查看所有用户
        if (!currentUserId.equals(userId)) {
            ServiceResult<String> roleResult = userService.getUserRole(currentUserId);
            if (!roleResult.isSuccess() || !"ADMIN".equals(roleResult.getData())) {
                return ControllerUtils.error(403, "权限不足");
            }
        }
        
        ServiceResult<UserDTO> result = userService.getUserById(userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 更新用户信息
     * PUT /api/v1/users/{id}
     */
    @PutMapping(path = "/{id}")
    public ApiResponseWrapper updateUser(@RequestParam("id") Long userId,
                                        @RequestBody Map<String, Object> requestData,
                                        HttpServletRequest request) {
        Long currentUserId = getUserIdFromToken(request);
        if (currentUserId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        // 用户只能更新自己的信息，或者管理员可以更新所有用户
        if (!currentUserId.equals(userId)) {
            ServiceResult<String> roleResult = userService.getUserRole(currentUserId);
            if (!roleResult.isSuccess() || !"ADMIN".equals(roleResult.getData())) {
                return ControllerUtils.error(403, "权限不足");
            }
        }
        
        // 提取更新参数
        String username = (String) requestData.get("username");
        String email = (String) requestData.get("email");
        String role = (String) requestData.get("role");
        
        ServiceResult<UserDTO> result = userService.updateUser(userId, username, email, role);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 搜索用户
     * GET /api/v1/users/search
     */
    @GetMapping(path = "/search")
    public ApiResponseWrapper searchUsers(HttpServletRequest request,
                                        @RequestParam String keyword,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        Long currentUserId = getUserIdFromToken(request);
        if (currentUserId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        // 检查当前用户是否有管理员权限
        ServiceResult<String> roleResult = userService.getUserRole(currentUserId);
        if (!roleResult.isSuccess() || !"ADMIN".equals(roleResult.getData())) {
            return ControllerUtils.error(403, "权限不足");
        }
        
        ServiceResult<PageResultDTO<UserDTO>> result = userService.searchUsers(keyword, page, size);
        return ControllerUtils.handleServiceResult(result);
    }
    
    // /**
    //  * 获取用户统计
    //  * GET /api/v1/users/stats
    //  */
    // @GetMapping(path = "/stats")
    // public ApiResponseWrapper getUserStats(HttpServletRequest request) {
    //     Long currentUserId = getUserIdFromToken(request);
    //     if (currentUserId == null) {
    //         return ControllerUtils.error(401, "未认证");
    //     }
        
    //     // 检查当前用户是否有管理员权限
    //     ServiceResult<String> roleResult = userService.getUserRole(currentUserId);
    //     if (!roleResult.isSuccess() || !"ADMIN".equals(roleResult.getData())) {
    //         return ControllerUtils.error(403, "权限不足");
    //     }
        
    //     ServiceResult<Map<String, Object>> result = userService.getUserStats();
    //     return ControllerUtils.handleServiceResult(result);
    // }
    
    /**
     * 切换用户状态
     * PUT /api/v1/users/{id}/toggle-status
     */
    @PutMapping(path = "/{id}/toggle-status")
    public ApiResponseWrapper toggleUserStatus(@RequestParam("id") Long userId,
                                              @RequestBody Map<String, Object> requestData,
                                              HttpServletRequest request) {
        Long currentUserId = getUserIdFromToken(request);
        if (currentUserId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        // 检查当前用户是否有管理员权限
        ServiceResult<String> roleResult = userService.getUserRole(currentUserId);
        if (!roleResult.isSuccess() || !"ADMIN".equals(roleResult.getData())) {
            return ControllerUtils.error(403, "权限不足");
        }
        
        Boolean enabled = (Boolean) requestData.get("enabled");
        if (enabled == null) {
            return ControllerUtils.error(400, "enabled参数不能为空");
        }
        
        ServiceResult<Boolean> result = userService.toggleUserStatus(userId, enabled);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 批量删除用户
     * DELETE /api/v1/users/batch
     */
    @PutMapping(path = "/batch")
    public ApiResponseWrapper batchDeleteUsers(@RequestBody Map<String, Object> requestData,
                                              HttpServletRequest request) {
        Long currentUserId = getUserIdFromToken(request);
        if (currentUserId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        // 检查当前用户是否有管理员权限
        ServiceResult<String> roleResult = userService.getUserRole(currentUserId);
        if (!roleResult.isSuccess() || !"ADMIN".equals(roleResult.getData())) {
            return ControllerUtils.error(403, "权限不足");
        }
        
        @SuppressWarnings("unchecked")
        java.util.List<Long> userIds = (java.util.List<Long>) requestData.get("userIds");
        if (userIds == null || userIds.isEmpty()) {
            return ControllerUtils.error(400, "userIds参数不能为空");
        }
        
        ServiceResult<Boolean> result = userService.batchDeleteUsers(userIds);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取用户活动日志
     * GET /api/v1/users/{id}/activity-log
     */
    @GetMapping(path = "/{id}/activity-log")
    public ApiResponseWrapper getUserActivityLog(@RequestParam("id") Long userId,
                                                HttpServletRequest request,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        Long currentUserId = getUserIdFromToken(request);
        if (currentUserId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        // 用户只能查看自己的活动日志，或者管理员可以查看所有用户
        if (!currentUserId.equals(userId)) {
            ServiceResult<String> roleResult = userService.getUserRole(currentUserId);
            if (!roleResult.isSuccess() || !"ADMIN".equals(roleResult.getData())) {
                return ControllerUtils.error(403, "权限不足");
            }
        }
        
        ServiceResult<PageResultDTO<Map<String, Object>>> result = userService.getUserActivityLog(userId, page, size);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取当前用户信息
     * GET /api/v1/users/me/profile
     * Authorization: Bearer <token>
     */
    @GetMapping(path = "/me/profile")
    public ApiResponseWrapper getCurrentUserProfile(HttpServletRequest request) {
        String token = ControllerUtils.extractTokenFromRequest(request);
        if (token == null) {
            return ControllerUtils.error(401, "No authentication token provided");
        }
        
        ServiceResult<UserDTO> result = userService.getCurrentUserProfile(token);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 更新当前用户信息
     * PUT /api/v1/users/me/update-profile
     * Authorization: Bearer <token>
     * 
     * Request body example:
     * {
     *   "displayName": "New Display Name",
     *   "email": "newemail@example.com"
     * }
     */
    @PutMapping(path = "/me/update-profile")
    public ApiResponseWrapper updateCurrentUser(@RequestBody Map<String, Object> requestData,
                                               HttpServletRequest request) {
        String token = ControllerUtils.extractTokenFromRequest(request);
        if (token == null) {
            return ControllerUtils.error(401, "No authentication token provided");
        }
        
        // Get current user ID
        ServiceResult<Long> userIdResult = authService.extractUserIdFromToken(token);
        if (!userIdResult.isSuccess()) {
            return ControllerUtils.handleServiceResult(userIdResult);
        }
        
        Long userId = userIdResult.getData();
        String displayName = (String) requestData.get("displayName");
        String email = (String) requestData.get("email");
        
        // Parameter validation
        if (displayName != null) {
            ControllerUtils.validateStringLength(displayName, "displayName", 1, 100);
        }
        if (email != null) {
            ControllerUtils.validateEmail(email, "email");
        }
        
        // Call service layer to update user information
        ServiceResult<UserDTO> result = userService.updateProfile(userId, displayName, email);
        return ControllerUtils.handleServiceResult(result);
    }

    /**
     * 获取当前用户存储统计信息
     * GET /api/v1/users/me/storage-stats
     * Authorization: Bearer <token>
     */
    @GetMapping(path = "/me/storage-stats")
    public ApiResponseWrapper getCurrentUserStorageStats(HttpServletRequest request) {
        String token = ControllerUtils.extractTokenFromRequest(request);
        if (token == null) {
            return ControllerUtils.error(401, "No authentication token provided");
        }
        
        // Get current user ID
        ServiceResult<Long> userIdResult = authService.extractUserIdFromToken(token);
        if (!userIdResult.isSuccess()) {
            return ControllerUtils.handleServiceResult(userIdResult);
        }
        
        Long userId = userIdResult.getData();
        ServiceResult<Map<String, Object>> result = storageService.getUserStorageStatistics(userId);
        return ControllerUtils.handleServiceResult(result);
    }

    /**
     * 获取当前用户活动日志
     * GET /api/v1/users/me/activity-log
     * Authorization: Bearer <token>
     * 
     * Query parameters:
     * - page: 页码（默认1）
     * - size: 每页大小（默认20）
     * - operation: 操作类型（可选）
     */
    @GetMapping(path = "/me/activity-log")
    public ApiResponseWrapper getCurrentUserActivityLog(HttpServletRequest request,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "20") int size,
                                                       @RequestParam(required = false) String operation) {
        String token = ControllerUtils.extractTokenFromRequest(request);
        if (token == null) {
            return ControllerUtils.error(401, "No authentication token provided");
        }
        
        // Get current user ID
        ServiceResult<Long> userIdResult = authService.extractUserIdFromToken(token);
        if (!userIdResult.isSuccess()) {
            return ControllerUtils.handleServiceResult(userIdResult);
        }
        
        Long userId = userIdResult.getData();
        ServiceResult<PageResultDTO<Map<String, Object>>> result = userService.getUserActivityLog(userId, page, size);
        return ControllerUtils.handleServiceResult(result);
    }

    /**
     * 获取用户存储统计信息（管理员功能）
     * GET /api/v1/users/{id}/storage-stats
     * Authorization: Bearer <token>
     */
    @GetMapping(path = "/{id}/storage-stats")
    public ApiResponseWrapper getUserStorageStats(@RequestParam("id") Long userId, HttpServletRequest request) {
        Long currentUserId = getUserIdFromToken(request);
        if (currentUserId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        // 用户只能查看自己的存储统计，或者管理员可以查看所有用户
        if (!currentUserId.equals(userId)) {
            ServiceResult<String> roleResult = userService.getUserRole(currentUserId);
            if (!roleResult.isSuccess() || !"ADMIN".equals(roleResult.getData())) {
                return ControllerUtils.error(403, "权限不足");
            }
        }
        
        ServiceResult<Map<String, Object>> result = userService.getUserStorageStats(userId);
        return ControllerUtils.handleServiceResult(result);
    }

    /**
     * 获取指定用户活动日志（管理员功能）
     * GET /api/v1/users/{id}/activity-log
     * Authorization: Bearer <token>
     */
    @GetMapping(path = "/{id}/activity-log")
    public ApiResponseWrapper getSpecificUserActivityLog(@RequestParam("id") Long userId,
                                                        HttpServletRequest request,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        Long currentUserId = getUserIdFromToken(request);
        if (currentUserId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        // 用户只能查看自己的活动日志，或者管理员可以查看所有用户
        if (!currentUserId.equals(userId)) {
            ServiceResult<String> roleResult = userService.getUserRole(currentUserId);
            if (!roleResult.isSuccess() || !"ADMIN".equals(roleResult.getData())) {
                return ControllerUtils.error(403, "权限不足");
            }
        }
        
        ServiceResult<PageResultDTO<Map<String, Object>>> result = userService.getUserActivityLog(userId, page, size);
        return ControllerUtils.handleServiceResult(result);
    }

    /**
     * 从token中获取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = ControllerUtils.extractTokenFromRequest(request);
        if (token == null) {
            return null;
        }
        
        ServiceResult<Long> result = authService.extractUserIdFromToken(token);
        if (!result.isSuccess()) {
            return null;
        }
        
        return result.getData();
    }
} 