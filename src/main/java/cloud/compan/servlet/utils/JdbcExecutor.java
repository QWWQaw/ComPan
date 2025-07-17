package cloud.compan.servlet.utils;

import cloud.compan.servlet.config.GuiceDataSourceProvider;  
import cloud.compan.servlet.model.EntityMetadata;
import com.google.inject.Inject;  
import com.google.inject.Singleton;  

import javax.sql.DataSource;  
import java.sql.Connection;  
import java.sql.Statement;
import java.sql.PreparedStatement;  
import java.sql.ResultSet;  
import java.sql.SQLException;  
import java.util.ArrayList;  
import java.util.List;  
import java.util.Optional;  

/**
 * 一个通用的 JDBC 执行器，用于执行 SQL 查询和更新操作。
 * 它利用了 Guice 的依赖注入机制来获取数据源。
 */
@Singleton 
public class JdbcExecutor {  

    // 它依赖于一个能找到正确数据源的提供者  
    private final GuiceDataSourceProvider dataSourceProvider;  

    @Inject  
    public JdbcExecutor(GuiceDataSourceProvider dataSourceProvider) {  
        this.dataSourceProvider = dataSourceProvider;  
    }  

    @FunctionalInterface  
    public interface RowMapper<T> {  
        T map(ResultSet rs) throws SQLException;  
    }  

    /**
     * 执行一个查询操作，并返回一个 List 对象，如果查询结果为空，则返回空 List
     * @param <T>
     * @param entityClass
     * @param sql
     * @param rowMapper
     * @param params SQL 参数
     * @return
     */
    public <T> List<T> query(Class<?> entityClass, String sql, RowMapper<T> rowMapper, Object... params) {  
        // 使用注入的 provider 来获取 DataSource  
        DataSource ds = dataSourceProvider.get(entityClass);  
        List<T> results = new ArrayList<>();  
        try (Connection conn = ds.getConnection(); // 从连接池获取连接  
             PreparedStatement ps = conn.prepareStatement(sql)) {  
            setParameters(ps, params);  
            try (ResultSet rs = ps.executeQuery()) {  
                while (rs.next()) {  
                    results.add(rowMapper.map(rs));  
                }  
            }  
        } catch (SQLException e) {  
            throw new RuntimeException("Failed to execute query", e);  
        }  
        return results;  
    }  

    /**
     * 执行一个查询操作，并返回一个 Optional 对象，如果查询结果为空，则返回 Optional.empty()
     * @param <T>
     * @param entityClass
     * @param sql
     * @param rowMapper
     * @param params
     * @return
     */
    public <T> Optional<T> queryForObject(Class<?> entityClass, String sql, RowMapper<T> rowMapper, Object... params) {  
        List<T> results = query(entityClass, sql, rowMapper, params);  
        return results.stream().findFirst();  
    }  

    /**
     * 执行一个查询，返回一个对象列表。
     * @param <T> 实体类型
     * @param entityClass 实体类，用于获取数据源
     * @param sql SQL 查询语句
     * @param rowMapper 用于将 ResultSet 的每一行映射到对象的映射器
     * @param params 查询参数
     * @return 结果对象的列表；如果未找到，则返回空列表
     */
    public <T> List<T> queryForList(Class<?> entityClass, String sql, RowMapper<T> rowMapper, Object... params) {
        return query(entityClass, sql, rowMapper, params);
    }

    /**
     * 执行一个更新操作，并返回受影响的行数
     * @param entityClass
     * @param sql
     * @param params
     * @return
     */
    public int update(Class<?> entityClass, String sql, Object... params) {  
        DataSource ds = dataSourceProvider.get(entityClass);  
        try (Connection conn = ds.getConnection();  
             PreparedStatement ps = conn.prepareStatement(sql)) {  
            setParameters(ps, params);  
            return ps.executeUpdate();  
        } catch (SQLException e) {  
            throw new RuntimeException("Failed to execute update", e);  
        }  
    }  
    
    /**  
     * 一个方便的 RowMapper，它重用 EntityMetadata 的映射逻辑。  
     * 只有当 SQL 查询的列名与实体字段的 @Column 注解完全匹配时才有效。  
     * @param <T> 实体类型  
     */  
    public static <T> RowMapper<T> getEntityRowMapper(Class<T> entityClass) {  
        EntityMetadata metadata = EntityMetadata.of(entityClass);  
        // 返回一个实现了 RowMapper 接口的 lambda 表达式  
        return rs -> {  
            try {  
                return metadata.mapRowToObject(rs, entityClass);  
            } catch (Exception e) {  
                throw new SQLException("Failed to map row to object for " + entityClass.getSimpleName(), e);  
            }  
        };  
    }  

    private void setParameters(PreparedStatement ps, Object... params) throws SQLException {  
        for (int i = 0; i < params.length; i++) {  
            ps.setObject(i + 1, params[i]);  
        }  
    }  

    /**  
     * 执行一个更新操作 (特别是 INSERT)，并返回自动生成的主键。  
     * @param entityClass 用于定位数据源  
     * @param sql 要执行的 INSERT 语句  
     * @param params SQL 参数  
     * @return 生成的主键 (通常是 Long 类型)  
     * @throws SQLException 如果没有生成主键  
     */  
    public long updateAndGetKey(Class<?> entityClass, String sql, Object... params) {  
        DataSource ds = dataSourceProvider.get(entityClass); // 假设 dataSourceProvider 已注入  
        try (Connection conn = ds.getConnection();  
            // 关键点：传入 Statement.RETURN_GENERATED_KEYS  
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {  

            setParameters(ps, params);  
            int affectedRows = ps.executeUpdate();  

            if (affectedRows == 0) {  
                throw new SQLException("Creating entity failed, no rows affected.");  
            }  

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {  
                if (generatedKeys.next()) {  
                    // 返回第一个生成的主键  
                    return generatedKeys.getLong(1);  
                } else {  
                    throw new SQLException("Creating entity failed, no ID obtained.");  
                }  
            }  
        } catch (SQLException e) {  
            throw new RuntimeException("Failed to execute update and get key: " + sql, e);  
        }  
    } 
} 