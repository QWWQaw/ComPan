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
     * 获取用户信息 - UserHandler中需要的方法
     */
    public Map<String, Object> getUserProfile(Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT user_id, username, email, storage_limit, storage_used, status, created_at, updated_at FROM user WHERE user_id = ? AND status = 'active'";
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
                    userData.put("created_at", rs.getTimestamp("created_at"));
                    userData.put("updated_at", rs.getTimestamp("updated_at"));

                    result.put("success", true);
                    result.put("message", "获取用户信息成功");
                    result.put("data", userData);
                } else {
                    result.put("success", false);
                    result.put("message", "用户不存在");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 更新用户信息 - UserHandler中需要的方法
     */
    public Map<String, Object> updateUserProfile(Long userId, String username, String email) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查新用户名是否被其他用户使用
            if (username != null && isUsernameExistsExcludeUser(username, userId)) {
                result.put("success", false);
                result.put("message", "用户名已被使用");
                return result;
            }

            // 检查新邮箱是否被其他用户使用
            if (email != null && isEmailExistsExcludeUser(email, userId)) {
                result.put("success", false);
                result.put("message", "邮箱已被使用");
                return result;
            }

            StringBuilder sqlBuilder = new StringBuilder("UPDATE user SET updated_at = CURRENT_TIMESTAMP");
            List<Object> params = new ArrayList<>();

            if (username != null && !username.trim().isEmpty()) {
                sqlBuilder.append(", username = ?");
                params.add(username);
            }

            if (email != null && !email.trim().isEmpty()) {
                sqlBuilder.append(", email = ?");
                params.add(email);
            }

            sqlBuilder.append(" WHERE user_id = ?");
            params.add(userId);

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sqlBuilder.toString());

                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    // 获取更新后的用户信息
                    Map<String, Object> userProfile = getUserProfile(userId);
                    if ((Boolean) userProfile.get("success")) {
                        result.put("success", true);
                        result.put("message", "用户信息更新成功");
                        result.put("data", userProfile.get("data"));
                    } else {
                        result.put("success", false);
                        result.put("message", "更新成功但获取信息失败");
                    }
                } else {
                    result.put("success", false);
                    result.put("message", "更新失败");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("更新用户信息失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 修改密码 - UserHandler中需要的方法
     */
    public Map<String, Object> changePassword(Long userId, String oldPassword, String newPassword) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证旧密码
            if (!verifyPassword(userId, oldPassword)) {
                result.put("success", false);
                result.put("message", "原密码错误");
                return result;
            }

            // 更新密码
            String sql = "UPDATE user SET password = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, hashPassword(newPassword));
                stmt.setLong(2, userId);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    result.put("success", true);
                    result.put("message", "密码修改成功");
                } else {
                    result.put("success", false);
                    result.put("message", "密码修改失败");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, null);
            }

        } catch (Exception e) {
            System.err.println("修改密码失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 获取用户存储统计信息
     */
    public Map<String, Object> getUserStorageStats(Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT storage_limit, storage_used, (storage_limit - storage_used) as storage_available FROM user WHERE user_id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, userId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    Map<String, Object> storageStats = new HashMap<>();
                    storageStats.put("storage_limit", rs.getLong("storage_limit"));
                    storageStats.put("storage_used", rs.getLong("storage_used"));
                    storageStats.put("storage_available", rs.getLong("storage_available"));

                    // 计算使用率百分比
                    long limit = rs.getLong("storage_limit");
                    long used = rs.getLong("storage_used");
                    double usagePercentage = limit > 0 ? (double) used / limit * 100 : 0;
                    storageStats.put("usage_percentage", Math.round(usagePercentage * 100.0) / 100.0);

                    result.put("success", true);
                    result.put("message", "获取存储统计信息成功");
                    result.put("data", storageStats);
                } else {
                    result.put("success", false);
                    result.put("message", "用户不存在");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取用户存储统计失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 获取用户活动日志
     */
    public Map<String, Object> getUserActivityLog(Long userId, int page, int perPage, String action) {
        Map<String, Object> result = new HashMap<>();

        try {
            int offset = (page - 1) * perPage;

            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT log_id, action, details, ip_address, user_agent, created_at FROM log WHERE user_id = ?");

            List<Object> params = new ArrayList<>();
            params.add(userId);

            if (action != null && !action.trim().isEmpty()) {
                sqlBuilder.append(" AND action = ?");
                params.add(action);
            }

            sqlBuilder.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
            params.add(perPage);
            params.add(offset);

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
                    log.put("log_id", rs.getLong("log_id"));
                    log.put("action", rs.getString("action"));
                    log.put("details", rs.getString("details"));
                    log.put("ip_address", rs.getString("ip_address"));
                    log.put("user_agent", rs.getString("user_agent"));
                    log.put("created_at", rs.getTimestamp("created_at"));
                    logs.add(log);
                }

                // 获取总数
                int totalCount = getUserLogCount(userId, action);

                Map<String, Object> pagination = new HashMap<>();
                pagination.put("current_page", page);
                pagination.put("per_page", perPage);
                pagination.put("total", totalCount);
                pagination.put("total_pages", (totalCount + perPage - 1) / perPage);
                pagination.put("has_next", offset + perPage < totalCount);
                pagination.put("has_prev", page > 1);

                Map<String, Object> logData = new HashMap<>();
                logData.put("logs", logs);
                logData.put("pagination", pagination);

                result.put("success", true);
                result.put("message", "获取活动日志成功");
                result.put("data", logData);

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取用户活动日志失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 获取用户日志总数（私有辅助方法）
     */
    private int getUserLogCount(Long userId, String action) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT COUNT(*) FROM log WHERE user_id = ?");

        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (action != null && !action.trim().isEmpty()) {
            sqlBuilder.append(" AND action = ?");
            params.add(action);
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
                return rs.getInt(1);
            }
            return 0;

        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }
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
}
