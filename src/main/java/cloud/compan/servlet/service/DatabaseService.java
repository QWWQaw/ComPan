package cloud.compan.servlet.service;

import java.sql.*;
import java.util.Properties;
import java.io.InputStream;

/**
 * 数据库服务类 - 处理MySQL数据库连接和查询
 */
public class DatabaseService {

    private static DatabaseService instance;
    private String dbUrl;
    private String username;
    private String password;

    private DatabaseService() {
        loadDatabaseConfig();
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            synchronized (DatabaseService.class) {
                if (instance == null) {
                    instance = new DatabaseService();
                }
            }
        }
        return instance;
    }

    /**
     * 加载数据库配置
     */
    private void loadDatabaseConfig() {
        // 读取配置文件 application.properties
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            Properties prop = new Properties();
            prop.load(input);

            this.dbUrl = prop.getProperty("db.connect.url");
            this.username = prop.getProperty("db.username");
            this.password = prop.getProperty("db.password");

            System.out.println("数据库配置加载成功: " + dbUrl);

        } catch (Exception e) {
            System.err.println("加载数据库配置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接
     */
    public Connection getConnection() throws SQLException {
        try {
            // 确保MySQL驱动已加载
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn = DriverManager.getConnection(dbUrl, username, password);
            System.out.println("数据库连接成功");
            return conn;

        } catch (ClassNotFoundException e) {
            System.err.println("MySQL驱动未找到: " + e.getMessage());
            throw new SQLException("MySQL驱动未找到", e);
        } catch (SQLException e) {
            System.err.println("数据库连接失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 测试数据库连接
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("数据库连接测试失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 关闭数据库资源
     */
    public static void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("关闭数据库资源失败: " + e.getMessage());
        }
    }
}
