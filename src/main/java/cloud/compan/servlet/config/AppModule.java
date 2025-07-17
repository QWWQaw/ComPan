package cloud.compan.servlet.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.field.Value;

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
        
        // 其他依赖也可在此绑定
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