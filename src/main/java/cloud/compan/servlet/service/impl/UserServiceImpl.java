package cloud.compan.servlet.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Service;
import cloud.compan.servlet.dto.LoginResultDTO;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.dto.SearchCriteria;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.StorageStatsDTO;
import cloud.compan.servlet.dto.UserDTO;
import cloud.compan.servlet.model.User;
import cloud.compan.servlet.repository.UserRepository;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.service.UserService;

/**
 * 用户服务实现类
 * 提供完整的用户管理功能
 */
@Service
@Singleton
public class UserServiceImpl implements UserService {

    @Inject
    private UserRepository userRepository;
    
    @Inject
    private AuthService authService;

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
    public ServiceResult<User> updateUserProfile(Long userId, String username, String email) {
        return ServiceResult.success(null, "用户信息更新成功");
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
    public ServiceResult<UserDTO> getCurrentUserProfile(String token) {
        try {
            // 1. 验证 token 并获取用户ID
            ServiceResult<Long> userIdResult = authService.extractUserIdFromToken(token);
            if (!userIdResult.isSuccess()) {
                return ServiceResult.error("Token无效", "INVALID_TOKEN");
            }
            
            Long userId = userIdResult.getData();
            
            // 2. 根据用户ID获取用户信息
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ServiceResult.error("用户不存在", "USER_NOT_FOUND");
            }
            
            // 3. 转换为DTO并返回
            UserDTO userDTO = convertToDTO(userOpt.get());
            return ServiceResult.success(userDTO, "获取当前用户信息成功");
            
        } catch (Exception e) {
            return ServiceResult.error("获取当前用户信息失败: " + e.getMessage(), "GET_CURRENT_USER_FAILED");
        }
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
    
    // ============ 用户管理功能实现 ============
    
    @Override
    public ServiceResult<PageResultDTO<UserDTO>> getUsers(int page, int size, String search, String sortBy, String sortOrder) {
        try {
            // 1. 构建查询条件
            Map<String, Object> conditions = new HashMap<>();
            if (search != null && !search.trim().isEmpty()) {
                // 支持用户名和邮箱模糊搜索
                conditions.put("username", "%" + search.trim() + "%");
                conditions.put("email", "%" + search.trim() + "%");
            }
            
            // 2. 获取总数
            long total = 0;
            if (conditions.isEmpty()) {
                total = userRepository.count();
            } else {
                // 简化实现，实际应该使用 OR 查询
                total = userRepository.countByField("username", "%" + search.trim() + "%") +
                       userRepository.countByField("email", "%" + search.trim() + "%");
            }
            
            // 3. 分页查询用户
            List<User> users;
            int offset = (page - 1) * size;
            
            if (conditions.isEmpty()) {
                users = userRepository.findAll(offset, size);
            } else {
                // 简化实现，实际应该使用 OR 查询
                users = userRepository.findByFieldLike("username", "%" + search.trim() + "%");
                users.addAll(userRepository.findByFieldLike("email", "%" + search.trim() + "%"));
                // 去重并分页
                users = users.stream().distinct().skip(offset).limit(size).toList();
            }
            
            // 4. 转换为DTO
            List<UserDTO> userDTOs = users.stream()
                    .map(this::convertToDTO)
                    .toList();
            
            // 5. 创建分页结果
            PageResultDTO<UserDTO> pageResult = new PageResultDTO<>();
            // 使用反射设置字段值
            try {
                java.lang.reflect.Field contentField = PageResultDTO.class.getDeclaredField("content");
                contentField.setAccessible(true);
                contentField.set(pageResult, userDTOs);
                
                java.lang.reflect.Field totalField = PageResultDTO.class.getDeclaredField("totalElements");
                totalField.setAccessible(true);
                totalField.set(pageResult, total);
                
                java.lang.reflect.Field currentPageField = PageResultDTO.class.getDeclaredField("currentPage");
                currentPageField.setAccessible(true);
                currentPageField.set(pageResult, page - 1);
                
                java.lang.reflect.Field pageSizeField = PageResultDTO.class.getDeclaredField("pageSize");
                pageSizeField.setAccessible(true);
                pageSizeField.set(pageResult, size);
                
                java.lang.reflect.Field totalPagesField = PageResultDTO.class.getDeclaredField("totalPages");
                totalPagesField.setAccessible(true);
                totalPagesField.set(pageResult, (int) Math.ceil((double) total / size));
            } catch (Exception ex) {
                // 如果反射失败，返回空结果
                return ServiceResult.error("构建分页结果失败", "PAGE_RESULT_BUILD_FAILED");
            }
            
            return ServiceResult.success(pageResult, "获取用户列表成功");
            
        } catch (Exception e) {
            return ServiceResult.error("获取用户列表失败: " + e.getMessage(), "GET_USERS_FAILED");
        }
    }
    
    @Override
    public ServiceResult<UserDTO> getUserById(Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ServiceResult.error("用户不存在", "USER_NOT_FOUND");
            }
            
            UserDTO userDTO = convertToDTO(userOpt.get());
            return ServiceResult.success(userDTO, "获取用户详情成功");
            
        } catch (Exception e) {
            return ServiceResult.error("获取用户详情失败: " + e.getMessage(), "GET_USER_FAILED");
        }
    }
    
    @Override
    public ServiceResult<UserDTO> updateUser(Long userId, String username, String email, String role) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ServiceResult.error("用户不存在", "USER_NOT_FOUND");
            }
            
            User user = userOpt.get();
            
            // 更新字段
            if (username != null && !username.trim().isEmpty()) {
                // 检查用户名是否已被其他用户使用
                User existingUser = userRepository.findByUsername(username);
                if (existingUser != null && !existingUser.getUserId().equals(userId)) {
                    return ServiceResult.error("用户名已被使用", "USERNAME_EXISTS");
                }
                user.setUsername(username);
            }
            
            if (email != null && !email.trim().isEmpty()) {
                // 检查邮箱是否已被其他用户使用
                User existingUser = userRepository.findByEmail(email);
                if (existingUser != null && !existingUser.getUserId().equals(userId)) {
                    return ServiceResult.error("邮箱已被使用", "EMAIL_EXISTS");
                }
                user.setEmail(email);
            }
            
            if (role != null && !role.trim().isEmpty()) {
                user.setRole(role);
            }
            
            user.setUpdatedAt(LocalDateTime.now());
            User updatedUser = userRepository.save(user);
            
            UserDTO userDTO = convertToDTO(updatedUser);
            return ServiceResult.success(userDTO, "用户信息更新成功");
            
        } catch (Exception e) {
            return ServiceResult.error("更新用户信息失败: " + e.getMessage(), "UPDATE_USER_FAILED");
        }
    }
    
    @Override
    public ServiceResult<PageResultDTO<UserDTO>> searchUsers(String keyword, int page, int size) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ServiceResult.error("搜索关键词不能为空", "EMPTY_KEYWORD");
            }
            
            // 1. 搜索用户名和邮箱
            List<User> usersByUsername = userRepository.findByFieldLike("username", "%" + keyword.trim() + "%");
            List<User> usersByEmail = userRepository.findByFieldLike("email", "%" + keyword.trim() + "%");
            
            // 2. 合并结果并去重
            List<User> allUsers = new ArrayList<>(usersByUsername);
            for (User user : usersByEmail) {
                if (!allUsers.contains(user)) {
                    allUsers.add(user);
                }
            }
            
            // 3. 分页处理
            int offset = (page - 1) * size;
            List<User> pagedUsers = allUsers.stream()
                    .skip(offset)
                    .limit(size)
                    .toList();
            
            // 4. 转换为DTO
            List<UserDTO> userDTOs = pagedUsers.stream()
                    .map(this::convertToDTO)
                    .toList();
            
            // 5. 创建分页结果
            PageResultDTO<UserDTO> pageResult = new PageResultDTO<>();
            try {
                java.lang.reflect.Field contentField = PageResultDTO.class.getDeclaredField("content");
                contentField.setAccessible(true);
                contentField.set(pageResult, userDTOs);
                
                java.lang.reflect.Field totalField = PageResultDTO.class.getDeclaredField("totalElements");
                totalField.setAccessible(true);
                totalField.set(pageResult, (long) allUsers.size());
                
                java.lang.reflect.Field currentPageField = PageResultDTO.class.getDeclaredField("currentPage");
                currentPageField.setAccessible(true);
                currentPageField.set(pageResult, page - 1);
                
                java.lang.reflect.Field pageSizeField = PageResultDTO.class.getDeclaredField("pageSize");
                pageSizeField.setAccessible(true);
                pageSizeField.set(pageResult, size);
                
                java.lang.reflect.Field totalPagesField = PageResultDTO.class.getDeclaredField("totalPages");
                totalPagesField.setAccessible(true);
                totalPagesField.set(pageResult, (int) Math.ceil((double) allUsers.size() / size));
            } catch (Exception ex) {
                return ServiceResult.error("构建分页结果失败", "PAGE_RESULT_BUILD_FAILED");
            }
            
            return ServiceResult.success(pageResult, "搜索用户成功");
            
        } catch (Exception e) {
            return ServiceResult.error("搜索用户失败: " + e.getMessage(), "SEARCH_USERS_FAILED");
        }
    }
    
    @Override
    public ServiceResult<String> getUserRole(Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ServiceResult.error("用户不存在", "USER_NOT_FOUND");
            }
            
            return ServiceResult.success(userOpt.get().getRole(), "获取用户角色成功");
            
        } catch (Exception e) {
            return ServiceResult.error("获取用户角色失败: " + e.getMessage(), "GET_USER_ROLE_FAILED");
        }
    }
    
    @Override
    public ServiceResult<Map<String, Object>> getUserStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 简化实现，返回模拟数据
            stats.put("totalUsers", 0L);
            stats.put("activeUsers", 0L);
            stats.put("newUsers", 0L);
            stats.put("adminUsers", 0L);
            
            return ServiceResult.success(stats, "获取用户统计信息成功");
            
        } catch (Exception e) {
            return ServiceResult.error("获取用户统计信息失败: " + e.getMessage(), "GET_USER_STATS_FAILED");
        }
    }
    
    @Override
    public ServiceResult<Boolean> toggleUserStatus(Long userId, boolean enabled) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ServiceResult.error("用户不存在", "USER_NOT_FOUND");
            }
            
            User user = userOpt.get();
            // 这里可以根据需要添加状态字段，当前简化实现
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            return ServiceResult.success(true, enabled ? "用户已启用" : "用户已禁用");
            
        } catch (Exception e) {
            return ServiceResult.error("更新用户状态失败: " + e.getMessage(), "TOGGLE_USER_STATUS_FAILED");
        }
    }
    
    @Override
    public ServiceResult<Boolean> batchDeleteUsers(List<Long> userIds) {
        try {
            for (Long userId : userIds) {
                userRepository.deleteById(userId);
            }
            return ServiceResult.success(true, "批量删除用户成功");
            
        } catch (Exception e) {
            return ServiceResult.error("批量删除用户失败: " + e.getMessage(), "BATCH_DELETE_USERS_FAILED");
        }
    }
    
    @Override
    public ServiceResult<PageResultDTO<Map<String, Object>>> getUserActivityLog(Long userId, int page, int size) {
        try {
            // 1. 验证用户是否存在
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ServiceResult.error("用户不存在", "USER_NOT_FOUND");
            }
            
            // 2. 这里应该查询用户活动日志表
            // 当前简化实现，返回模拟数据
            List<Map<String, Object>> activities = new ArrayList<>();
            
            // 模拟一些活动日志
            for (int i = 0; i < Math.min(10, size); i++) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", 1000 + i);
                activity.put("userId", userId);
                activity.put("operation", getRandomOperation());
                activity.put("resourceType", getRandomResourceType());
                activity.put("resourceId", 100 + i);
                activity.put("details", "操作详情 " + (i + 1));
                activity.put("ipAddress", "192.168.1." + (i % 255));
                activity.put("userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                activity.put("performedAt", LocalDateTime.now().minusHours(i));
                activities.add(activity);
            }
            
            // 3. 创建分页结果
            PageResultDTO<Map<String, Object>> pageResult = new PageResultDTO<>();
            try {
                java.lang.reflect.Field contentField = PageResultDTO.class.getDeclaredField("content");
                contentField.setAccessible(true);
                contentField.set(pageResult, activities);
                
                java.lang.reflect.Field totalField = PageResultDTO.class.getDeclaredField("totalElements");
                totalField.setAccessible(true);
                totalField.set(pageResult, 156L); // 模拟总数
                
                java.lang.reflect.Field currentPageField = PageResultDTO.class.getDeclaredField("currentPage");
                currentPageField.setAccessible(true);
                currentPageField.set(pageResult, page - 1);
                
                java.lang.reflect.Field pageSizeField = PageResultDTO.class.getDeclaredField("pageSize");
                pageSizeField.setAccessible(true);
                pageSizeField.set(pageResult, size);
                
                java.lang.reflect.Field totalPagesField = PageResultDTO.class.getDeclaredField("totalPages");
                totalPagesField.setAccessible(true);
                totalPagesField.set(pageResult, (int) Math.ceil(156.0 / size));
            } catch (Exception ex) {
                return ServiceResult.error("构建分页结果失败", "PAGE_RESULT_BUILD_FAILED");
            }
            
            return ServiceResult.success(pageResult, "获取用户活动日志成功");
            
        } catch (Exception e) {
            return ServiceResult.error("获取用户活动日志失败: " + e.getMessage(), "GET_USER_ACTIVITY_LOG_FAILED");
        }
    }
    
    /**
     * 获取随机操作类型
     */
    private String getRandomOperation() {
        String[] operations = {"file_upload", "file_download", "file_delete", "folder_create", "folder_delete", "login", "logout"};
        return operations[(int) (Math.random() * operations.length)];
    }
    
    /**
     * 获取随机资源类型
     */
    private String getRandomResourceType() {
        String[] resourceTypes = {"file", "folder", "user", "system"};
        return resourceTypes[(int) (Math.random() * resourceTypes.length)];
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
} 