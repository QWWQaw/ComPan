package cloud.compan.servlet.controller;

import cloud.compan.servlet.annotations.*;
import cloud.compan.servlet.service.UserService;
import cloud.compan.servlet.dto.*;
import cloud.compan.servlet.dto.mapper.UserMapper;
import cloud.compan.servlet.model.User;
import com.google.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户控制�?
 * 提供用户管理相关的REST API接口
 */
@Controller
@RequestMapping(path = "/api/users")
public class UserController implements BaseController {
    
    @Inject
    private UserService userService;
    
    @Inject
    private UserMapper userMapper;
    
    /**
     * 用户注册
     * POST /api/users/register
     */
    @PostMapping(path = "/register")
    @ResponseBody
    public ApiResponse<Object> register(@RequestBody java.util.Map<String, Object> requestData, 
                                      HttpServletResponse response) {
        try {
            String username = (String) requestData.get("username");
            String email = (String) requestData.get("email");
            String password = (String) requestData.get("password");
            
            if (username == null || username.trim().isEmpty()) {
                return badRequest("用户名不能为空");
            }
            if (email == null || email.trim().isEmpty()) {
                return badRequest("邮箱不能为空");
            }
            if (password == null || password.length() < 6) {
                return badRequest("密码长度不能少于6位");
            }
            
            ServiceResult<User> result = userService.register(username, email, password);
            
            if (result.isSuccess()) {
                setResponseStatus(response, 201);
                UserDTO userDTO = UserMapper.toDTO(result.getData());
                return created(userDTO);
            } else {
                return handleServiceError(result);
            }
            
        } catch (Exception e) {
            return internalError("注册失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户登录
     * POST /api/users/login
     */
    @PostMapping(path = "/login")
    @ResponseBody
    public ApiResponse<Object> login(@RequestBody java.util.Map<String, Object> requestData) {
        try {
            String username = (String) requestData.get("username");
            String password = (String) requestData.get("password");
            
            if (username == null || username.trim().isEmpty()) {
                return badRequest("用户名不能为空");
            }
            if (password == null || password.isEmpty()) {
                return badRequest("密码不能为空");
            }
            
            ServiceResult<LoginResultDTO> result = userService.login(username, password);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return handleServiceError(result);
            }
            
        } catch (Exception e) {
            return internalError("登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户信息
     * GET /api/users/{id}
     */
    @GetMapping(path = "/{id}")
    @ResponseBody
    public ApiResponse<Object> getUserById(@PathVariable Long id) {
        try {
            ServiceResult<User> result = userService.findById(id);
            
            if (result.isSuccess()) {
                UserDTO userDTO = UserMapper.toDTO(result.getData());
                return success(userDTO);
            } else {
                return handleServiceError(result);
            }
            
        } catch (Exception e) {
            return internalError("获取用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户信息
     * PUT /api/users/{id}
     */
    @PutMapping(path = "/{id}")
    @ResponseBody
    public ApiResponse<Object> updateUser(@PathVariable Long id, 
                                        @RequestBody java.util.Map<String, Object> requestData) {
        try {
            String username = (String) requestData.get("username");
            String email = (String) requestData.get("email");
            
            ServiceResult<User> result = userService.updateProfile(id, username, email);
            
            if (result.isSuccess()) {
                UserDTO userDTO = UserMapper.toDTO(result.getData());
                return success(userDTO);
            } else {
                return handleServiceError(result);
            }
            
        } catch (Exception e) {
            return internalError("更新用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除用户
     * DELETE /api/users/{id}
     */
    @DeleteMapping(path = "/{id}")
    @ResponseBody
    public ApiResponse<Object> deleteUser(@PathVariable Long id) {
        try {
            ServiceResult<Boolean> result = userService.deleteById(id);
            
            if (result.isSuccess()) {
                return success("删除用户成功");
            } else {
                return handleServiceError(result);
            }
            
        } catch (Exception e) {
            return internalError("删除用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户列表（分页）
     * GET /api/users?page=1&size=20
     */
    @GetMapping(path = "")
    @ResponseBody
    public ApiResponse<Object> getUsers(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "20") Integer size) {
        try {
            ServiceResult<PageResultDTO<User>> result = userService.findPage(page, size);
            
            if (result.isSuccess()) {
                PageResultDTO<User> pageResult = result.getData();
                PageResultDTO<UserDTO> dtoPageResult = convertPageResult(pageResult);
                return success(dtoPageResult);
            } else {
                return handleServiceError(result);
            }
            
        } catch (Exception e) {
            return internalError("获取用户列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 修改密码
     * POST /api/users/{id}/change-password
     */
    @PostMapping(path = "/{id}/change-password")
    @ResponseBody
    public ApiResponse<Object> changePassword(@PathVariable Long id,
                                            @RequestBody java.util.Map<String, Object> requestData) {
        try {
            String oldPassword = (String) requestData.get("oldPassword");
            String newPassword = (String) requestData.get("newPassword");
            
            if (oldPassword == null || oldPassword.isEmpty()) {
                return badRequest("当前密码不能为空");
            }
            if (newPassword == null || newPassword.length() < 6) {
                return badRequest("新密码长度不能少于6位");
            }
            
            ServiceResult<Boolean> result = userService.changePassword(id, oldPassword, newPassword);
            
            if (result.isSuccess()) {
                return success("修改密码成功");
            } else {
                return handleServiceError(result);
            }
            
        } catch (Exception e) {
            return internalError("修改密码失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户存储统计
     * GET /api/users/{id}/storage-stats
     */
    @GetMapping(path = "/{id}/storage-stats")
    @ResponseBody
    public ApiResponse<Object> getStorageStats(@PathVariable Long id) {
        try {
            ServiceResult<StorageStatsDTO> result = userService.getStorageStats(id);
            
            if (result.isSuccess()) {
                return success(result.getData());
            } else {
                return handleServiceError(result);
            }
            
        } catch (Exception e) {
            return internalError("获取存储统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理服务层错�?
     */
    private ApiResponse<Object> handleServiceError(ServiceResult<?> result) {
        String errorCode = result.getErrorCode();
        
        if ("USER_NOT_FOUND".equals(errorCode)) {
            return notFound(result.getMessage());
        } else if ("USERNAME_EXISTS".equals(errorCode) || "EMAIL_EXISTS".equals(errorCode)) {
            return conflict(result.getMessage());
        } else if ("INVALID_CREDENTIALS".equals(errorCode) || "INVALID_OLD_PASSWORD".equals(errorCode)) {
            return unauthorized(result.getMessage());
        } else if ("STORAGE_LIMIT_EXCEEDED".equals(errorCode)) {
            return badRequest(result.getMessage());
        } else {
            return badRequest(result.getMessage());
        }
    }
    
    /**
     * 转换分页结果为DTO
     */
    private PageResultDTO<UserDTO> convertPageResult(PageResultDTO<User> userPageResult) {
        if (userPageResult == null || userPageResult.getContent() == null) {
            return new PageResultDTO<>();
        }
        
        java.util.List<UserDTO> userDTOs = userPageResult.getContent().stream()
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
