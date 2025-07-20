//package cloud.compan.servlet.service.impl;
//
//import cloud.compan.servlet.service.UserService;
//import cloud.compan.servlet.model.User;
//import cloud.compan.servlet.repository.UserRepository;
//import cloud.compan.servlet.utils.HashUtil;
//import cloud.compan.servlet.dto.ServiceResult;
//import cloud.compan.servlet.dto.LoginResultDTO;
//import cloud.compan.servlet.dto.StorageStatsDTO;
//import cloud.compan.servlet.dto.PageResultDTO;
//import cloud.compan.servlet.dto.UserDTO;
//import cloud.compan.servlet.dto.SearchCriteria;
//import cloud.compan.servlet.dto.mapper.UserMapper;
//import com.google.inject.Inject;
//import com.google.inject.Singleton;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
///**
// * 用户服务实现类
// * 提供完整的用户管理功能，包括认证、授权、信息管理等
// */
//@Singleton
//public class UserServiceImpl implements UserService {
//
//    @Inject
//    private UserRepository userRepository;
//
//    @Inject
//    private HashUtil hashUtil;
//
//    @Inject
//    private UserMapper userMapper;
//
//    // ============ 基础CRUD操作 ============
//
//    @Override
//    public ServiceResult<User> findById(Long id) {
//        try {
//            if (id == null) {
//                return ServiceResult.failure("用户ID不能为空");
//            }
//
//            Optional<User> userOpt = userRepository.findById(id);
//            if (userOpt.isPresent()) {
//                return ServiceResult.success(userOpt.get());
//            } else {
//                return ServiceResult.failure("用户不存在", "USER_NOT_FOUND");
//            }
//        } catch (Exception e) {
//            return ServiceResult.failure("查询用户失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<List<User>> findAll() {
//        try {
//            List<User> users = userRepository.findAll();
//            return ServiceResult.success(users);
//        } catch (Exception e) {
//            return ServiceResult.failure("查询用户列表失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<User> save(User entity) {
//        try {
//            if (entity == null) {
//                return ServiceResult.failure("用户对象不能为空");
//            }
//
//            // 设置更新时间
//            entity.setUpdatedAt(LocalDateTime.now());
//            if (entity.getUserId() == null) {
//                entity.setCreatedAt(LocalDateTime.now());
//            }
//
//            User savedUser = userRepository.save(entity);
//            return ServiceResult.success(savedUser);
//        } catch (Exception e) {
//            return ServiceResult.failure("保存用户失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<Boolean> deleteById(Long id) {
//        try {
//            if (id == null) {
//                return ServiceResult.failure("用户ID不能为空");
//            }
//
//            userRepository.deleteById(id);
//            return ServiceResult.success(true);
//        } catch (Exception e) {
//            return ServiceResult.failure("删除用户失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<Boolean> delete(User entity) {
//        if (entity != null && entity.getUserId() != null) {
//            return deleteById(entity.getUserId());
//        }
//        return ServiceResult.failure("用户实体无效");
//    }
//
//    @Override
//    public ServiceResult<User> updateById(Long id, User entity) {
//        entity.setUserId(id);
//        return save(entity);
//    }
//
//    @Override
//    public ServiceResult<Boolean> existsById(Long id) {
//        ServiceResult<User> result = findById(id);
//        return ServiceResult.success(result.isSuccess());
//    }
//
//    @Override
//    public ServiceResult<Long> count() {
//        try {
//            List<User> users = userRepository.findAll();
//            return ServiceResult.success((long) users.size());
//        } catch (Exception e) {
//            return ServiceResult.failure("统计用户数量失败: " + e.getMessage());
//        }
//    }
//
//    // ============ 扩展查询操作 ============
//
//    @Override
//    public ServiceResult<PageResultDTO<User>> findPage(int page, int size) {
//        try {
//            List<User> allUsers = userRepository.findAll();
//
//            // 简单分页实现
//            int start = (page - 1) * size;
//            int end = Math.min(start + size, allUsers.size());
//
//            if (start >= allUsers.size()) {
//                return ServiceResult.success(new PageResultDTO<User>(
//                    List.of(), allUsers.size(), page, size
//                ));
//            }
//
//            List<User> pageUsers = allUsers.subList(start, end);
//            PageResultDTO<User> pageResult = new PageResultDTO<User>(
//                pageUsers, allUsers.size(), page, size
//            );
//
//            return ServiceResult.success(pageResult);
//        } catch (Exception e) {
//            return ServiceResult.failure("分页查询失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<List<User>> findByCriteria(SearchCriteria criteria) {
//        try {
//            List<User> allUsers = userRepository.findAll();
//
//            if (criteria == null || !criteria.hasKeyword()) {
//                return ServiceResult.success(allUsers);
//            }
//
//            String keyword = criteria.getKeyword().toLowerCase();
//            List<User> filteredUsers = allUsers.stream()
//                .filter(user ->
//                    user.getUsername().toLowerCase().contains(keyword) ||
//                    user.getEmail().toLowerCase().contains(keyword)
//                )
//                .collect(Collectors.toList());
//
//            return ServiceResult.success(filteredUsers);
//        } catch (Exception e) {
//            return ServiceResult.failure("条件查询失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<PageResultDTO<User>> findPageByCriteria(SearchCriteria criteria) {
//        try {
//            ServiceResult<List<User>> result = findByCriteria(criteria);
//            if (!result.isSuccess()) {
//                return ServiceResult.failure(result.getMessage());
//            }
//
//            List<User> users = result.getData();
//            int page = criteria != null ? criteria.getPage() : 1;
//            int size = criteria != null ? criteria.getSize() : 20;
//
//            int start = (page - 1) * size;
//            int end = Math.min(start + size, users.size());
//
//            if (start >= users.size()) {
//                return ServiceResult.success(new PageResultDTO<User>(
//                    List.of(), users.size(), page, size
//                ));
//            }
//
//            List<User> pageUsers = users.subList(start, end);
//            PageResultDTO<User> pageResult = new PageResultDTO<User>(
//                pageUsers, users.size(), page, size
//            );
//
//            return ServiceResult.success(pageResult);
//        } catch (Exception e) {
//            return ServiceResult.failure("条件分页查询失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<List<User>> saveAll(List<User> entities) {
//        try {
//            if (entities == null || entities.isEmpty()) {
//                return ServiceResult.success(List.of());
//            }
//
//            List<User> savedUsers = entities.stream()
//                .map(user -> {
//                    ServiceResult<User> result = save(user);
//                    return result.isSuccess() ? result.getData() : null;
//                })
//                .filter(user -> user != null)
//                .collect(Collectors.toList());
//
//            return ServiceResult.success(savedUsers);
//        } catch (Exception e) {
//            return ServiceResult.failure("批量保存失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<Integer> deleteByIds(List<Long> ids) {
//        try {
//            if (ids == null || ids.isEmpty()) {
//                return ServiceResult.success(0);
//            }
//
//            int deletedCount = 0;
//            for (Long id : ids) {
//                ServiceResult<Boolean> result = deleteById(id);
//                if (result.isSuccess() && result.getData()) {
//                    deletedCount++;
//                }
//            }
//
//            return ServiceResult.success(deletedCount);
//        } catch (Exception e) {
//            return ServiceResult.failure("批量删除失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<Long> countByCriteria(SearchCriteria criteria) {
//        try {
//            ServiceResult<List<User>> result = findByCriteria(criteria);
//            if (result.isSuccess()) {
//                return ServiceResult.success((long) result.getData().size());
//            } else {
//                return ServiceResult.failure(result.getMessage());
//            }
//        } catch (Exception e) {
//            return ServiceResult.failure("条件统计失败: " + e.getMessage());
//        }
//    }
//
//    // ============ 用户认证相关 ============
//
//    @Override
//    public ServiceResult<User> register(String username, String email, String password) {
//        try {
//            // 参数验证
//            if (username == null || username.trim().isEmpty()) {
//                return ServiceResult.failure("用户名不能为空");
//            }
//            if (email == null || email.trim().isEmpty()) {
//                return ServiceResult.failure("邮箱不能为空");
//            }
//            if (password == null || password.length() < 6) {
//                return ServiceResult.failure("密码长度不能少于6位");
//            }
//
//            // 检查用户名是否存在
//            if (userRepository.existsByUsername(username)) {
//                return ServiceResult.failure("用户名已存在", "USERNAME_EXISTS");
//            }
//
//            // 创建新用户
//            User newUser = userRepository.registerNewUser(username, email, password);
//            return ServiceResult.success(newUser);
//
//        } catch (Exception e) {
//            return ServiceResult.failure("注册失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<LoginResultDTO> login(String username, String password) {
//        try {
//            // 参数验证
//            if (username == null || username.trim().isEmpty()) {
//                return ServiceResult.failure("用户名不能为空");
//            }
//            if (password == null || password.isEmpty()) {
//                return ServiceResult.failure("密码不能为空");
//            }
//
//            // 查找用户
//            Optional<User> userOpt = userRepository.findByUsername(username.trim());
//            if (!userOpt.isPresent()) {
//                return ServiceResult.failure("用户名或密码错误", "INVALID_CREDENTIALS");
//            }
//
//            User user = userOpt.get();
//
//            // 验证密码
//            if (!hashUtil.checkPassword(password, user.getPassword())) {
//                return ServiceResult.failure("用户名或密码错误", "INVALID_CREDENTIALS");
//            }
//
//            // 生成token（简化实现）
//            String token = "token_" + user.getUserId() + "_" + System.currentTimeMillis();
//            long expiresIn = 7 * 24 * 60 * 60; // 7天（秒）
//
//            // 创建登录结果
//            UserDTO userDTO = userMapper.toDTO(user);
//            LoginResultDTO loginResult = new LoginResultDTO(userDTO, token, expiresIn);
//
//            return ServiceResult.success(loginResult);
//
//        } catch (Exception e) {
//            return ServiceResult.failure("登录失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<User> findByUsername(String username) {
//        try {
//            Optional<User> userOpt = userRepository.findByUsername(username);
//            if (userOpt.isPresent()) {
//                return ServiceResult.success(userOpt.get());
//            } else {
//                return ServiceResult.failure("用户不存在", "USER_NOT_FOUND");
//            }
//        } catch (Exception e) {
//            return ServiceResult.failure("查询用户失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<User> findByEmail(String email) {
//        // 基于现有方法的简单实现
//        try {
//            List<User> allUsers = userRepository.findAll();
//            Optional<User> userOpt = allUsers.stream()
//                .filter(user -> email.equals(user.getEmail()))
//                .findFirst();
//
//            if (userOpt.isPresent()) {
//                return ServiceResult.success(userOpt.get());
//            } else {
//                return ServiceResult.failure("用户不存在", "USER_NOT_FOUND");
//            }
//        } catch (Exception e) {
//            return ServiceResult.failure("查询用户失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<Boolean> existsByUsername(String username) {
//        try {
//            boolean exists = userRepository.existsByUsername(username);
//            return ServiceResult.success(exists);
//        } catch (Exception e) {
//            return ServiceResult.failure("检查用户名失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<Boolean> existsByEmail(String email) {
//        try {
//            ServiceResult<User> result = findByEmail(email);
//            return ServiceResult.success(result.isSuccess());
//        } catch (Exception e) {
//            return ServiceResult.failure("检查邮箱失败: " + e.getMessage());
//        }
//    }
//
//    // ============ 用户信息管理 ============
//
//    @Override
//    public ServiceResult<User> updateProfile(Long userId, String username, String email) {
//        try {
//            ServiceResult<User> userResult = findById(userId);
//            if (!userResult.isSuccess()) {
//                return userResult;
//            }
//
//            User user = userResult.getData();
//
//            // 更新用户信息
//            if (username != null && !username.trim().isEmpty()) {
//                // 检查新用户名是否已被其他用户使用
//                if (!username.equals(user.getUsername()) && userRepository.existsByUsername(username)) {
//                    return ServiceResult.failure("用户名已被使用", "USERNAME_EXISTS");
//                }
//                user.setUsername(username.trim());
//            }
//
//            if (email != null && !email.trim().isEmpty()) {
//                // 检查新邮箱是否已被其他用户使用
//                ServiceResult<Boolean> emailExists = existsByEmail(email);
//                if (emailExists.isSuccess() && emailExists.getData() && !email.equals(user.getEmail())) {
//                    return ServiceResult.failure("邮箱已被使用", "EMAIL_EXISTS");
//                }
//                user.setEmail(email.trim());
//            }
//
//            return save(user);
//        } catch (Exception e) {
//            return ServiceResult.failure("更新用户信息失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<Boolean> changePassword(Long userId, String oldPassword, String newPassword) {
//        try {
//            ServiceResult<User> userResult = findById(userId);
//            if (!userResult.isSuccess()) {
//                return ServiceResult.failure("用户不存在");
//            }
//
//            User user = userResult.getData();
//
//            // 验证旧密码
//            if (!hashUtil.checkPassword(oldPassword, user.getPassword())) {
//                return ServiceResult.failure("当前密码错误", "INVALID_OLD_PASSWORD");
//            }
//
//            // 验证新密码
//            if (newPassword == null || newPassword.length() < 6) {
//                return ServiceResult.failure("新密码长度不能少于6位");
//            }
//
//            // 更新密码
//            user.setPassword(hashUtil.hashPassword(newPassword));
//            ServiceResult<User> saveResult = save(user);
//
//            return ServiceResult.success(saveResult.isSuccess());
//        } catch (Exception e) {
//            return ServiceResult.failure("修改密码失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<Boolean> resetPassword(Long userId, String newPassword) {
//        try {
//            ServiceResult<User> userResult = findById(userId);
//            if (!userResult.isSuccess()) {
//                return ServiceResult.failure("用户不存在");
//            }
//
//            // 验证新密码
//            if (newPassword == null || newPassword.length() < 6) {
//                return ServiceResult.failure("新密码长度不能少于6位");
//            }
//
//            User user = userResult.getData();
//            user.setPassword(hashUtil.hashPassword(newPassword));
//            ServiceResult<User> saveResult = save(user);
//
//            return ServiceResult.success(saveResult.isSuccess());
//        } catch (Exception e) {
//            return ServiceResult.failure("重置密码失败: " + e.getMessage());
//        }
//    }
//
//    // ============ 存储管理 ============
//
//    @Override
//    public ServiceResult<StorageStatsDTO> getStorageStats(Long userId) {
//        try {
//            ServiceResult<User> userResult = findById(userId);
//            if (!userResult.isSuccess()) {
//                return ServiceResult.failure("用户不存在");
//            }
//
//            User user = userResult.getData();
//
//            StorageStatsDTO stats = new StorageStatsDTO(
//                user.getStorageLimit(),
//                user.getStorageUsed(),
//                0, // fileCount - TODO: 实际项目中需要查询文件表
//                0  // folderCount - TODO: 实际项目中需要查询文件夹表
//            );
//
//            return ServiceResult.success(stats);
//        } catch (Exception e) {
//            return ServiceResult.failure("获取存储统计失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<Boolean> updateStorageUsed(Long userId, long sizeChange) {
//        try {
//            ServiceResult<User> userResult = findById(userId);
//            if (!userResult.isSuccess()) {
//                return ServiceResult.failure("用户不存在");
//            }
//
//            User user = userResult.getData();
//            long newStorageUsed = user.getStorageUsed() + sizeChange;
//
//            // 确保存储使用量不为负数
//            if (newStorageUsed < 0) {
//                newStorageUsed = 0;
//            }
//
//            // 检查是否超过存储限制
//            if (sizeChange > 0 && newStorageUsed > user.getStorageLimit()) {
//                return ServiceResult.failure("存储空间不足", "STORAGE_LIMIT_EXCEEDED");
//            }
//
//            user.setStorageUsed(newStorageUsed);
//            ServiceResult<User> saveResult = save(user);
//
//            return ServiceResult.success(saveResult.isSuccess());
//        } catch (Exception e) {
//            return ServiceResult.failure("更新存储使用量失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<Boolean> checkStorageCapacity(Long userId, long requiredSize) {
//        try {
//            ServiceResult<User> userResult = findById(userId);
//            if (!userResult.isSuccess()) {
//                return ServiceResult.failure("用户不存在");
//            }
//
//            User user = userResult.getData();
//            long availableSpace = user.getStorageLimit() - user.getStorageUsed();
//
//            return ServiceResult.success(availableSpace >= requiredSize);
//        } catch (Exception e) {
//            return ServiceResult.failure("检查存储容量失败: " + e.getMessage());
//        }
//    }
//
//    // ============ 用户查询 ============
//
//    @Override
//    public ServiceResult<PageResultDTO<User>> searchUsers(String keyword, int page, int size) {
//        try {
//            SearchCriteria criteria = new SearchCriteria(keyword)
//                .page(page)
//                .size(size);
//            return findPageByCriteria(criteria);
//        } catch (Exception e) {
//            return ServiceResult.failure("搜索用户失败: " + e.getMessage());
//        }
//    }
//
//    @Override
//    public ServiceResult<PageResultDTO<User>> getActiveUsers(int days, int page, int size) {
//        try {
//            // 简化实现：返回所有用户
//            // TODO: 实际项目中需要根据用户活动记录来过滤
//            return findPage(page, size);
//        } catch (Exception e) {
//            return ServiceResult.failure("查询活跃用户失败: " + e.getMessage());
//        }
//    }
//}