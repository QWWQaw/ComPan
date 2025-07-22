package cloud.compan.servlet.config;

import javax.sql.DataSource;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Google Guice 模块，负责创建、配置和绑定数据源。
 * 它取代了手动的 DataSource 管理，并使其可被依赖注入。
 */
public class DatabaseModule extends AbstractModule {

    @Override  
    protected void configure() {  // 原本是空的，现在添加了绑定默认数据源
        // 绑定默认数据源
        bind(DataSource.class)
        .annotatedWith(Names.named("default"))
        .toProvider(DefaultDataSourceProvider.class)
        .in(Singleton.class);
    }  

    /**
     * 默认数据源提供者
     * 使用HikariCP作为连接池
     */
    @Singleton  
    static class DefaultDataSourceProvider implements Provider<DataSource> {  
        private final AppModule config;  

        // Guice 会自动注入 AppModule  
        @Inject  
        public DefaultDataSourceProvider(AppModule config) {  
            this.config = config;  
        }  

        /**
         * 获取数据源
         * @return 数据源
         */
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
