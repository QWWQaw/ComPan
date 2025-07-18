package cloud.compan.servlet.test;

import cloud.compan.servlet.service.DatabaseService;
import cloud.compan.servlet.service.UserService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * 用户登录API测试工具
 */
public class LoginApiTester {

    private UserService userService;
    private DatabaseService databaseService;

    public LoginApiTester() {
        this.userService = new UserService();
        this.databaseService = DatabaseService.getInstance();
    }

    /**
     * 测试数据库连接
     */
    public void testDatabaseConnection() {
        System.out.println("=== 测试数据库连接 ===");
        boolean isConnected = databaseService.testConnection();
        if (isConnected) {
            System.out.println("✅ 数据库连接成功");
        } else {
            System.out.println("❌ 数据库连接失败");
        }
        System.out.println();
    }

    /**
     * 创建测试用户表
     */
    public void createTestUserTable() {
        System.out.println("=== 创建测试用户表 ===");
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS users (
                user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) NOT NULL UNIQUE,
                password_hash VARCHAR(255) NOT NULL,
                storage_limit BIGINT NOT NULL DEFAULT 10737418240,
                storage_used BIGINT NOT NULL DEFAULT 0,
                status ENUM('active', 'inactive', 'banned') NOT NULL DEFAULT 'active',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                last_login TIMESTAMP NULL,
                INDEX idx_username (username),
                INDEX idx_email (email),
                INDEX idx_status (status)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """;

        try (Connection conn = databaseService.getConnection();
             PreparedStatement stmt = conn.prepareStatement(createTableSQL)) {

            stmt.executeUpdate();
            System.out.println("✅ 用户表创建成功");

        } catch (SQLException e) {
            System.out.println("❌ 创建用户表失败: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * 插入测试用户数据
     */
    public void insertTestUsers() {
        System.out.println("=== 插入测试用户数据 ===");

        // 创建测试用户
        Map<String, Object> testUser1 = userService.createUser("testuser", "test@example.com", "password123");
        if (testUser1 != null) {
            System.out.println("✅ 测试用户1创建成功: testuser");
        } else {
            System.out.println("⚠️ 测试用户1可能已存在或创建失败");
        }

        Map<String, Object> testUser2 = userService.createUser("admin", "admin@example.com", "admin123");
        if (testUser2 != null) {
            System.out.println("✅ 测试用户2创建成功: admin");
        } else {
            System.out.println("⚠️ 测试用户2可能已存在或创建失败");
        }
        System.out.println();
    }

    /**
     * 测试用户登录功能
     */
    public void testUserLogin() {
        System.out.println("=== 测试用户登录功能 ===");

        // 测试正确的用户名和密码
        System.out.println("1. 测试正确的用户名和密码:");
        Map<String, Object> loginResult1 = userService.authenticateUser("testuser", "password123");
        if (loginResult1 != null) {
            System.out.println("✅ 登录成���: " + loginResult1.get("username"));
            System.out.println("   用户ID: " + loginResult1.get("user_id"));
            System.out.println("   邮箱: " + loginResult1.get("email"));
            System.out.println("   状态: " + loginResult1.get("status"));
        } else {
            System.out.println("❌ 登录失败");
        }

        // 测试错误的密码
        System.out.println("\\n2. 测试错误的密码:");
        Map<String, Object> loginResult2 = userService.authenticateUser("testuser", "wrongpassword");
        if (loginResult2 == null) {
            System.out.println("✅ 正确拒绝了错误密码");
        } else {
            System.out.println("❌ 错误：接受了错误的密码");
        }

        // 测试不存在的用户
        System.out.println("\\n3. 测试不存在的用户:");
        Map<String, Object> loginResult3 = userService.authenticateUser("nonexistent", "password");
        if (loginResult3 == null) {
            System.out.println("✅ 正确拒绝了不存在的用户");
        } else {
            System.out.println("❌ 错误：接受了不存在的用户");
        }
        System.out.println();
    }

    /**
     * 测试用户注册功能
     */
    public void testUserRegistration() {
        System.out.println("=== 测试用户注册功能 ===");

        // 测试注册新用户
        System.out.println("1. 测试注册新用户:");
        Map<String, Object> newUser = userService.createUser("newuser", "newuser@example.com", "newpassword");
        if (newUser != null) {
            System.out.println("✅ 新用户注册成功: " + newUser.get("username"));
        } else {
            System.out.println("❌ 新用户注册失败");
        }

        // 测试重复用户名
        System.out.println("\\n2. 测试重复用户名:");
        boolean usernameExists = userService.isUsernameExists("testuser");
        if (usernameExists) {
            System.out.println("✅ 正确检测到用户名已存在");
        } else {
            System.out.println("❌ 错误：未检测到用户名已存在");
        }

        // 测试重复邮箱
        System.out.println("\\n3. 测试重复邮箱:");
        boolean emailExists = userService.isEmailExists("test@example.com");
        if (emailExists) {
            System.out.println("✅ 正确检测到邮箱已存在");
        } else {
            System.out.println("❌ 错误：未检测到邮箱已存在");
        }
        System.out.println();
    }

    /**
     * 主测试方法
     */
    public static void main(String[] args) {
        System.out.println("开始测试用户登录API功能...");
        System.out.println("=".repeat(50));

        LoginApiTester tester = new LoginApiTester();

        try {
            // 1. 测试数据库连接
            tester.testDatabaseConnection();

            // 2. 创建用户表
            tester.createTestUserTable();

            // 3. 插入测试数据
            tester.insertTestUsers();

            // 4. 测试注册功能
            tester.testUserRegistration();

            // 5. 测试登录功能
            tester.testUserLogin();

            System.out.println("=".repeat(50));
            System.out.println("所有测试完成！");

        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
