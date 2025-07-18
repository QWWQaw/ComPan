package cloud.compan.servlet.repository;

import cloud.compan.servlet.model.User;
import cloud.compan.servlet.utils.HashUtil;
import cloud.compan.servlet.utils.JdbcExecutor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 继承自 {@link BaseRepository}，提供了对 {@link User} 实体的特定数据访问操作。
 * 它利用基类提供的通用 CRUD 功能，并添加了用户特有的查询，例如按用户名查找。
 */
@Singleton
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
     * @return 包含用户的 Optional  
     */  
    public Optional<User> findByUsername(String username) {  
        String sql = "SELECT * FROM `user` WHERE `username` = ?";  
        return executor.queryForObject(entityClass, sql, rowMapper, username);
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
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(hashedPassword);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        newUser.setStorageLimit(10737418240L); // 默认 10GB
        newUser.setStorageUsed(0L);

        return save(newUser);
    }

}
