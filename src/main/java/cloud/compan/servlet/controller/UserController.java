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
import cloud.compan.servlet.service.UserService;
import cloud.compan.servlet.web.response.ApiResponseWrapper;

/**
 * 用户管理控制器
 * 处理用户相关的HTTP请求
 * 
 * 实现API 1.3文档中的用户管理路由：
 * - GET /api/users - 获取用户列表
 * - GET /api/users/{id} - 获取用户详情
 * - PUT /api/users/{id} - 更新用户信息
 * - GET /api/users/search - 搜索用户
 */
@RestController("/api/users")
@Singleton
public class UserController extends BaseController {
    
    @Inject
    private UserService userService;
    
    @Inject
    private AuthService authService;
    
    /**
     * 获取用户列表
     * GET /api/users
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
            return error(401, "未认证");
        }
        
        // 检查当前用户是否有管理员权限
        ServiceResult<String> roleResult = userService.getUserRole(currentUserId);
        if (!roleResult.isSuccess() || !"ADMIN".equals(roleResult.getData())) {
            return error(403, "权限不足");
        }
        
        ServiceResult<PageResultDTO<UserDTO>> result = userService.getUsers(page, size, search, sortBy, sortOrder);
        return handleServiceResult(result);
    }
    
    /**
     * 获取用户详情
     * GET /api/users/{id}
     */
    @GetMapping(path = "/{id}")
    public ApiResponseWrapper getUserDetails(@RequestParam("id") Long userId, HttpServletRequest request) {
        Long currentUserId = getUserIdFromToken(request);
        if (currentUserId == null) {
            return error(401, "未认证");
        }
        
        // 用户只能查看自己的信息，或者管理员可以查看所有用户
        if (!currentUserId.equals(userId)) {
            ServiceResult<String> roleResult = userService.getUserRole(currentUserId);
            if (!roleResult.isSuccess() || !"ADMIN".equals(roleResult.getData())) {
                return error(403, "权限不足");
            }
        }
        
        ServiceResult<UserDTO> result = userService.getUserById(userId);
        return handleServiceResult(result);
    }
    
    /**
     * 更新用户信息
     * PUT /api/users/{id}
     */
    @PutMapping(path = "/{id}")
    public ApiResponseWrapper updateUser(@RequestParam("id") Long userId,
                                        @RequestBody Map<String, Object> requestData,
                                        HttpServletRequest request) {
        Long currentUserId = getUserIdFromToken(request);
        if (currentUserId == null) {
            return error(401, "未认证");
        }
        
        // 用户只能更新自己的信息，或者管理员可以更新所有用户
        if (!currentUserId.equals(userId)) {
            ServiceResult<String> roleResult = userService.getUserRole(currentUserId);
            if (!roleResult.isSuccess() || !"ADMIN".equals(roleResult.getData())) {
                return error(403, "权限不足");
            }
        }
        
        // 提取更新参数
        String username = (String) requestData.get("username");
        String email = (String) requestData.get("email");
        String role = (String) requestData.get("role");
        
        ServiceResult<UserDTO> result = userService.updateUser(userId, username, email, role);
        return handleServiceResult(result);
    }
    
    /**
     * 搜索用户
     * GET /api/users/search
     */
    @GetMapping(path = "/search")
    public ApiResponseWrapper searchUsers(HttpServletRequest request,
                                        @RequestParam String keyword,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        Long currentUserId = getUserIdFromToken(request);
        if (currentUserId == null) {
            return error(401, "未认证");
        }
        
        // 检查当前用户是否有管理员权限
        ServiceResult<String> roleResult = userService.getUserRole(currentUserId);
        if (!roleResult.isSuccess() || !"ADMIN".equals(roleResult.getData())) {
            return error(403, "权限不足");
        }
        
        ServiceResult<PageResultDTO<UserDTO>> result = userService.searchUsers(keyword, page, size);
        return handleServiceResult(result);
    }
    
    /**
     * 获取用户统计信息
     * GET /api/users/stats
     */
    @GetMapping(path = "/stats")
    public ApiResponseWrapper getUserStats(HttpServletRequest request) {
        Long currentUserId = getUserIdFromToken(request);
        if (currentUserId == null) {
            return error(401, "未认证");
        }
        
        // 检查当前用户是否有管理员权限
        ServiceResult<String> roleResult = userService.getUserRole(currentUserId);
        if (!roleResult.isSuccess() || !"ADMIN".equals(roleResult.getData())) {
            return error(403, "权限不足");
        }
        
        ServiceResult<Map<String, Object>> result = userService.getUserStats();
        return handleServiceResult(result);
    }
    
    /**
     * 从token中提取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null) {
            return null;
        }
        
        ServiceResult<Long> result = authService.extractUserIdFromToken(token);
        return result.isSuccess() ? result.getData() : null;
    }
    
    /**
     * 从请求中提取token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
} 