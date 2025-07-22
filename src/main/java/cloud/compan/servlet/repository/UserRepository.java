package cloud.compan.servlet.repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Repository;
import cloud.compan.servlet.model.User;
import cloud.compan.servlet.utils.HashUtil;
import cloud.compan.servlet.utils.JdbcExecutor;

/**
 * 继承自 {@link BaseRepository}，提供了对 {@link User} 实体的特定数据访问操作。
 * 它利用基类提供的通用 CRUD 功能，并添加了用户特有的查询，例如按用户名查找。
 */
@Singleton
@Repository
public class UserRepository extends BaseRepository<User, Long> {

    private final HashUtil hashUtil;

    @Inject
    public UserRepository(JdbcExecutor executor, HashUtil hashUtil) {
        super(executor);
        this.hashUtil = hashUtil;
    }

    /**  
     * 根据用户名查找用户。因为用户名是唯一的，所以预期最多返回一个结果。  
     * @param username 用户名  
     * @return 用户对象，如果不存在返回null
     */
    public User findByUsername(String username) {
        return findOneByField("username", username).orElse(null);
    }

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户对象，如果不存在返回null
     */
    public User findByEmail(String email) {
        return findOneByField("email", email).orElse(null);
    }
    
    /**  
     * 检查具有给定用户名的用户是否存在。  
     * 这比 findByUsername 更高效，因为它不传输所有列的数据。  
     * @param username 用户名  
     * @return 如果存在则为 true  
     */  
    public boolean existsByUsername(String username) {  
        return existsByField("username", username);
    }

    /**
     * 注册一个新用户。
     * @param username 用户名
     * @param email 邮箱
     * @param plainTextPassword 明文密码
     * @return 创建的新用户对象
     */
    public User registerNewUser(String username, String email, String plainTextPassword) {
        String hashedPassword = hashUtil.hashPassword(plainTextPassword);
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(hashedPassword);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        newUser.setStorageLimit(10737418240L); // 默认 10GB
        newUser.setStorageUsed(0L);
        newUser.setRole("USER");

        return save(newUser);
    }

    /**
     * 根据邮箱检查用户是否存在
     * @param email 邮箱
     * @return 如果存在则为 true
     */
    public boolean existsByEmail(String email) {
        return existsByField("email", email);
    }

    /**
     * 更新用户存储使用量
     * @param userId 用户ID
     * @param sizeChange 存储大小变化（正数增加，负数减少）
     * @return 更新后的用户对象
     */
    public User updateStorageUsed(Long userId, long sizeChange) {
        Optional<User> userOpt = findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在: " + userId);
        }
        
        User user = userOpt.get();
        long newStorageUsed = user.getStorageUsed() + sizeChange;
        if (newStorageUsed < 0) {
            newStorageUsed = 0; // 不能为负数
        }
        user.setStorageUsed(newStorageUsed);
        user.setUpdatedAt(LocalDateTime.now());
        
        return save(user);
    }

    /**
     * 检查用户存储容量是否足够
     * @param userId 用户ID
     * @param requiredSize 需要的存储大小
     * @return 如果足够返回 true
     */
    public boolean checkStorageCapacity(Long userId, long requiredSize) {
        Optional<User> userOpt = findById(userId);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        return (user.getStorageUsed() + requiredSize) <= user.getStorageLimit();
    }

    /**
     * 获取用户存储统计信息
     * @param userId 用户ID
     * @return 存储统计信息 Map
     */
    public Map<String, Object> getStorageStats(Long userId) {
        Optional<User> userOpt = findById(userId);
        if (userOpt.isEmpty()) {
            return null;
        }
        
        User user = userOpt.get();
        Map<String, Object> stats = new HashMap<>();
        stats.put("storageLimit", user.getStorageLimit());
        stats.put("storageUsed", user.getStorageUsed());
        stats.put("storageAvailable", user.getStorageLimit() - user.getStorageUsed());
        stats.put("storagePercentage", (double) user.getStorageUsed() / user.getStorageLimit() * 100);
        
        return stats;
    }

    /**
     * 根据角色查找用户
     * @param role 角色
     * @return 用户列表
     */
    public List<User> findByRole(String role) {
        return findAllByField("role", role);
    }

    /**
     * 查找活跃用户（最近登录的用户）
     * @param days 最近天数
     * @return 用户列表
     */
    public List<User> findActiveUsers(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        return findByFieldBetween("updatedAt", cutoffDate, LocalDateTime.now());
    }

    /**
     * 查找存储使用量超过阈值的用户
     * @param threshold 阈值（字节）
     * @return 用户列表
     */
    public List<User> findUsersWithHighStorageUsage(long threshold) {
        return findByFieldBetween("storageUsed", threshold, Long.MAX_VALUE);
    }

    /**
     * 批量更新用户状态
     * @param userIds 用户ID列表
     * @param enabled 是否启用
     * @return 更新的用户数量
     */
    public int batchUpdateUserStatus(List<Long> userIds, boolean enabled) {
        int count = 0;
        for (Long userId : userIds) {
            Optional<User> userOpt = findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // 这里可以根据需要添加状态字段
                user.setUpdatedAt(LocalDateTime.now());
                save(user);
                count++;
            }
        }
        return count;
    }

    /**
     * 查找最近注册的用户
     * @param limit 限制数量
     * @return 用户列表
     */
    public List<User> findRecentUsers(int limit) {
        List<User> users = findAllOrderBy("createdAt", false); // DESC
        return users.stream().limit(limit).toList();
    }

    /**
     * 根据存储使用量排序查找用户
     * @param order 排序方式 ("ASC" 或 "DESC")
     * @param limit 限制数量
     * @return 用户列表
     */
    public List<User> findUsersByStorageUsage(String order, int limit) {
        boolean ascending = "ASC".equalsIgnoreCase(order);
        List<User> users = findAllOrderBy("storageUsed", ascending);
        return users.stream().limit(limit).toList();
    }

    /**
     * 统计用户数量
     * @return 用户总数
     */
    public long countUsers() {
        return count();
    }

    /**
     * 统计指定角色的用户数量
     * @param role 角色
     * @return 用户数量
     */
    public long countUsersByRole(String role) {
        return countByField("role", role);
    }

    /**
     * 查找邮箱域名相同的用户
     * @param emailDomain 邮箱域名（如 "example.com"）
     * @return 用户列表
     */
    public List<User> findByEmailDomain(String emailDomain) {
        return findByFieldLike("email", "%@" + emailDomain);
    }

    /**
     * 查找用户名包含特定关键词的用户
     * @param keyword 关键词
     * @return 用户列表
     */
    public List<User> findByUsernameContaining(String keyword) {
        return findByFieldLike("username", "%" + keyword + "%");
    }

    /**
     * 查找在指定时间范围内创建的用户
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 用户列表
     */
    public List<User> findUsersCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return findByFieldBetween("createdAt", startDate, endDate);
    }

    /**
     * 查找在指定时间范围内更新的用户
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 用户列表
     */
    public List<User> findUsersUpdatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return findByFieldBetween("updatedAt", startDate, endDate);
    }
}
