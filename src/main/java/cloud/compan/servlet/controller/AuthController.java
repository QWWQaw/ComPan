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
import cloud.compan.servlet.web.response.ApiResponseWrapper;

/**
 * Authentication Controller
 * Inherits BaseController, handles HTTP requests related to user authentication
 * 
 * Implements authentication routes from API 1.3 documentation:
 * - POST /api/auth/register - User registration
 * - POST /api/auth/login - User login  
 * - POST /api/auth/logout - User logout
 * - GET /api/me/profile - Get user information
 * - PUT /api/me/update-profile - Update user information
 * - PUT /api/me/password - Change password
 * - GET /api/me/storage-stats - Get storage statistics
 */
@RestController
@Singleton
public class AuthController extends BaseController {
    
    @Inject
    private AuthService authService;
    
    // ============ Authentication Routes /api/auth/* ============
    
    /**
     * User registration
     * POST /api/auth/register
     * 
     * Request body example:
     * {
     *   "username": "john_doe",
     *   "email": "john@example.com",
     *   "password": "password123"
     * }
     */
    @PostMapping(path = "/api/auth/register")
    public ApiResponseWrapper register(@RequestBody Map<String, Object> requestData) {
        System.out.println("CALLED: " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        System.out.println("REQUEST_PARAMS: " + requestData);
        // Extract request parameters
        String username = (String) requestData.get("username");
        String email = (String) requestData.get("email");
        String password = (String) requestData.get("password");
        
        // Parameter validation
        requireNonEmpty(username, "username");
        requireNonEmpty(email, "email");
        requireNonEmpty(password, "password");
        
        validateStringLength(username, "username", 3, 50);
        validateStringLength(password, "password", 6, 100);
        validateEmail(email, "email");
        
        // Call service layer for registration
        ServiceResult<UserDTO> result = authService.register(username, email, password);
        
        if (result.isSuccess()) {
            return created("Registration successful", result.getData());
        } else {
            return handleServiceResult(result);
        }
    }
    
    /**
     * User login
     * POST /api/auth/login
     * 
     * Request body example:
     * {
     *   "username": "john_doe",
     *   "password": "password123"
     * }
     */
    @PostMapping(path = "/api/auth/login")
    public ApiResponseWrapper login(@RequestBody Map<String, Object> requestData, 
                                   HttpServletRequest request) {
        System.out.println("CALLED: " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        System.out.println("REQUEST_PARAMS: " + requestData);
        // Extract request parameters
        String username = (String) requestData.get("username");
        String password = (String) requestData.get("password");
        
        // Parameter validation
        requireNonEmpty(username, "username");
        requireNonEmpty(password, "password");
        
        // Get client information (for security logs)
        String ipAddress = getClientIP(request);
        String userAgent = getUserAgent(request);
        
        // Check login rate limit
        ServiceResult<Boolean> rateLimitResult = authService.checkLoginRateLimit(username, ipAddress);
        if (rateLimitResult.isSuccess() && rateLimitResult.getData()) {
            return error(429, "Login attempts too frequent, please try again later");
        }
        
        // Call service layer for login
        ServiceResult<LoginResultDTO> result = authService.login(username, password);
        
        // Record login attempt
        authService.recordLoginAttempt(username, result.isSuccess(), ipAddress, userAgent);
        
        return handleServiceResult(result);
    }
    
    /**
     * User logout
     * POST /api/auth/logout
     * Authorization: Bearer <token>
     */
    @PostMapping(path = "/api/auth/logout")
    public ApiResponseWrapper logout(HttpServletRequest request) {
        System.out.println("CALLED: " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        System.out.println("REQUEST_PARAMS: " + request.toString());
        // Extract token from request header
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return error(401, "No authentication token provided");
        }
        
        // Call service layer for logout
        ServiceResult<Void> result = authService.logout(token);
        
        if (result.isSuccess()) {
            return success("Successfully logged out");
        } else {
            return handleServiceResult(result);
        }
    }
    
    /**
     * Refresh token
     * POST /api/auth/refresh
     * Authorization: Bearer <token>
     */
    @PostMapping(path = "/api/auth/refresh")
    public ApiResponseWrapper refreshToken(HttpServletRequest request) {
        System.out.println("CALLED: " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        System.out.println("REQUEST_PARAMS: " + request.toString());
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return error(401, "No authentication token provided");
        }
        
        // Call service layer to refresh token
        ServiceResult<LoginResultDTO> result = authService.refreshToken(token);
        
        return handleServiceResult(result);
    }
    
    // ============ Personal Information Routes /api/me/* ============
    
    /**
     * Get user profile
     * GET /api/me/profile
     * Authorization: Bearer <token>
     */
    @GetMapping(path = "/api/me/profile")
    public ApiResponseWrapper getProfile(HttpServletRequest request) {
        System.out.println("CALLED: " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        System.out.println("REQUEST_PARAMS: " + request);
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return error(401, "No authentication token provided");
        }
        
        // Get current user ID
        ServiceResult<Long> userIdResult = authService.extractUserIdFromToken(token);
        if (!userIdResult.isSuccess()) {
            return handleServiceResult(userIdResult);
        }
        
        Long userId = userIdResult.getData();
        
        // Call service layer to get user information
        ServiceResult<UserDTO> result = authService.getCurrentUser(token);
        
        if (result.isSuccess()) {
            return success("Get user information successful", result.getData());
        } else {
            return handleServiceResult(result);
        }
    }
    
    /**
     * Update user profile
     * PUT /api/me/update-profile
     * Authorization: Bearer <token>
     * 
     * Request body example:
     * {
     *   "displayName": "New Display Name",
     *   "email": "newemail@example.com",
     *   "avatar": "avatar_url"
     * }
     */
    @PutMapping(path = "/api/me/update-profile")
    public ApiResponseWrapper updateProfile(@RequestBody Map<String, Object> requestData,
                                          HttpServletRequest request) {
        System.out.println("CALLED: " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        System.out.println("REQUEST_PARAMS: " + requestData);
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return error(401, "No authentication token provided");
        }
        
        // Get current user ID
        ServiceResult<Long> userIdResult = authService.extractUserIdFromToken(token);
        if (!userIdResult.isSuccess()) {
            return handleServiceResult(userIdResult);
        }
        
        Long userId = userIdResult.getData();
        String displayName = (String) requestData.get("displayName");
        String email = (String) requestData.get("email");
        String avatar = (String) requestData.get("avatar");
        
        // Parameter validation
        if (displayName != null) {
            validateStringLength(displayName, "displayName", 1, 100);
        }
        if (email != null) {
            validateEmail(email, "email");
        }
        if (avatar != null) {
            validateStringLength(avatar, "avatar", 1, 500);
        }
        
        // Call service layer to update user information (need to add corresponding method in AuthService, or call UserService)
        // Temporarily return success, specific implementation adjusted according to actual needs
        Map<String, Object> responseData = new java.util.HashMap<>();
        responseData.put("userId", userId);
        if (displayName != null) responseData.put("displayName", displayName);
        if (email != null) responseData.put("email", email);
        if (avatar != null) responseData.put("avatar", avatar);
        responseData.put("updated_at", java.time.LocalDateTime.now());
        
        return success("User information updated successfully", responseData);
    }
    
    /**
     * Change password
     * PUT /api/me/password
     * Authorization: Bearer <token>
     * 
     * Request body example:
     * {
     *   "old_password": "old_password123",
     *   "new_password": "new_password456"
     * }
     * OR
     * {
     *   "currentPassword": "old_password123",
     *   "newPassword": "new_password456"
     * }
     */
    @PutMapping(path = "/api/me/password")
    public ApiResponseWrapper changePassword(@RequestBody Map<String, Object> requestData,
                                           HttpServletRequest request) {
        System.out.println("CALLED: " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        System.out.println("REQUEST_PARAMS: " + requestData);
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return error(401, "No authentication token provided");
        }
        
        // Get current user ID
        ServiceResult<Long> userIdResult = authService.extractUserIdFromToken(token);
        if (!userIdResult.isSuccess()) {
            return handleServiceResult(userIdResult);
        }
        
        Long userId = userIdResult.getData();
        
        // Support multiple parameter name formats
        String oldPassword = (String) requestData.get("old_password");
        if (oldPassword == null) {
            oldPassword = (String) requestData.get("currentPassword");
        }
        
        String newPassword = (String) requestData.get("new_password");
        if (newPassword == null) {
            newPassword = (String) requestData.get("newPassword");
        }
        
        // Parameter validation
        requireNonEmpty(oldPassword, "old_password/currentPassword");
        requireNonEmpty(newPassword, "new_password/newPassword");
        validateStringLength(newPassword, "new_password", 6, 100);
        
        // Validate new password strength
        ServiceResult<Void> strengthResult = authService.validatePasswordStrength(newPassword);
        if (!strengthResult.isSuccess()) {
            return handleServiceResult(strengthResult);
        }
        
        // Call service layer to change password
        ServiceResult<Void> result = authService.changePassword(userId, oldPassword, newPassword);
        
        if (result.isSuccess()) {
            return success("Password changed successfully");
        } else {
            return handleServiceResult(result);
        }
    }
    
    /**
     * Get storage statistics
     * GET /api/me/storage-stats
     * Authorization: Bearer <token>
     */
    @GetMapping(path = "/api/me/storage-stats")
    public ApiResponseWrapper getStorageStats(HttpServletRequest request) {
        System.out.println("CALLED: " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        System.out.println("REQUEST_PARAMS: " + request.toString());
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return error(401, "No authentication token provided");
        }
        
        // Get current user ID
        ServiceResult<Long> userIdResult = authService.extractUserIdFromToken(token);
        if (!userIdResult.isSuccess()) {
            return handleServiceResult(userIdResult);
        }
        
        Long userId = userIdResult.getData();
        
        // Need to call storage service to get statistics
        // Temporarily return mock data
        return success("Get storage statistics successful", Map.of(
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
     * GET /api/me/activity-log?page=1&per_page=20&operation=upload
     * Authorization: Bearer <token>
     */
    @GetMapping(path = "/api/me/activity-log")
    public ApiResponseWrapper getActivityLog(HttpServletRequest request) {
        System.out.println("CALLED: " + this.getClass().getSimpleName() + "." +
                Thread.currentThread().getStackTrace()[1].getMethodName() + "()");
        System.out.println("REQUEST_PARAMS: " + request.toString());
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            return error(401, "No authentication token provided");
        }
        
        // Get query parameters
        SearchCriteria criteria = parseSearchCriteria(request);
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
        
        activityItem.put("ip_address", getClientIP(request));
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
        
        return success("Get activity log successful", responseData);
    }
    
    // ============ Helper Methods ============
    
    /**
     * Extract JWT token from request header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }
} 