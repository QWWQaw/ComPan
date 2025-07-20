package cloud.compan.servlet.service.impl;

import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.LoginResultDTO;
import cloud.compan.servlet.dto.UserDTO;
import com.google.inject.Singleton;

/**
 * 认证服务实现类
 * 简化实现，主要用于测试
 */
@Singleton
public class AuthServiceImpl implements AuthService {

    @Override
    public ServiceResult<UserDTO> register(String username, String email, String password) {
        // 简化实现，返回成功结果（不构造复杂对象）
        return ServiceResult.success(null, "注册成功");
    }

    @Override
    public ServiceResult<Void> validateRegistrationData(String username, String email, String password) {
        return ServiceResult.success(null, "验证通过");
    }

    @Override
    public ServiceResult<LoginResultDTO> login(String username, String password) {
        // 简化实现，返回成功但不构造复杂对象
        return ServiceResult.success(null, "登录成功");
    }

    @Override
    public ServiceResult<Void> logout(String token) {
        return ServiceResult.success(null, "登出成功");
    }

    @Override
    public ServiceResult<LoginResultDTO> refreshToken(String token) {
        return ServiceResult.success(null, "Token刷新成功");
    }

    @Override
    public ServiceResult<UserDTO> validateToken(String token) {
        return ServiceResult.success(null, "Token验证成功");
    }

    @Override
    public ServiceResult<Long> extractUserIdFromToken(String token) {
        return ServiceResult.success(1L, "用户ID提取成功");
    }

    @Override
    public ServiceResult<Boolean> isTokenExpired(String token) {
        return ServiceResult.success(false, "Token未过期");
    }

    @Override
    public ServiceResult<Void> changePassword(Long userId, String oldPassword, String newPassword) {
        return ServiceResult.success(null, "密码修改成功");
    }

    @Override
    public ServiceResult<Void> resetPassword(Long userId, String newPassword) {
        return ServiceResult.success(null, "密码重置成功");
    }

    @Override
    public ServiceResult<Void> validatePasswordStrength(String password) {
        return ServiceResult.success(null, "密码强度验证通过");
    }

    @Override
    public ServiceResult<String> checkAccountStatus(Long userId) {
        return ServiceResult.success("ACTIVE", "账户状态正常");
    }

    @Override
    public ServiceResult<UserDTO> getCurrentUser(String token) {
        return ServiceResult.success(null, "获取当前用户信息成功");
    }

    @Override
    public ServiceResult<Void> recordLoginAttempt(String username, boolean success, String ipAddress, String userAgent) {
        return ServiceResult.success(null, "登录尝试记录成功");
    }

    @Override
    public ServiceResult<Boolean> checkLoginRateLimit(String username, String ipAddress) {
        // 默认不限制
        return ServiceResult.success(false, "登录频率检查通过");
    }
} 