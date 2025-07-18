package cloud.compan.servlet.service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务类 - 处理用户相关的数据库操作， 密码验证部分是简单验证，没有使用hash验证
 */
public class UserService {

    private DatabaseService databaseService;

    public UserService() {
        this.databaseService = DatabaseService.getInstance();
    }

    /**
     * 检查用户名是否已存在
     */
    public boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("检查用户名是否存在失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }

        return false;
    }

    /**
     * 检查邮箱是否已存在
     */
    public boolean isEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("检查邮箱是否存在失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }

        return false;
    }

    /**
     * 创建新用户
     */
    public Map<String, Object> createUser(String username, String email, String passwordHash) {
        String sql = "INSERT INTO users (username, email, password_hash, storage_limit, storage_used, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, passwordHash);
            stmt.setLong(4, 10737418240L); // 10GB默认存储空间
            stmt.setLong(5, 0L); // 初始使用空间为0
            stmt.setString(6, "active");
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    Long userId = generatedKeys.getLong(1);

                    // 返回创建的用户信息
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("user_id", userId);
                    userData.put("username", username);
                    userData.put("email", email);
                    userData.put("storage_limit", 10737418240L);
                    userData.put("storage_used", 0L);
                    userData.put("status", "active");
                    userData.put("created_at", LocalDateTime.now().toString());

                    return userData;
                }
            }

        } catch (SQLException e) {
            System.err.println("创建用户失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, null);
        }

        return null;
    }

    /**
     * 用户登录验证
     */
    public Map<String, Object> authenticateUser(String username, String password) {
        String sql = "SELECT user_id, username, email, password_hash, storage_limit, storage_used, status, created_at FROM users WHERE username = ? AND status = 'active'";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPasswordHash = rs.getString("password_hash");

                // 这里应该使用密码哈希验证，暂时使用简单比较
                if (password.equals(storedPasswordHash) || verifyPassword(password, storedPasswordHash)) {

//                    // 更新最后登录时间
//                    updateLastLogin(rs.getLong("user_id"));

                    // 返回用户信息
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("user_id", rs.getLong("user_id"));
                    userData.put("username", rs.getString("username"));
                    userData.put("email", rs.getString("email"));
                    userData.put("storage_limit", rs.getLong("storage_limit"));
                    userData.put("storage_used", rs.getLong("storage_used"));
                    userData.put("status", rs.getString("status"));
                    userData.put("created_at", rs.getTimestamp("created_at").toString());

                    return userData;
                }
            }

        } catch (SQLException e) {
            System.err.println("用户认证失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }

        return null;
    }

    /**
     * 根据用户ID获取用户信息
     */
    public Map<String, Object> getUserById(Long userId) {
        String sql = "SELECT user_id, username, email, storage_limit, storage_used, status, created_at, updated_at, last_login FROM users WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("user_id", rs.getLong("user_id"));
                userData.put("username", rs.getString("username"));
                userData.put("email", rs.getString("email"));
                userData.put("storage_limit", rs.getLong("storage_limit"));
                userData.put("storage_used", rs.getLong("storage_used"));
                userData.put("status", rs.getString("status"));
                userData.put("created_at", rs.getTimestamp("created_at").toString());

                Timestamp updatedAt = rs.getTimestamp("updated_at");
                if (updatedAt != null) {
                    userData.put("updated_at", updatedAt.toString());
                }

                Timestamp lastLogin = rs.getTimestamp("last_login");
                if (lastLogin != null) {
                    userData.put("last_login", lastLogin.toString());
                }

                return userData;
            }

        } catch (SQLException e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }

        return null;
    }

//    /**
//     * 更新用户最后登录时间，但是数据库里面好像没有这个字段
//     */
//    private void updateLastLogin(Long userId) {
//        String sql = "UPDATE users SET last_login = ? WHERE user_id = ?";
//        Connection conn = null;
//        PreparedStatement stmt = null;
//
//        try {
//            conn = databaseService.getConnection();
//            stmt = conn.prepareStatement(sql);
//            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
//            stmt.setLong(2, userId);
//            stmt.executeUpdate();
//
//        } catch (SQLException e) {
//            System.err.println("更新最后登录时间失败: " + e.getMessage());
//        } finally {
//            DatabaseService.closeResources(conn, stmt, null);
//        }
//    }

    /**
     * 简单的密码验证（实际项目中应该使用BCrypt等加密算法）
     */
    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        // 这里应该使用BCrypt.checkpw(rawPassword, hashedPassword)
        // 暂时使用简单比较
        return rawPassword.equals(hashedPassword);
    }

    /**
     * 密码哈希（实际项目中应该使用BCrypt等加密算法）
     */
    public String hashPassword(String rawPassword) {
        // 这里应该使用BCrypt.hashpw(rawPassword, BCrypt.gensalt())
        // 暂时直接返回原密码
        return rawPassword;
    }
}
