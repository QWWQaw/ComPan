package cloud.compan.servlet.service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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
            // 1. 验证当前密码
            String sql = "SELECT password_hash FROM user WHERE user_id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, userId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    String currentPasswordHash = rs.getString("password_hash");

                    // 简单密码验证（实际项目中应该使用加密验证）
                    if (!oldPassword.equals(currentPasswordHash)) {
                        result.put("success", false);
                        result.put("message", "当前密码错误");
                        return result;
                    }

                    // 2. 更新密码
                    String updateSql = "UPDATE user SET password_hash = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, newPassword); // 实际项目中应该加密
                        updateStmt.setLong(2, userId);

                        int affectedRows = updateStmt.executeUpdate();
                        if (affectedRows > 0) {
                            result.put("success", true);
                            result.put("message", "密码修改成功");
                        } else {
                            result.put("success", false);
                            result.put("message", "密码修改失败");
                        }
                    }
                } else {
                    result.put("success", false);
                    result.put("message", "用户不存在");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
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
     * 获取用户存储统计 - UserHandler中需要的方法
     */
    public Map<String, Object> getUserStorageStats(Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT storage_limit, storage_used FROM user WHERE user_id = ?";
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = databaseService.getConnection();
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, userId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    long storageLimit = rs.getLong("storage_limit");
                    long storageUsed = rs.getLong("storage_used");
                    long storageAvailable = storageLimit - storageUsed;
                    double storagePercentage = storageLimit > 0 ? (double) storageUsed / storageLimit * 100 : 0;

                    // 获取文件和文件夹统计
                    Map<String, Object> counts = getFileAndFolderCounts(userId, conn);

                    Map<String, Object> statsData = new HashMap<>();
                    statsData.put("storage_limit", storageLimit);
                    statsData.put("storage_used", storageUsed);
                    statsData.put("storage_available", storageAvailable);
                    statsData.put("storage_percentage", Math.round(storagePercentage * 100.0) / 100.0);
                    statsData.put("file_count", counts.get("file_count"));
                    statsData.put("folder_count", counts.get("folder_count"));

                    result.put("success", true);
                    result.put("message", "获取存储统计成功");
                    result.put("data", statsData);
                } else {
                    result.put("success", false);
                    result.put("message", "用户不存在");
                }

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取存储统计失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
    }

    /**
     * 获取用户活动日志 - UserHandler中需要的方法
     */
    public Map<String, Object> getUserActivityLog(Long userId, int page, int perPage, String operation) {
        Map<String, Object> result = new HashMap<>();

        try {
            int offset = (page - 1) * perPage;

            StringBuilder sqlBuilder = new StringBuilder(
                "SELECT id, operation, details, ip_address, performed_at FROM log WHERE user_id = ?"
            );
            List<Object> params = new ArrayList<>();
            params.add(userId);

            if (operation != null && !operation.trim().isEmpty()) {
                sqlBuilder.append(" AND operation LIKE ?");
                params.add("%" + operation + "%");
            }

            sqlBuilder.append(" ORDER BY performed_at DESC LIMIT ? OFFSET ?");
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
                    log.put("id", rs.getLong("id"));
                    log.put("operation", rs.getString("operation"));
                    log.put("details", rs.getString("details"));
                    log.put("ip_address", rs.getString("ip_address"));
                    log.put("performed_at", rs.getTimestamp("performed_at"));
                    logs.add(log);
                }

                // 获取总数
                int totalCount = getTotalLogCount(userId, operation, conn);

                Map<String, Object> pagination = new HashMap<>();
                pagination.put("current_page", page);
                pagination.put("per_page", perPage);
                pagination.put("total", totalCount);
                pagination.put("total_pages", (totalCount + perPage - 1) / perPage);
                pagination.put("has_next", offset + perPage < totalCount);
                pagination.put("has_prev", page > 1);

                Map<String, Object> logData = new HashMap<>();
                logData.put("items", logs);
                logData.put("pagination", pagination);

                result.put("success", true);
                result.put("message", "获取活动日志成功");
                result.put("data", logData);

            } finally {
                DatabaseService.closeResources(conn, stmt, rs);
            }

        } catch (Exception e) {
            System.err.println("获取活动日志失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
        }

        return result;
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

    /**
     * 辅助方法：检查用户名是否已存在（排除指定用户）
     */
    private boolean isUsernameExistsExcludeUser(String username, Long excludeUserId) {
        String sql = "SELECT COUNT(*) FROM user WHERE username = ? AND user_id != ?";
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

        } catch (SQLException e) {
            System.err.println("检查用户名是否存在失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }

        return false;
    }

    /**
     * 辅助方法：检查邮箱是否已存在（排除指定用户）
     */
    private boolean isEmailExistsExcludeUser(String email, Long excludeUserId) {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ? AND user_id != ?";
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

        } catch (SQLException e) {
            System.err.println("检查邮箱是否存在失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseService.closeResources(conn, stmt, rs);
        }

        return false;
    }

    /**
     * 辅助方法：获取文件和文件夹数量统计
     */
    private Map<String, Object> getFileAndFolderCounts(Long userId, Connection conn) throws SQLException {
        Map<String, Object> counts = new HashMap<>();

        // 获取文件数量
        String fileSql = "SELECT COUNT(*) FROM file WHERE uploader_id = ? AND status = 'active'";
        try (PreparedStatement stmt = conn.prepareStatement(fileSql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                counts.put("file_count", rs.next() ? rs.getInt(1) : 0);
            }
        }

        // 获取文件夹数量
        String folderSql = "SELECT COUNT(*) FROM folder WHERE owner_id = ? AND status = 'active'";
        try (PreparedStatement stmt = conn.prepareStatement(folderSql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                counts.put("folder_count", rs.next() ? rs.getInt(1) : 0);
            }
        }

        return counts;
    }

    /**
     * 辅助方法：获取日志总数
     */
    private int getTotalLogCount(Long userId, String operation, Connection conn) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) FROM log WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (operation != null && !operation.trim().isEmpty()) {
            sqlBuilder.append(" AND operation LIKE ?");
            params.add("%" + operation + "%");
        }

        try (PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
