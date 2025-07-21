package cloud.compan.servlet.service.impl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Service;
import cloud.compan.servlet.dto.LoginResultDTO;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.UserDTO;
import cloud.compan.servlet.model.User;
import cloud.compan.servlet.repository.UserRepository;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.utils.HashUtil;
import cloud.compan.servlet.utils.JwtUtil;

/**
 * 认证服务实现类
 * 提供完整的用户认证和授权功能
 */
@Service
@Singleton
public class AuthServiceImpl implements AuthService {

    @Inject
    private UserRepository userRepository;
    
    @Inject
    private HashUtil hashUtil;
    
    @Inject
    private JwtUtil jwtUtil;
    
    // 简单的内存缓存，用于存储登录尝试记录（生产环境应使用Redis）
    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> lastLoginAttempt = new ConcurrentHashMap<>();

    @Override
    public ServiceResult<UserDTO> register(String username, String email, String password) {
        try {
            // 1. 验证注册数据
            ServiceResult<Void> validationResult = validateRegistrationData(username, email, password);
            if (!validationResult.isSuccess()) {
                return ServiceResult.error(validationResult.getMessage(), validationResult.getErrorCode());
            }
            
            // 2. 检查用户名是否已存在
            if (userRepository.existsByUsername(username)) {
                return ServiceResult.error("用户名已存在", "USERNAME_EXISTS");
            }
            
            // 3. 检查邮箱是否已存在
            if (userRepository.findByEmail(email) != null) {
                return ServiceResult.error("邮箱已被注册", "EMAIL_EXISTS");
            }
            
            // 4. 创建新用户
            User newUser = userRepository.registerNewUser(username, email, password);
            
            // 5. 转换为DTO并返回
            UserDTO userDTO = convertToDTO(newUser);
            return ServiceResult.success(userDTO, "注册成功");
            
        } catch (Exception e) {
            return ServiceResult.error("注册失败: " + e.getMessage(), "REGISTRATION_FAILED");
        }
    }

    @Override
    public ServiceResult<Void> validateRegistrationData(String username, String email, String password) {
        // 用户名验证
        if (username == null || username.trim().isEmpty()) {
            return ServiceResult.error("用户名不能为空", "USERNAME_EMPTY");
        }
        if (username.length() < 3 || username.length() > 50) {
            return ServiceResult.error("用户名长度必须在3-50个字符之间", "USERNAME_LENGTH_INVALID");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return ServiceResult.error("用户名只能包含字母、数字和下划线", "USERNAME_FORMAT_INVALID");
        }
        
        // 邮箱验证
        if (email == null || email.trim().isEmpty()) {
            return ServiceResult.error("邮箱不能为空", "EMAIL_EMPTY");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ServiceResult.error("邮箱格式不正确", "EMAIL_FORMAT_INVALID");
        }
        
        // 密码验证
        ServiceResult<Void> passwordResult = validatePasswordStrength(password);
        if (!passwordResult.isSuccess()) {
            return passwordResult;
        }
        
        return ServiceResult.success(null, "验证通过");
    }

    @Override
    public ServiceResult<LoginResultDTO> login(String username, String password) {
        try {
            // 1. 检查登录频率限制
            ServiceResult<Boolean> rateLimitResult = checkLoginRateLimit(username, "127.0.0.1");
            if (rateLimitResult.isSuccess() && rateLimitResult.getData()) {
                return ServiceResult.error("登录尝试过于频繁，请稍后再试", "RATE_LIMIT_EXCEEDED");
            }
            
            // 2. 查找用户
            User user = userRepository.findByUsername(username);
            if (user == null) {
                // 尝试通过邮箱查找
                user = userRepository.findByEmail(username);
            }
            
            if (user == null) {
                recordLoginAttempt(username, false, "127.0.0.1", "Unknown");
                return ServiceResult.error("用户名或密码错误", "INVALID_CREDENTIALS");
            }
            
            // 3. 验证密码
            if (!hashUtil.verifyPassword(password, user.getPasswordHash())) {
                recordLoginAttempt(username, false, "127.0.0.1", "Unknown");
                return ServiceResult.error("用户名或密码错误", "INVALID_CREDENTIALS");
            }
            
            // 4. 检查账户状态
            ServiceResult<String> statusResult = checkAccountStatus(user.getUserId());
            if (!statusResult.isSuccess() || !"ACTIVE".equals(statusResult.getData())) {
                return ServiceResult.error("账户已被禁用", "ACCOUNT_DISABLED");
            }
            
            // 5. 生成JWT token
            String token = jwtUtil.generateToken(user.getUserId().toString());
            
            // 6. 记录成功登录
            recordLoginAttempt(username, true, "127.0.0.1", "Unknown");
            
            // 7. 更新最后登录时间
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            // 8. 构造返回结果
            UserDTO userDTO = convertToDTO(user);
            LoginResultDTO loginResult = new LoginResultDTO(userDTO, token, 3600);
            
            return ServiceResult.success(loginResult, "登录成功");
            
        } catch (Exception e) {
            return ServiceResult.error("登录失败: " + e.getMessage(), "LOGIN_FAILED");
        }
    }

    @Override
    public ServiceResult<Void> logout(String token) {
        try {
            // 在实际应用中，这里应该将token加入黑名单
            // 当前简化实现，直接返回成功
            return ServiceResult.success(null, "登出成功");
        } catch (Exception e) {
            return ServiceResult.error("登出失败: " + e.getMessage(), "LOGOUT_FAILED");
        }
    }

    @Override
    public ServiceResult<LoginResultDTO> refreshToken(String token) {
        try {
            // 1. 验证当前token
            ServiceResult<UserDTO> validationResult = validateToken(token);
            if (!validationResult.isSuccess()) {
                return ServiceResult.error("Token无效", "INVALID_TOKEN");
            }
            
            // 2. 生成新token
            String newToken = jwtUtil.generateToken(validationResult.getData().getUserId().toString());
            
            // 3. 构造返回结果
            LoginResultDTO loginResult = new LoginResultDTO(validationResult.getData(), newToken, 3600);
            
            return ServiceResult.success(loginResult, "Token刷新成功");
            
        } catch (Exception e) {
            return ServiceResult.error("Token刷新失败: " + e.getMessage(), "TOKEN_REFRESH_FAILED");
        }
    }

    @Override
    public ServiceResult<UserDTO> validateToken(String token) {
        try {
            // 1. 从token中提取用户ID
            ServiceResult<Long> userIdResult = extractUserIdFromToken(token);
            if (!userIdResult.isSuccess()) {
                return ServiceResult.error("Token无效", "INVALID_TOKEN");
            }
            
            // 2. 查找用户
            Optional<User> userOpt = userRepository.findById(userIdResult.getData());
            if (userOpt.isEmpty()) {
                return ServiceResult.error("用户不存在", "USER_NOT_FOUND");
            }
            User user = userOpt.get();
            
            // 3. 检查账户状态
            ServiceResult<String> statusResult = checkAccountStatus(user.getUserId());
            if (!statusResult.isSuccess() || !"ACTIVE".equals(statusResult.getData())) {
                return ServiceResult.error("账户已被禁用", "ACCOUNT_DISABLED");
            }
            
            // 4. 转换为DTO并返回
            UserDTO userDTO = convertToDTO(user);
            return ServiceResult.success(userDTO, "Token验证成功");
            
        } catch (Exception e) {
            return ServiceResult.error("Token验证失败: " + e.getMessage(), "TOKEN_VALIDATION_FAILED");
        }
    }

    @Override
    public ServiceResult<Long> extractUserIdFromToken(String token) {
        try {
            // 使用JWT工具类解析token
            var claims = jwtUtil.validateToken(token);
            String userIdStr = claims.getSubject();
            Long userId = Long.parseLong(userIdStr);
            return ServiceResult.success(userId, "用户ID提取成功");
        } catch (Exception e) {
            return ServiceResult.error("Token解析失败: " + e.getMessage(), "TOKEN_PARSE_FAILED");
        }
    }

    @Override
    public ServiceResult<Boolean> isTokenExpired(String token) {
        try {
            var claims = jwtUtil.validateToken(token);
            return ServiceResult.success(claims.getExpiration().before(new java.util.Date()), "Token状态检查完成");
        } catch (Exception e) {
            return ServiceResult.success(true, "Token已过期");
        }
    }

    @Override
    public ServiceResult<Void> changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            // 1. 查找用户
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ServiceResult.error("用户不存在", "USER_NOT_FOUND");
            }
            User user = userOpt.get();
            
            // 2. 验证旧密码
            if (!hashUtil.verifyPassword(oldPassword, user.getPasswordHash())) {
                return ServiceResult.error("旧密码错误", "OLD_PASSWORD_INCORRECT");
            }
            
            // 3. 验证新密码强度
            ServiceResult<Void> passwordResult = validatePasswordStrength(newPassword);
            if (!passwordResult.isSuccess()) {
                return passwordResult;
            }
            
            // 4. 更新密码
            String newPasswordHash = hashUtil.hashPassword(newPassword);
            user.setPasswordHash(newPasswordHash);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            return ServiceResult.success(null, "密码修改成功");
            
        } catch (Exception e) {
            return ServiceResult.error("密码修改失败: " + e.getMessage(), "PASSWORD_CHANGE_FAILED");
        }
    }

    @Override
    public ServiceResult<Void> resetPassword(Long userId, String newPassword) {
        try {
            // 1. 查找用户
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ServiceResult.error("用户不存在", "USER_NOT_FOUND");
            }
            User user = userOpt.get();
            
            // 2. 验证新密码强度
            ServiceResult<Void> passwordResult = validatePasswordStrength(newPassword);
            if (!passwordResult.isSuccess()) {
                return passwordResult;
            }
            
            // 3. 更新密码
            String newPasswordHash = hashUtil.hashPassword(newPassword);
            user.setPasswordHash(newPasswordHash);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            return ServiceResult.success(null, "密码重置成功");
            
        } catch (Exception e) {
            return ServiceResult.error("密码重置失败: " + e.getMessage(), "PASSWORD_RESET_FAILED");
        }
    }

    @Override
    public ServiceResult<Void> validatePasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return ServiceResult.error("密码长度至少6位", "PASSWORD_TOO_SHORT");
        }
        if (password.length() > 100) {
            return ServiceResult.error("密码长度不能超过100位", "PASSWORD_TOO_LONG");
        }
        // 可以添加更多密码强度检查，如包含大小写字母、数字、特殊字符等
        return ServiceResult.success(null, "密码强度验证通过");
    }

    @Override
    public ServiceResult<String> checkAccountStatus(Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ServiceResult.error("用户不存在", "USER_NOT_FOUND");
            }
            // 这里可以根据用户的状态字段返回不同的状态
            // 当前简化实现，所有用户都是活跃状态
            return ServiceResult.success("ACTIVE", "账户状态正常");
        } catch (Exception e) {
            return ServiceResult.error("账户状态检查失败: " + e.getMessage(), "STATUS_CHECK_FAILED");
        }
    }

    @Override
    public ServiceResult<UserDTO> getCurrentUser(String token) {
        return validateToken(token);
    }

    @Override
    public ServiceResult<Void> recordLoginAttempt(String username, boolean success, String ipAddress, String userAgent) {
        try {
            String key = username + "_" + ipAddress;
            long currentTime = System.currentTimeMillis();
            
            if (!success) {
                // 记录失败尝试
                loginAttempts.put(key, loginAttempts.getOrDefault(key, 0) + 1);
                lastLoginAttempt.put(key, currentTime);
            } else {
                // 登录成功，清除失败记录
                loginAttempts.remove(key);
                lastLoginAttempt.remove(key);
            }
            
            return ServiceResult.success(null, "登录尝试记录成功");
        } catch (Exception e) {
            return ServiceResult.error("记录登录尝试失败: " + e.getMessage(), "LOGIN_ATTEMPT_RECORD_FAILED");
        }
    }

    @Override
    public ServiceResult<Boolean> checkLoginRateLimit(String username, String ipAddress) {
        try {
            String key = username + "_" + ipAddress;
            long currentTime = System.currentTimeMillis();
            
            // 检查是否在限制时间内
            Long lastAttempt = lastLoginAttempt.get(key);
            if (lastAttempt != null && currentTime - lastAttempt < 300000) { // 5分钟内
                int attempts = loginAttempts.getOrDefault(key, 0);
                if (attempts >= 5) { // 5次失败后限制
                    return ServiceResult.success(true, "登录频率超限");
                }
            } else {
                // 超过限制时间，清除记录
                loginAttempts.remove(key);
                lastLoginAttempt.remove(key);
            }
            
            return ServiceResult.success(false, "登录频率检查通过");
        } catch (Exception e) {
            return ServiceResult.error("登录频率检查失败: " + e.getMessage(), "RATE_LIMIT_CHECK_FAILED");
        }
    }
    
    /**
     * 将User实体转换为UserDTO
     */
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .storageLimit(user.getStorageLimit())
                .storageUsed(user.getStorageUsed())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
} 