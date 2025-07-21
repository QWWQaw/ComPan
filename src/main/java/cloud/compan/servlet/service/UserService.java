package cloud.compan.servlet.service;

import java.util.Map;
import cloud.compan.servlet.dto.LoginResultDTO;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.dto.SearchCriteria;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.StorageStatsDTO;
import cloud.compan.servlet.dto.UserDTO;
import cloud.compan.servlet.model.User;

/**
 * 用户服务接口
 * 定义用户相关的业务操作
 */
public interface UserService  {
    
    // ============ 用户认证相关 ============
    
    /**
     * 用户注册
     */
    ServiceResult<User> register(String username, String email, String password);
    
    /**
     * 用户登录
     */
    ServiceResult<LoginResultDTO> login(String username, String password);
    
    /**
     * 根据用户名查找用户
     */
    ServiceResult<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    ServiceResult<User> findByEmail(String email);
    
    /**
     * 检查用户名是否存在
     */
    ServiceResult<Boolean> existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    ServiceResult<Boolean> existsByEmail(String email);
    
    // ============ 用户信息管理 ============
    
    /**
     * 更新用户基本信息
     */
    ServiceResult<User> updateProfile(Long userId, String username, String email);
    
    /**
     * 修改密码
     */
    ServiceResult<Boolean> changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 重置密码
     */
    ServiceResult<Boolean> resetPassword(Long userId, String newPassword);
    
    // ============ 存储管理 ============
    
    /**
     * 获取存储统计信息
     */
    ServiceResult<StorageStatsDTO> getStorageStats(Long userId);
    
    /**
     * 更新存储使用量
     */
    ServiceResult<Boolean> updateStorageUsed(Long userId, long sizeChange);
    
    /**
     * 检查存储容量
     */
    ServiceResult<Boolean> checkStorageCapacity(Long userId, long requiredSize);
    
    // ============ 用户查询 ============
    
    /**
     * 获取活跃用户
     */
    ServiceResult<PageResultDTO<User>> getActiveUsers(int days, int page, int size);

    /**
     * 获取所有用户
     */
    ServiceResult<User> findById(Long userId);

    /**
     * 删除指定id用户
     */
    ServiceResult<Boolean> deleteById(Long userId);

    /**
     * 分页查询用户
     */
    ServiceResult<PageResultDTO<User>>  findPageByCriteria(SearchCriteria criteria);
    
    // ============ 用户管理功能 ============
    
    /**
     * 获取用户列表（分页）
     */
    ServiceResult<PageResultDTO<UserDTO>> getUsers(int page, int size, String search, String sortBy, String sortOrder);
    
    /**
     * 根据ID获取用户详情
     */
    ServiceResult<UserDTO> getUserById(Long userId);
    
    /**
     * 更新用户信息
     */
    ServiceResult<UserDTO> updateUser(Long userId, String username, String email, String role);
    
    /**
     * 搜索用户（返回DTO）
     */
    ServiceResult<PageResultDTO<UserDTO>> searchUsers(String keyword, int page, int size);
    
    /**
     * 获取用户角色
     */
    ServiceResult<String> getUserRole(Long userId);
    
    /**
     * 获取用户统计信息
     */
    ServiceResult<Map<String, Object>> getUserStats();
    
    /**
     * 禁用/启用用户
     */
    ServiceResult<Boolean> toggleUserStatus(Long userId, boolean enabled);
    
    /**
     * 批量删除用户
     */
    ServiceResult<Boolean> batchDeleteUsers(java.util.List<Long> userIds);
    
    /**
     * 获取用户活动日志
     */
    ServiceResult<PageResultDTO<Map<String, Object>>> getUserActivityLog(Long userId, int page, int size);
} 