package cloud.compan.servlet.config;

import cloud.compan.servlet.utils.HashUtil;
import cloud.compan.servlet.utils.JdbcExecutor;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Google Guice 模块，负责创建、配置和绑定数据源。
 * 它取代了手动的 DataSource 管理，并使其可被依赖注入。
 */
public class DatabaseModule extends AbstractModule {

    @Override  
    protected void configure() {  

    }  

    @Singleton  
    static class DefaultDataSourceProvider implements Provider<DataSource> {  
        private final AppModule config;  

        // Guice 会自动注入 AppConfig  
        @Inject  
        public DefaultDataSourceProvider(AppModule config) {  
            this.config = config;  
        }  

        @Override  
        public DataSource get() {  
            HikariConfig hikariConfig = new HikariConfig();  
            hikariConfig.setJdbcUrl(config.getDatabaseUrl());  
            hikariConfig.setUsername(config.getUsername());  
            hikariConfig.setPassword(config.getPassword());  
            hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
            // 推荐的连接池设置
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            hikariConfig.setMaximumPoolSize(10); // 设置最大连接数
            return new HikariDataSource(hikariConfig);  
        }  
    }  
}
