package cloud.compan.servlet.config;

import cloud.compan.servlet.utils.HashUtil;
import cloud.compan.servlet.utils.JdbcExecutor;


import com.google.inject.AbstractModule;
import cloud.compan.servlet.annotations.data.field.Value;
import com.google.inject.Singleton;

public class AppModule extends AbstractModule {

    @Value("db.default.name")
    private String defaultDataSourceName;

    @Value("db.connect.url")
    private String db_url;

    @Value("db.username")
    private String db_username;

    @Value("db.password")
    private String db_password;

    @Override
    protected void configure() {
        ConfigLoader.inject(this);
        
        bind(AppModule.class).toProvider(AppConfigProvider.class).in(Singleton.class);  
        

        // 3. 绑定其他服务  
        bind(GuiceDataSourceProvider.class);  
        bind(JdbcExecutor.class);  
        bind(HashUtil.class);
        bind(cloud.compan.servlet.repository.UserRepository.class);
    }

    public String getDefaultDataSourceName() {  
        return defaultDataSourceName != null ? defaultDataSourceName : "No data source provided";  
    }  

    public String getDatabaseUrl() {  
        return db_url;  
    }  

    public String getUsername() {  
        return db_username;  
    }  

    public String getPassword() {  
        return db_password;  
    }  
}