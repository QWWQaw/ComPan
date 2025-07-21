package cloud.compan.servlet.repository;

import java.time.LocalDateTime;
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
        String sql = "SELECT * FROM `user` WHERE `username` = ?";
        Optional<User> result = executor.queryForObject(entityClass, sql, rowMapper, username);
        return result.orElse(null);
    }

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户对象，如果不存在返回null
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM `user` WHERE `email` = ?";
        Optional<User> result = executor.queryForObject(entityClass, sql, rowMapper, email);
        return result.orElse(null);
    }
    
    /**  
     * 检查具有给定用户名的用户是否存在。  
     * 这比 findByUsername 更高效，因为它不传输所有列的数据。  
     * @param username 用户名  
     * @return 如果存在则为 true  
     */  
    public boolean existsByUsername(String username) {  
        String sql = "SELECT 1 FROM `user` WHERE `username` = ? LIMIT 1";  
        // 我们只需要知道有没有结果，所以 RowMapper 很简单  
        return executor.queryForObject(User.class, sql, rs -> rs.getInt(1), username).isPresent();  
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
        User newUser = User.builder()
                .username(username)
                .email(email)
                .passwordHash(hashedPassword)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .storageLimit(10737418240L) // 默认 10GB
                .storageUsed(0L)
                .build();

        return save(newUser);
    }

}
