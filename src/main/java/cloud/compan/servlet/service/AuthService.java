package cloud.compan.servlet.service;

import cloud.compan.servlet.dto.LoginResultDTO;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.UserDTO;

/**
 * 认证服务接口
 * 
 */
public interface AuthService {
    
    // ============ 用户注册相关 ============
    
    /**
     * 用户注册
     * @param username 用户名
     * @param email 邮箱
     * @param password 密码
     * @return 注册结果，包含用户信息
     */
    ServiceResult<UserDTO> register(String username, String email, String password);
    
    /**
     * 验证注册信息
     * @param username 用户名
     * @param email 邮箱
     * @param password 密码
     * @return 验证结果
     */
    ServiceResult<Void> validateRegistrationData(String username, String email, String password);
    
    // ============ 用户登录相关 ============
    
    /**
     * 用户登录
     * @param username 用户名（或邮箱）
     * @param password 密码
     * @param request HTTP请求对象，用于获取客户端信息
     * @return 登录结果，包含token和用户信息
     */
    ServiceResult<LoginResultDTO> login(String username, String password, jakarta.servlet.http.HttpServletRequest request);
    
    /**
     * 用户登出
     * @param token 当前token
     * @return 登出结果
     */
    ServiceResult<Void> logout(String token);
    
    /**
     * 刷新token
     * @param token 当前token
     * @return 新的token信息
     */
    ServiceResult<LoginResultDTO> refreshToken(String token);
    
    // ============ Token管理相关 ============
    
    /**
     * 验证token有效性
     * @param token JWT token
     * @return 验证结果，包含用户信息
     */
    ServiceResult<UserDTO> validateToken(String token);
    
    /**
     * 从token中提取用户ID
     * @param token JWT token
     * @return 用户ID
     */
    ServiceResult<Long> extractUserIdFromToken(String token);
    
    /**
     * 检查token是否已过期
     * @param token JWT token
     * @return 是否过期
     */
    ServiceResult<Boolean> isTokenExpired(String token);
    
    // ============ 密码管理相关 ============
    
    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    ServiceResult<Void> changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 重置密码（管理员操作）
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 重置结果
     */
    ServiceResult<Void> resetPassword(Long userId, String newPassword);
    
    /**
     * 验证密码强度
     * @param password 密码
     * @return 验证结果
     */
    ServiceResult<Void> validatePasswordStrength(String password);
    
    // ============ 安全相关 ============
    
    /**
     * 记录登录尝试
     * @param username 用户名
     * @param success 是否成功
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 记录结果
     */
    ServiceResult<Void> recordLoginAttempt(String username, boolean success, String ipAddress, String userAgent);
    
    /**
     * 检查登录频率限制
     * @param username 用户名
     * @param ipAddress IP地址
     * @return 是否被限制
     */
    ServiceResult<Boolean> checkLoginRateLimit(String username, String ipAddress);
} 