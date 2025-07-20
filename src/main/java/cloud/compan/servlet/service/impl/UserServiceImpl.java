package cloud.compan.servlet.service.impl;

import cloud.compan.servlet.service.UserService;
import cloud.compan.servlet.model.User;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.LoginResultDTO;
import cloud.compan.servlet.dto.StorageStatsDTO;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.dto.SearchCriteria;
import com.google.inject.Singleton;

/**
 * 用户服务实现类
 * 简化实现，主要用于测试
 */
@Singleton
public class UserServiceImpl implements UserService {

    @Override
    public ServiceResult<User> register(String username, String email, String password) {
        return ServiceResult.success(null, "用户注册成功");
    }

    @Override
    public ServiceResult<LoginResultDTO> login(String username, String password) {
        return ServiceResult.success(null, "用户登录成功");
    }

    @Override
    public ServiceResult<User> findByUsername(String username) {
        return ServiceResult.success(null, "根据用户名查找用户成功");
    }

    @Override
    public ServiceResult<User> findByEmail(String email) {
        return ServiceResult.success(null, "根据邮箱查找用户成功");
    }

    @Override
    public ServiceResult<Boolean> existsByUsername(String username) {
        return ServiceResult.success(false, "用户名不存在");
    }

    @Override
    public ServiceResult<Boolean> existsByEmail(String email) {
        return ServiceResult.success(false, "邮箱不存在");
    }

    @Override
    public ServiceResult<User> updateProfile(Long userId, String username, String email) {
        return ServiceResult.success(null, "用户信息更新成功");
    }

    @Override
    public ServiceResult<Boolean> changePassword(Long userId, String oldPassword, String newPassword) {
        return ServiceResult.success(true, "密码修改成功");
    }

    @Override
    public ServiceResult<Boolean> resetPassword(Long userId, String newPassword) {
        return ServiceResult.success(true, "密码重置成功");
    }

    @Override
    public ServiceResult<StorageStatsDTO> getStorageStats(Long userId) {
        return ServiceResult.success(null, "获取存储统计成功");
    }

    @Override
    public ServiceResult<Boolean> updateStorageUsed(Long userId, long sizeChange) {
        return ServiceResult.success(true, "存储使用量更新成功");
    }

    @Override
    public ServiceResult<Boolean> checkStorageCapacity(Long userId, long requiredSize) {
        return ServiceResult.success(true, "存储容量检查通过");
    }

    @Override
    public ServiceResult<PageResultDTO<User>> searchUsers(String keyword, int page, int size) {
        return ServiceResult.success(null, "搜索用户成功");
    }

    @Override
    public ServiceResult<PageResultDTO<User>> getActiveUsers(int days, int page, int size) {
        return ServiceResult.success(null, "获取活跃用户成功");
    }

    @Override
    public ServiceResult<User> findById(Long userId) {
        return ServiceResult.success(null, "根据ID查找用户成功");
    }

    @Override
    public ServiceResult<Boolean> deleteById(Long userId) {
        return ServiceResult.success(true, "删除用户成功");
    }

    @Override
    public ServiceResult<PageResultDTO<User>> findPageByCriteria(SearchCriteria criteria) {
        return ServiceResult.success(null, "分页查询用户成功");
    }
} 