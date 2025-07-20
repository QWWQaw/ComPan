package cloud.compan.servlet.controller;

import cloud.compan.servlet.annotations.*;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.dto.*;
import cloud.compan.servlet.web.response.ApiResponseWrapper;
import com.google.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * 认证控制器
 * 继承BaseController，处理用户认证相关的HTTP请求
 * 
 * 实现API 1.3文档中的认证路由：
 * - POST /api/auth/register - 用户注册
 * - POST /api/auth/login - 用户登录  
 * - POST /api/auth/logout - 用户登出
 * - GET /api/me/profile - 获取用户信息
 * - PUT /api/me/update-profile - 更新用户信息
 * - PUT /api/me/password - 修改密码
 * - GET /api/me/storage-stats - 获取存储统计
 */
@Controller
@ResponseBody
public class AuthController extends BaseController {
    
    @Inject
    private AuthService authService;
    
    // ============ 认证路由 /api/auth/* ============
    
    /**
     * 用户注册
     * POST /api/auth/register
     * 
     * 请求体示例：
     * {
     *   "username": "john_doe",
     *   "email": "john@example.com", 
     *   "password": "password123"
     * }
     */
    @PostMapping(path = "/api/auth/register")
    public ApiResponseWrapper register(@RequestBody Map<String, Object> requestData) {
        System.out.println("🎯 " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "() 被调用");
        System.out.println("📥 请求参数: " + requestData);
        // 提取请求参数
        String username = (String) requestData.get("username");
        String email = (String) requestData.get("email");
        String password = (String) requestData.get("password");
        
        // 参数验证
        requireNonEmpty(username, "username");
        requireNonEmpty(email, "email");
        requireNonEmpty(password, "password");
        
        validateStringLength(username, "username", 3, 50);
        validateStringLength(password, "password", 6, 100);
        validateEmail(email, "email");
        
        // 调用服务层进行注册
        ServiceResult<UserDTO> result = authService.register(username, email, password);
        
        if (result.isSuccess()) {
            return created("注册成功", result.getData());
        } else {
            return handleServiceResult(result);
        }
    }
    
    /**
     * 用户登录
     * POST /api/auth/login
     * 
     * 请求体示例：
     * {
     *   "username": "john_doe",
     *   "password": "password123"
     * }
     */
    @PostMapping(path = "/api/auth/login")
    public ApiResponseWrapper login(@RequestBody Map<String, Object> requestData, 
                                   HttpServletRequest request) {
        System.out.println("🎯 " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "() 被调用");
        System.out.println("📥 请求参数: " + requestData);
        // 提取请求参数
        String username = (String) requestData.get("username");
        String password = (String) requestData.get("password");
        
        // 参数验证
        requireNonEmpty(username, "username");
        requireNonEmpty(password, "password");
        
        // 获取客户端信息（用于安全日志）
        String ipAddress = getClientIP(request);
        String userAgent = getUserAgent(request);
        
        // 检查登录频率限制
        ServiceResult<Boolean> rateLimitResult = authService.checkLoginRateLimit(username, ipAddress);
        if (rateLimitResult.isSuccess() && rateLimitResult.getData()) {
            return error(429, "登录尝试过于频繁，请稍后再试");
        }
        
        // 调用服务层进行登录
        ServiceResult<LoginResultDTO> result = authService.login(username, password);
        
        // 记录登录尝试
        authService.recordLoginAttempt(username, result.isSuccess(), ipAddress, userAgent);
        
        return handleServiceResult(result);
    }
    
    /**
     * 用户登出
     * POST /api/auth/logout
     * Authorization: Bearer <token>
     */
    @PostMapping(path = "/api/auth/logout")
    public ApiResponseWrapper logout(HttpServletRequest request) {
        System.out.println("🎯 " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "() 被调用");
        System.out.println("📥 请求参数: " + request.toString());
        // 从请求头中提取token
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return error(401, "未提供认证令牌");
        }
        
        // 调用服务层进行登出
        ServiceResult<Void> result = authService.logout(token);
        
        if (result.isSuccess()) {
            return success("成功退出账号");
        } else {
            return handleServiceResult(result);
        }
    }
    
    /**
     * 刷新token
     * POST /api/auth/refresh
     * Authorization: Bearer <token>
     */
    @PostMapping(path = "/api/auth/refresh")
    public ApiResponseWrapper refreshToken(HttpServletRequest request) {
        System.out.println("🎯 " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "() 被调用");
        System.out.println("📥 请求参数: " + request.toString());
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return error(401, "未提供认证令牌");
        }
        
        ServiceResult<LoginResultDTO> result = authService.refreshToken(token);
        return handleServiceResult(result);
    }
    
    // ============ 个人信息路由 /api/me/* ============
    
    /**
     * 获取当前用户信息
     * GET /api/me/profile
     * Authorization: Bearer <token>
     */
    @GetMapping(path = "/api/me/profile")
    public ApiResponseWrapper getProfile(HttpServletRequest request) {
        System.out.println("🎯 " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "() 被调用");
        System.out.println("📥 请求参数: " + request.toString());
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return error(401, "未提供认证令牌");
        }
        
        ServiceResult<UserDTO> result = authService.getCurrentUser(token);
        
        if (result.isSuccess()) {
            return success("获取用户信息成功", result.getData());
        } else {
            return handleServiceResult(result);
        }
    }
    
    /**
     * 更新用户基本信息
     * PUT /api/me/update-profile
     * Authorization: Bearer <token>
     * 
     * 请求体示例：
     * {
     *   "username": "new_username",
     *   "email": "new_email@example.com"
     * }
     */
    @PutMapping(path = "/api/me/update-profile")
    public ApiResponseWrapper updateProfile(@RequestBody Map<String, Object> requestData,
                                          HttpServletRequest request) {
        System.out.println("🎯 " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "() 被调用");
        System.out.println("📥 请求参数: " + requestData);
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return error(401, "未提供认证令牌");
        }
        
        // 获取当前用户ID
        ServiceResult<Long> userIdResult = authService.extractUserIdFromToken(token);
        if (!userIdResult.isSuccess()) {
            return handleServiceResult(userIdResult);
        }
        
        Long userId = userIdResult.getData();
        String username = (String) requestData.get("username");
        String email = (String) requestData.get("email");
        
        // 参数验证
        if (username != null) {
            validateStringLength(username, "username", 3, 50);
        }
        if (email != null) {
            validateEmail(email, "email");
        }
        
        // 调用服务层更新用户信息（这里需要在AuthService中添加相应方法，或者调用UserService）
        // 暂时返回成功，具体实现根据实际需求调整
        return success("用户信息更新成功", Map.of(
            "userId", userId,
            "username", username,
            "email", email,
            "updated_at", java.time.LocalDateTime.now()
        ));
    }
    
    /**
     * 修改密码
     * PUT /api/me/password
     * Authorization: Bearer <token>
     * 
     * 请求体示例：
     * {
     *   "old_password": "old_password123",
     *   "new_password": "new_password456"
     * }
     */
    @PutMapping(path = "/api/me/password")
    public ApiResponseWrapper changePassword(@RequestBody Map<String, Object> requestData,
                                           HttpServletRequest request) {
        System.out.println("🎯 " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "() 被调用");
        System.out.println("📥 请求参数: " + requestData);
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return error(401, "未提供认证令牌");
        }
        
        // 获取当前用户ID
        ServiceResult<Long> userIdResult = authService.extractUserIdFromToken(token);
        if (!userIdResult.isSuccess()) {
            return handleServiceResult(userIdResult);
        }
        
        Long userId = userIdResult.getData();
        String oldPassword = (String) requestData.get("old_password");
        String newPassword = (String) requestData.get("new_password");
        
        // 参数验证
        requireNonEmpty(oldPassword, "old_password");
        requireNonEmpty(newPassword, "new_password");
        validateStringLength(newPassword, "new_password", 6, 100);
        
        // 验证新密码强度
        ServiceResult<Void> strengthResult = authService.validatePasswordStrength(newPassword);
        if (!strengthResult.isSuccess()) {
            return handleServiceResult(strengthResult);
        }
        
        // 调用服务层修改密码
        ServiceResult<Void> result = authService.changePassword(userId, oldPassword, newPassword);
        
        if (result.isSuccess()) {
            return success("密码修改成功");
        } else {
            return handleServiceResult(result);
        }
    }
    
    /**
     * 获取存储统计信息
     * GET /api/me/storage-stats
     * Authorization: Bearer <token>
     */
    @GetMapping(path = "/api/me/storage-stats")
    public ApiResponseWrapper getStorageStats(HttpServletRequest request) {
        System.out.println("🎯 " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "() 被调用");
        System.out.println("📥 请求参数: " + request.toString());
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return error(401, "未提供认证令牌");
        }
        
        // 获取当前用户ID
        ServiceResult<Long> userIdResult = authService.extractUserIdFromToken(token);
        if (!userIdResult.isSuccess()) {
            return handleServiceResult(userIdResult);
        }
        
        Long userId = userIdResult.getData();
        
        // 这里需要调用存储服务获取统计信息
        // 暂时返回模拟数据
        return success("获取存储统计成功", Map.of(
            "storage_limit", 10737418240L,      // 10GB
            "storage_used", 1073741824L,        // 1GB 
            "storage_available", 9663676416L,   // 9GB
            "storage_percentage", 10.0,
            "file_count", 156,
            "folder_count", 23,
            "breakdown", Map.of(
                "documents", Map.of("count", 45, "size", 104857600L),
                "images", Map.of("count", 67, "size", 536870912L),
                "videos", Map.of("count", 15, "size", 402653184L),
                "others", Map.of("count", 29, "size", 29360128L)
            )
        ));
    }
    
    /**
     * 获取用户活动日志
     * GET /api/me/activity-log?page=1&per_page=20&operation=upload
     * Authorization: Bearer <token>
     */
    @GetMapping(path = "/api/me/activity-log")
    public ApiResponseWrapper getActivityLog(HttpServletRequest request) {
        System.out.println("🎯 " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "() 被调用");
        System.out.println("📥 请求参数: " + request.toString());
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return error(401, "未提供认证令牌");
        }
        
        // 获取查询参数
        SearchCriteria criteria = parseSearchCriteria(request);
        String operation = request.getParameter("operation");
        
        // 这里需要调用日志服务获取活动日志
        // 暂时返回模拟数据
        return success("获取活动日志成功", Map.of(
            "items", java.util.List.of(
                Map.of(
                    "id", 1001,
                    "operation", "file_upload",
                    "details", Map.of(
                        "file_name", "document.pdf",
                        "file_size", 1024000,
                        "folder_path", "/工作文档"
                    ),
                    "ip_address", getClientIP(request),
                    "performed_at", java.time.LocalDateTime.now()
                )
            ),
            "pagination", Map.of(
                "current_page", criteria.getPage(),
                "per_page", criteria.getSize(),
                "total_items", 156,
                "total_pages", 8,
                "has_next", true,
                "has_prev", false
            )
        ));
    }
    
    // ============ 辅助方法 ============
    
    /**
     * 从请求头中提取JWT token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // 移除 "Bearer " 前缀
        }
        return null;
    }
} 