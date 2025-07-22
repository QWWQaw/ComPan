package cloud.compan.servlet.controller;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.GetMapping;
import cloud.compan.servlet.annotations.PostMapping;
import cloud.compan.servlet.annotations.PutMapping;
import cloud.compan.servlet.annotations.RequestBody;
import cloud.compan.servlet.annotations.RestController;
import cloud.compan.servlet.dto.LoginResultDTO;
import cloud.compan.servlet.dto.SearchCriteria;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.UserDTO;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.utils.ControllerUtils;
import cloud.compan.servlet.web.response.ApiResponseWrapper;

/**
 * Authentication Controller
 * Handles HTTP requests related to user authentication
 * 
 * Implements authentication routes from API 1.3 documentation:
 * - POST /api/v1/auth/register - User registration
 * - POST /api/v1/auth/login - User login  
 * - POST /api/v1/auth/logout - User logout
 * - GET /api/v1/me/profile - Get user information
 * - PUT /api/v1/me/update-profile - Update user information
 * - PUT /api/v1/me/password - Change password
 * - GET /api/v1/me/storage-stats - Get storage statistics
 */
@RestController
@Singleton
public class AuthController {
    
    @Inject
    private AuthService authService;
    
    // ============ Authentication Routes /api/v1/auth/* ============
    
    /**
     * User registration
     * POST /api/v1/auth/register
     * 
     * Request body example:
     * {
     *   "username": "john_doe",
     *   "email": "john@example.com",
     *   "password": "password123"
     * }
     */
    @PostMapping(path = "/api/v1/auth/register")
    public ApiResponseWrapper register(@RequestBody Map<String, Object> requestData) {
        System.out.println("CALLED: " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        System.out.println("REQUEST_PARAMS: " + requestData);
        // Extract request parameters
        String username = (String) requestData.get("username");
        String email = (String) requestData.get("email");
        String password = (String) requestData.get("password");
        
        // Parameter validation
        ControllerUtils.requireNonEmpty(username, "username");
        ControllerUtils.requireNonEmpty(email, "email");
        ControllerUtils.requireNonEmpty(password, "password");
        
        ControllerUtils.validateStringLength(username, "username", 3, 20);
        ControllerUtils.validateStringLength(password, "password", 6, 20);
        ControllerUtils.validateEmail(email, "email");
        
        // Call service layer for registration
        ServiceResult<UserDTO> result = authService.register(username, email, password);
        
        if (result.isSuccess()) {
            return ControllerUtils.created("Registration successful", result.getData());
        } else {
            return ControllerUtils.handleServiceResult(result);
        }
    }
    
    /**
     * User login
     * POST /api/v1/auth/login
     * 
     * Request body example:
     * {
     *   "username": "john_doe",
     *   "password": "password123"
     * }
     */
    @PostMapping(path = "/api/v1/auth/login")
    public ApiResponseWrapper login(@RequestBody Map<String, Object> requestData, 
                                   HttpServletRequest request) {
        System.out.println("CALLED: " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        System.out.println("REQUEST_PARAMS: " + requestData);
        // Extract request parameters
        String username = (String) requestData.get("username");
        String password = (String) requestData.get("password");
        
        // Parameter validation
        ControllerUtils.requireNonEmpty(username, "username");
        ControllerUtils.requireNonEmpty(password, "password");
        
        // Call service layer for login
        ServiceResult<LoginResultDTO> result = authService.login(username, password, request);
        
        if (result.isSuccess()) {
            return ControllerUtils.success("Login successful", result.getData());
        } else {
            return ControllerUtils.handleServiceResult(result);
        }
    }
    
    /**
     * User logout
     * POST /api/v1/auth/logout
     * Authorization: Bearer <token>
     */
    @PostMapping(path = "/api/v1/auth/logout")
    public ApiResponseWrapper logout(HttpServletRequest request) {
        System.out.println("CALLED: " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        System.out.println("REQUEST_PARAMS: " + request.toString());
        String token = ControllerUtils.extractTokenFromRequest(request);
        
        if (token == null) {
            return ControllerUtils.error(401, "No authentication token provided");
        }
        
        ServiceResult<Void> result = authService.logout(token);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * Refresh token
     * POST /api/v1/auth/refresh
     * Authorization: Bearer <token>
     */
    @PostMapping(path = "/api/v1/auth/refresh")
    public ApiResponseWrapper refreshToken(HttpServletRequest request) {
        System.out.println("CALLED: " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        System.out.println("REQUEST_PARAMS: " + request.toString());
        String token = ControllerUtils.extractTokenFromRequest(request);
        
        if (token == null) {
            return ControllerUtils.error(401, "No authentication token provided");
        }
        
        ServiceResult<LoginResultDTO> result = authService.refreshToken(token);
        return ControllerUtils.handleServiceResult(result);
    }
    
    // ============ User Profile Routes /api/v1/me/* ============
    
    /**
     * Change password
     * PUT /api/v1/me/password
     * Authorization: Bearer <token>
     * 
     * Request body example:
     * {
     *   "old_password": "old_password123",
     *   "new_password": "new_password456"
     * }
     */
    @PutMapping(path = "/api/v1/me/password")
    public ApiResponseWrapper changePassword(@RequestBody Map<String, Object> requestData,
                                           HttpServletRequest request) {
        System.out.println("CALLED: " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        System.out.println("REQUEST_PARAMS: " + requestData);
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
        String oldPassword = (String) requestData.get("old_password");
        String newPassword = (String) requestData.get("new_password");
        
        // Parameter validation
        ControllerUtils.requireNonEmpty(oldPassword, "old_password");
        ControllerUtils.requireNonEmpty(newPassword, "new_password");
        ControllerUtils.validateStringLength(newPassword, "new_password", 6, 100);
        
        // Call service layer to change password
        ServiceResult<Void> result = authService.changePassword(userId, oldPassword, newPassword);
        
        if (result.isSuccess()) {
            return ControllerUtils.success("Password changed successfully");
        } else {
            return ControllerUtils.handleServiceResult(result);
        }
    }
    
    /**
     * Get storage statistics
     * GET /api/v1/me/storage-stats
     * Authorization: Bearer <token>
     */
    @GetMapping(path = "/api/v1/me/storage-stats")
    public ApiResponseWrapper getStorageStats(HttpServletRequest request) {
        System.out.println("CALLED: " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        System.out.println("REQUEST_PARAMS: " + request.toString());
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
        
        // Need to call storage service to get statistics
        // Temporarily return mock data
        return ControllerUtils.success("Get storage statistics successful", Map.of(
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
     * Get user activity log
     * GET /api/v1/me/activity-log?page=1&per_page=20&operation=upload
     * Authorization: Bearer <token>
     */
    @GetMapping(path = "/api/v1/me/activity-log")
    public ApiResponseWrapper getActivityLog(HttpServletRequest request) {
        System.out.println("CALLED: " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        System.out.println("REQUEST_PARAMS: " + request.toString());
        String token = ControllerUtils.extractTokenFromRequest(request);
        
        if (token == null) {
            return ControllerUtils.error(401, "No authentication token provided");
        }
        
        // Get query parameters
        SearchCriteria criteria = ControllerUtils.parseSearchCriteria(request);
        String operation = request.getParameter("operation");
        
        // Need to call log service to get activity logs
        // Temporarily return mock data
        Map<String, Object> activityItem = new java.util.HashMap<>();
        activityItem.put("id", 1001);
        activityItem.put("operation", "file_upload");
        
        Map<String, Object> details = new java.util.HashMap<>();
        details.put("file_name", "document.pdf");
        details.put("file_size", 1024000);
        details.put("folder_path", "/work_documents");
        activityItem.put("details", details);
        
        activityItem.put("ip_address", ControllerUtils.getClientIP(request));
        activityItem.put("performed_at", java.time.LocalDateTime.now());
        
        Map<String, Object> pagination = new java.util.HashMap<>();
        pagination.put("current_page", criteria.getPage());
        pagination.put("per_page", criteria.getSize());
        pagination.put("total_items", 156);
        pagination.put("total_pages", 8);
        pagination.put("has_next", true);
        pagination.put("has_prev", false);
        
        Map<String, Object> responseData = new java.util.HashMap<>();
        responseData.put("items", java.util.List.of(activityItem));
        responseData.put("pagination", pagination);
        
        return ControllerUtils.success("Get activity log successful", responseData);
    }
} 