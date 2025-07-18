package cloud.compan.servlet.service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.security.MessageDigest;

/**
 * 用户服务类 - 处理用户相关的数据库操作
 * 根据RESTFUL API文档完善所有用户相关的业务逻辑
 */
public class UserService {

    private DatabaseService databaseService;

    public UserService() {
        this.databaseService = DatabaseService.getInstance();
    }

    /**
     * 用户注册 - Handler中需要的方法
     */
    public Map<String, Object> registerUser(String username, String email, String password) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 检查用户名是否已存在
            if (isUsernameExists(username)) {
                result.put("success", false);
                result.put("message", "用户名已存在");
                return result;
            }

            // 2. 检查邮箱是否已存在
            if (isEmailExists(email)) {
                result.put("success", false);
                result.put("message", "邮箱已存在");
                return result;
            }

            // 3. 创建用户
            Map<String, Object> userData = createUser(username, email, password);
            if (userData != null) {
                result.put("success", true);
                result.put("message", "注册成功");
                result.put("data", userData);
            } else {
                result.put("success", false);
                result.put("message", "注册失败");
            }

        } catch (Exception e) {
            System.err.println("用户注册失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 用户登录 - Handler中需要的方法
     */
    public Map<String, Object> loginUser(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> userData = authenticateUser(username, password);
            if (userData != null) {
                result.put("success", true);
                result.put("message", "登录成功");
                result.put("data", userData);
            } else {
                result.put("success", false);
                result.put("message", "用户名或密码错误");
            }

        } catch (Exception e) {
            System.err.println("用户登录失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 验证用户登录 - AuthService需要的方法
     */
    public Map<String, Object> validateLogin(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT user_id, username, email, password_hash FROM users WHERE username = ? OR email = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setString(2, username); // 支持用户名或邮箱登录
                rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedPasswordHash = rs.getString("password_hash");
                    String inputPasswordHash = hashPassword(password);

                    if (storedPasswordHash.equals(inputPasswordHash)) {
                        // 登录成功
                        result.put("success", true);
                        result.put("message", "登录成功");
                        result.put("data", Map.of(
                            "user_id", rs.getLong("user_id"),
                            "username", rs.getString("username"),
                            "email", rs.getString("email")
                        ));
                    } else {
                        // 密码错误
                        result.put("success", false);
                        result.put("message", "密码错误");
                    }
                } else {
                    // 用户不存在
                    result.put("success", false);
                    result.put("message", "用户不存在");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("验证登录失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "登录验证失败");
        }

        return result;
    }

    /**
     * 获取用户资料 - UserHandler需要的方法
     */
    public Map<String, Object> getUserProfile(Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT user_id, username, email, phone, avatar, created_at, updated_at FROM users WHERE user_id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, userId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    result.put("success", true);
                    result.put("data", Map.of(
                        "user_id", rs.getLong("user_id"),
                        "username", rs.getString("username"),
                        "email", rs.getString("email"),
                        "phone", rs.getString("phone"),
                        "avatar", rs.getString("avatar"),
                        "created_at", rs.getTimestamp("created_at"),
                        "updated_at", rs.getTimestamp("updated_at")
                    ));
                } else {
                    result.put("success", false);
                    result.put("message", "用户不存在");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取用户资料失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "获取用户资料失败");
        }

        return result;
    }

    /**
     * 更新用户资料 - UserHandler需要的方法
     */
    public Map<String, Object> updateUserProfile(Long userId, String username, String email, String phone, String avatar) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查用户名和邮箱是否已被其他用户使用
            if (username != null && isUsernameExistsForOtherUser(username, userId)) {
                result.put("success", false);
                result.put("message", "用户名已被其他用户使用");
                result.put("status_code", 409);
                return result;
            }

            if (email != null && isEmailExistsForOtherUser(email, userId)) {
                result.put("success", false);
                result.put("message", "邮箱已被其他用户使用");
                result.put("status_code", 409);
                return result;
            }

            String sql = "UPDATE users SET username = COALESCE(?, username), " +
                        "email = COALESCE(?, email), " +
                        "phone = COALESCE(?, phone), " +
                        "avatar = COALESCE(?, avatar), " +
                        "updated_at = NOW() WHERE user_id = ?";

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setString(2, email);
                stmt.setString(3, phone);
                stmt.setString(4, avatar);
                stmt.setLong(5, userId);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    result.put("success", true);
                    result.put("message", "用户资料更新成功");

                    // 返回更新后的用户信息
                    Map<String, Object> updatedUser = getUserProfile(userId);
                    if ((Boolean) updatedUser.get("success")) {
                        result.put("data", updatedUser.get("data"));
                    }
                } else {
                    result.put("success", false);
                    result.put("message", "用户不存在");
                    result.put("status_code", 404);
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("更新用户资料失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "更新用户资料失败");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 修改密码 - UserHandler需要的方法
     */
    public Map<String, Object> changePassword(Long userId, String oldPassword, String newPassword, String confirmPassword) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证新密码和确认密码是否一致
            if (!newPassword.equals(confirmPassword)) {
                result.put("success", false);
                result.put("message", "新密码和确认密码不一致");
                result.put("status_code", 400);
                return result;
            }

            // 2. 验证旧密码
            String checkSql = "SELECT password_hash FROM users WHERE user_id = ?";
            Connection conn = null;
            PreparedStatement checkStmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setLong(1, userId);
                rs = checkStmt.executeQuery();

                if (rs.next()) {
                    String storedPasswordHash = rs.getString("password_hash");
                    String oldPasswordHash = hashPassword(oldPassword);

                    if (!storedPasswordHash.equals(oldPasswordHash)) {
                        result.put("success", false);
                        result.put("message", "旧密码错误");
                        result.put("status_code", 400);
                        return result;
                    }
                } else {
                    result.put("success", false);
                    result.put("message", "用户不存在");
                    result.put("status_code", 404);
                    return result;
                }

                // 3. 更新密码
                String updateSql = "UPDATE users SET password_hash = ?, updated_at = NOW() WHERE user_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, hashPassword(newPassword));
                updateStmt.setLong(2, userId);

                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected > 0) {
                    result.put("success", true);
                    result.put("message", "密码修改成功");
                } else {
                    result.put("success", false);
                    result.put("message", "密码修改失败");
                    result.put("status_code", 500);
                }

                updateStmt.close();

            } finally {
                DatabaseService.closeResources(conn, checkStmt, rs);
            }

        } catch (Exception e) {
            System.err.println("修改密码失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "修改密码失败");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 获取用户存储统计 - UserHandler需要的方法
     */
    public Map<String, Object> getUserStorageStats(Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT storage_used, storage_limit FROM users WHERE user_id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, userId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    long storageUsed = rs.getLong("storage_used");
                    long storageLimit = rs.getLong("storage_limit");

                    result.put("success", true);
                    result.put("data", Map.of(
                        "storage_used", storageUsed,
                        "storage_limit", storageLimit,
                        "storage_available", storageLimit - storageUsed,
                        "usage_percentage", storageLimit > 0 ? (storageUsed * 100.0 / storageLimit) : 0
                    ));
                } else {
                    result.put("success", false);
                    result.put("message", "用户不存在");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取存储统计失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "获取存储统计失败");
        }

        return result;
    }

    /**
     * 获取用户活动日志 - UserHandler需要的方法
     */
    public Map<String, Object> getUserActivityLog(Long userId, int page, int size, String actionType) {
        Map<String, Object> result = new HashMap<>();

        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM activity_logs WHERE user_id = ?");
            List<Object> params = new ArrayList<>();
            params.add(userId);

            if (actionType != null && !actionType.trim().isEmpty()) {
                sqlBuilder.append(" AND action_type = ?");
                params.add(actionType);
            }

            sqlBuilder.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
            params.add(size);
            params.add((page - 1) * size);

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sqlBuilder.toString());

                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }

                rs = stmt.executeQuery();

                List<Map<String, Object>> logs = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> log = new HashMap<>();
                    log.put("id", rs.getLong("id"));
                    log.put("action_type", rs.getString("action_type"));
                    log.put("description", rs.getString("description"));
                    log.put("ip_address", rs.getString("ip_address"));
                    log.put("user_agent", rs.getString("user_agent"));
                    log.put("created_at", rs.getTimestamp("created_at"));
                    logs.add(log);
                }

                result.put("success", true);
                result.put("data", Map.of(
                    "logs", logs,
                    "page", page,
                    "size", size,
                    "total", getTotalActivityCount(userId, actionType)
                ));

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取活动日志失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "获取活动日志失败");
        }

        return result;
    }

    // ================== 私有辅助方法 ==================

    /**
     * 检查用户名是否存在
     */
    private boolean isUsernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE username = ?";
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
            return false;

        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 检查邮箱是否存在
     */
    private boolean isEmailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";
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
            return false;

        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 检查用户名是否被其他用户使用
     */
    private boolean isUsernameExistsExcludeUser(String username, Long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE username = ? AND user_id != ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setLong(2, userId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 检查邮箱是否被其他用户使用
     */
    private boolean isEmailExistsExcludeUser(String email, Long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ? AND user_id != ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setLong(2, userId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 创建用户
     */
    private Map<String, Object> createUser(String username, String email, String password) throws SQLException {
        String sql = "INSERT INTO user (username, email, password, storage_limit, storage_used, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashPassword(password));
            stmt.setLong(4, 5368709120L); // 默认5GB存储空间
            stmt.setLong(5, 0L); // 初始使用0字节
            stmt.setString(6, "active");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    Long userId = rs.getLong(1);

                    // 返回创建的用户信息（不包含密码）
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("user_id", userId);
                    userData.put("username", username);
                    userData.put("email", email);
                    userData.put("storage_limit", 5368709120L);
                    userData.put("storage_used", 0L);
                    userData.put("status", "active");

                    return userData;
                }
            }
            return null;

        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 用户认证
     */
    private Map<String, Object> authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT user_id, username, email, password, storage_limit, storage_used, status, created_at, updated_at FROM user WHERE username = ? AND status = 'active'";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (verifyPassword(password, storedPassword)) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("user_id", rs.getLong("user_id"));
                    userData.put("username", rs.getString("username"));
                    userData.put("email", rs.getString("email"));
                    userData.put("storage_limit", rs.getLong("storage_limit"));
                    userData.put("storage_used", rs.getLong("storage_used"));
                    userData.put("status", rs.getString("status"));
                    userData.put("created_at", rs.getTimestamp("created_at"));
                    userData.put("updated_at", rs.getTimestamp("updated_at"));

                    return userData;
                }
            }
            return null;

        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 验证用户密码
     */
    private boolean verifyPassword(Long userId, String password) throws SQLException {
        String sql = "SELECT password FROM user WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = databaseService.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, userId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return verifyPassword(password, storedPassword);
            }
            return false;

        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
    }

    /**
     * 密码哈希
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("密码哈希失败", e);
        }
    }

    /**
     * 验证密码
     */
    private boolean verifyPassword(String inputPassword, String storedPassword) {
        String hashedInput = hashPassword(inputPassword);
        return hashedInput.equals(storedPassword);
    }

    /**
     * 更新用户存储使用量
     */
    public Map<String, Object> updateStorageUsage(Long userId, Long sizeChange) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "UPDATE user SET storage_used = storage_used + ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, sizeChange);
                stmt.setLong(2, userId);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    result.put("success", true);
                    result.put("message", "存储使用量更新成功");
                } else {
                    result.put("success", false);
                    result.put("message", "用户不存在");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("更新存储使用量失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 检查用户存储空间是否足够
     */
    public boolean hasEnoughStorage(Long userId, Long requiredSize) {
        try {
            String sql = "SELECT storage_limit - storage_used as available_storage FROM user WHERE user_id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, userId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    long availableStorage = rs.getLong("available_storage");
                    return availableStorage >= requiredSize;
                }
                return false;

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("检查存储空间失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取活动日志总数
     */
    private long getTotalActivityCount(Long userId, String actionType) {
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) FROM activity_logs WHERE user_id = ?");
            List<Object> params = new ArrayList<>();
            params.add(userId);

            if (actionType != null && !actionType.trim().isEmpty()) {
                sqlBuilder.append(" AND action_type = ?");
                params.add(actionType);
            }

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sqlBuilder.toString());

                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }

                rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getLong(1);
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取活动日志总数失败: " + e.getMessage());
        }

        return 0;
    }

    /**
     * 检查用户名是否已被其他用户使用
     */
    private boolean isUsernameExistsForOtherUser(String username, Long excludeUserId) {
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND user_id != ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setLong(2, excludeUserId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("检查用户名存在性失败: " + e.getMessage());
        }

        return false;
    }

    /**
     * 检查邮箱是否已被其他用户使用
     */
    private boolean isEmailExistsForOtherUser(String email, Long excludeUserId) {
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND user_id != ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, email);
                stmt.setLong(2, excludeUserId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("检查邮箱存在性失败: " + e.getMessage());
        }

        return false;
    }
}
