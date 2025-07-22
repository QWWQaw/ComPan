package cloud.compan.servlet.service.impl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    // 添加 token 黑名单
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

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
        if (username.length() < 3 || username.length() > 15) {
            return ServiceResult.error("用户名长度必须在3-15个字符之间", "USERNAME_LENGTH_INVALID");
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
    public ServiceResult<LoginResultDTO> login(String username, String password, jakarta.servlet.http.HttpServletRequest request) {
        try {
            // 获取真实的客户端信息
            String clientIP = getClientIP(request);
            String userAgent = getUserAgent(request);
            
            // 1. 检查登录频率限制
            ServiceResult<Boolean> rateLimitResult = checkLoginRateLimit(username, clientIP);
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
                recordLoginAttempt(username, false, clientIP, userAgent);
                return ServiceResult.error("用户名或密码错误", "INVALID_CREDENTIALS");
            }
            
            // 3. 验证密码
            if (!hashUtil.verifyPassword(password, user.getPasswordHash())) {
                recordLoginAttempt(username, false, clientIP, userAgent);
                return ServiceResult.error("用户名或密码错误", "INVALID_CREDENTIALS");
            }
            
            // 4. 生成JWT token
            String token = jwtUtil.generateToken(user.getUserId().toString());
            
            // 6. 记录成功登录
            recordLoginAttempt(username, true, clientIP, userAgent);
            
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
            // 1. 验证 token 格式
            if (token == null || token.trim().isEmpty()) {
                return ServiceResult.error("Token不能为空", "INVALID_TOKEN");
            }
            
            // 2. 验证 token 有效性
            try {
                var claims = jwtUtil.validateToken(token);
                if (claims == null) {
                    return ServiceResult.error("Token无效", "INVALID_TOKEN");
                }
            } catch (Exception e) {
                return ServiceResult.error("Token验证失败", "TOKEN_VALIDATION_FAILED");
            }
            
            // 3. 将 token 加入黑名单
            blacklistedTokens.add(token);
            
            return ServiceResult.success(null, "登出成功");
        } catch (Exception e) {
            return ServiceResult.error("登出失败: " + e.getMessage(), "LOGOUT_FAILED");
        }
    }

    /**
     * 检查 token 是否在黑名单中
     * @param token JWT token
     * @return 如果在黑名单中返回 true
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * 清理过期的黑名单 token（可选，用于内存管理）
     */
    public void cleanupExpiredBlacklistedTokens() {
        // 这里可以实现定期清理逻辑
        // 由于 JWT 有过期时间，过期的 token 即使不在黑名单中也无效
        // 所以这里主要是为了内存管理
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
            // 1. 检查 token 是否在黑名单中
            if (isTokenBlacklisted(token)) {
                return ServiceResult.error("Token已失效（已登出）", "TOKEN_BLACKLISTED");
            }
            
            // 2. 从token中提取用户ID
            ServiceResult<Long> userIdResult = extractUserIdFromToken(token);
            if (!userIdResult.isSuccess()) {
                return ServiceResult.error("Token无效", "INVALID_TOKEN");
            }
            
            // 3. 查找用户
            Optional<User> userOpt = userRepository.findById(userIdResult.getData());
            if (userOpt.isEmpty()) {
                return ServiceResult.error("用户不存在", "USER_NOT_FOUND");
            }
            User user = userOpt.get();
            
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
        if (password.length() > 50) {
            return ServiceResult.error("密码长度不能超过50位", "PASSWORD_TOO_LONG");
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
    public ServiceResult<UserDTO> updateProfile(Long userId, String displayName, String email) {
        try {
            // 1. 查找用户
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ServiceResult.error("用户不存在", "USER_NOT_FOUND");
            }
            
            User user = userOpt.get();
            
            // 2. 更新字段
            if (displayName != null && !displayName.trim().isEmpty()) {
                // 检查用户名是否已被其他用户使用
                User existingUser = userRepository.findByUsername(displayName);
                if (existingUser != null && !existingUser.getUserId().equals(userId)) {
                    return ServiceResult.error("用户名已被使用", "USERNAME_EXISTS");
                }
                user.setUsername(displayName);
            }
            
            if (email != null && !email.trim().isEmpty()) {
                // 检查邮箱是否已被其他用户使用
                User existingUser = userRepository.findByEmail(email);
                if (existingUser != null && !existingUser.getUserId().equals(userId)) {
                    return ServiceResult.error("邮箱已被使用", "EMAIL_EXISTS");
                }
                user.setEmail(email);
            }
            
            // 3. 保存更新
            user.setUpdatedAt(LocalDateTime.now());
            User updatedUser = userRepository.save(user);
            
            // 4. 转换为DTO并返回
            UserDTO userDTO = convertToDTO(updatedUser);
            return ServiceResult.success(userDTO, "用户信息更新成功");
            
        } catch (Exception e) {
            return ServiceResult.error("更新用户信息失败: " + e.getMessage(), "UPDATE_PROFILE_FAILED");
        }
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
                    return ServiceResult.success(true, "Login rate limit exceeded");
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
        UserDTO dto = new UserDTO(user.getUserId(), user.getUsername(), user.getEmail());
        dto.setStorageLimit(user.getStorageLimit());
        dto.setStorageUsed(user.getStorageUsed());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    /**
     * 获取客户端真实IP地址
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    private String getClientIP(jakarta.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 如果是多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip != null ? ip : "unknown";
    }

    /**
     * 获取客户端User-Agent
     * @param request HTTP请求对象
     * @return User-Agent字符串
     */
    private String getUserAgent(jakarta.servlet.http.HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unknown";
    }
} 