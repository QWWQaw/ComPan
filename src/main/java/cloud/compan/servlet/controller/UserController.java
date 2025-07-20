package cloud.compan.servlet.controller;

import cloud.compan.servlet.annotations.*;
import cloud.compan.servlet.model.User;
import cloud.compan.servlet.service.UserService;
import cloud.compan.servlet.dto.*;
import cloud.compan.servlet.dto.mapper.UserMapper;
import cloud.compan.servlet.web.response.ApiResponseWrapper;
import com.google.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.List;

/**
 * 用户控制器
 * 处理用户相关的HTTP请求，包括认证、用户管理等功能
 */
@Controller("/api/users")
@ResponseBody
public class UserController extends BaseController {
    
    @Inject
    private UserService userService;
    
    @Inject
    private UserMapper userMapper;
    
    /**
     * 用户注册
     * POST /api/users/register
     */
    @PostMapping(path = "/register")
    public ApiResponseWrapper register(@RequestBody Map<String, Object> requestData) {
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
        
        ServiceResult<User> result = userService.register(username, email, password);
        
        if (result.isSuccess()) {
            UserDTO userDTO = userMapper.toDTO(result.getData());
            return created("注册成功", userDTO);
        } else {
            return handleServiceResult(result);
        }
    }
    
    /**
     * 用户登录
     * POST /api/users/login
     */
    @PostMapping(path = "/login")
    public ApiResponseWrapper login(@RequestBody Map<String, Object> requestData) {
        String username = (String) requestData.get("username");
        String password = (String) requestData.get("password");
        
        // 参数验证
        requireNonEmpty(username, "username");
        requireNonEmpty(password, "password");
        
        ServiceResult<LoginResultDTO> result = userService.login(username, password);
        return handleServiceResult(result);
    }
    
    /**
     * 获取用户信息
     * GET /api/users/{id}
     */
    @GetMapping(path = "/{id}")
    public ApiResponseWrapper getUserById(@PathVariable Long id) {
        requireNonNull(id, "id");
        
        ServiceResult<User> result = userService.findById(id);
        
        if (result.isSuccess()) {
            UserDTO userDTO = userMapper.toDTO(result.getData());
            return success("获取用户信息成功", userDTO);
        } else {
            return handleServiceResult(result);
        }
    }
    
    /**
     * 更新用户信息
     * PUT /api/users/{id}
     */
    @PutMapping(path = "/{id}")
    public ApiResponseWrapper updateUser(@PathVariable Long id, @RequestBody Map<String, Object> requestData) {
        requireNonNull(id, "id");
        
        String username = (String) requestData.get("username");
        String email = (String) requestData.get("email");
        
        // 参数验证
        if (username != null) {
            validateStringLength(username, "username", 3, 50);
        }
        if (email != null) {
            validateEmail(email, "email");
        }
        
        ServiceResult<User> result = userService.updateProfile(id, username, email);
        
        if (result.isSuccess()) {
            UserDTO userDTO = userMapper.toDTO(result.getData());
            return success("用户信息更新成功", userDTO);
        } else {
            return handleServiceResult(result);
        }
    }
    
    /**
     * 删除用户
     * DELETE /api/users/{id}
     */
    @DeleteMapping(path = "/{id}")
    public ApiResponseWrapper deleteUser(@PathVariable Long id) {
        requireNonNull(id, "id");
        
        ServiceResult<Boolean> result = userService.deleteById(id);
        return handleServiceResult(result);
    }
    
    /**
     * 获取用户列表（分页）
     * GET /api/users?page=1&size=20
     */
    @GetMapping
    public ApiResponseWrapper getUsers(HttpServletRequest request) {
        SearchCriteria criteria = parseSearchCriteria(request);
        
        ServiceResult<PageResultDTO<User>> result = userService.findPageByCriteria(criteria);
        
        if (result.isSuccess()) {
            PageResultDTO<User> pageResult = result.getData();
            PageResultDTO<UserDTO> userDTOPageResult = convertPageResult(pageResult);
            return success("查询用户列表成功", userDTOPageResult);
        } else {
            return handleServiceResult(result);
        }
    }
    
    /**
     * 修改密码
     * POST /api/users/{id}/change-password
     */
    @PutMapping(path = "/{id}/password")
    public ApiResponseWrapper changePassword(@PathVariable Long id, @RequestBody Map<String, Object> requestData) {
        requireNonNull(id, "id");
        
        String oldPassword = (String) requestData.get("old_password");
        String newPassword = (String) requestData.get("new_password");
        
        // 参数验证
        requireNonEmpty(oldPassword, "old_password");
        requireNonEmpty(newPassword, "new_password");
        validateStringLength(newPassword, "new_password", 6, 100);
        
        ServiceResult<Boolean> result = userService.changePassword(id, oldPassword, newPassword);
        return handleServiceResult(result);
    }
    
    /**
     * 获取存储统计信息
     * GET /api/v1/users/{id}/storage
     */
    @GetMapping(path = "/{id}/storage")
    public ApiResponseWrapper getStorageStats(@PathVariable Long id) {
        requireNonNull(id, "id");
        
        ServiceResult<StorageStatsDTO> result = userService.getStorageStats(id);
        return handleServiceResult(result);
    }
    
    /**
     * 检查用户名是否可用
     * GET /api/users/check-username
     */
    @GetMapping(path = "/check-username")
    public ApiResponseWrapper checkUsername(@RequestParam String username) {
        requireNonEmpty(username, "username");
        validateStringLength(username, "username", 3, 50);
        
        ServiceResult<Boolean> result = userService.existsByUsername(username);
        
        if (result.isSuccess()) {
            boolean exists = result.getData();
            Map<String, Object> data = Map.of(
                "username", username,
                "available", !exists
            );
            return success(exists ? "用户名已被使用" : "用户名可用", data);
        } else {
            return handleServiceResult(result);
        }
    }
    
    /**
     * 检查邮箱是否可用
     * GET /api/users/check-email
     */
    @GetMapping(path = "/check-email")
    public ApiResponseWrapper checkEmail(@RequestParam String email) {
        requireNonEmpty(email, "email");
        validateEmail(email, "email");
        
        ServiceResult<Boolean> result = userService.existsByEmail(email);
        
        if (result.isSuccess()) {
            boolean exists = result.getData();
            Map<String, Object> data = Map.of(
                "email", email,
                "available", !exists
            );
            return success(exists ? "邮箱已被使用" : "邮箱可用", data);
        } else {
            return handleServiceResult(result);
        }
    }
    
    /**
     * 转换分页结果为DTO
     */
    private PageResultDTO<UserDTO> convertPageResult(PageResultDTO<User> userPageResult) {
        if (userPageResult == null || userPageResult.getContent() == null) {
            return new PageResultDTO<>();
        }
        
        List<UserDTO> userDTOs = userPageResult.getContent().stream()
            .map(UserMapper::toDTO)
            .collect(java.util.stream.Collectors.toList());
            
        return new PageResultDTO<>(
            userDTOs,
            userPageResult.getTotal(),
            userPageResult.getPage(),
            userPageResult.getSize()
        );
    }
} 
