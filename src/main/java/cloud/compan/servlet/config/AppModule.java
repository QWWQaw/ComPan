package cloud.compan.servlet.config;

import cloud.compan.servlet.utils.HashUtil;
import cloud.compan.servlet.utils.JdbcExecutor;
import cloud.compan.servlet.utils.JwtUtil;
import cloud.compan.servlet.utils.JwtUtilImpl;
import cloud.compan.servlet.utils.ValidationUtil;

import cloud.compan.servlet.repository.*;


import com.google.inject.AbstractModule;
import cloud.compan.servlet.annotations.Value;
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
        
        
         
        bind(GuiceDataSourceProvider.class);  

        bind(JwtUtil.class).to(JwtUtilImpl.class).asEagerSingleton();
        bind(ValidationUtil.class).in(Singleton.class);
        bind(JdbcExecutor.class);  
        bind(HashUtil.class);

        // Bind repositories here
        bind(UserRepository.class);
        bind(StorageObjectRepository.class);
        bind(UserGroupRepository.class);
        bind(UserGroupMemberRepository.class);
        bind(FolderRepository.class);
        bind(FileRepository.class);
        bind(AclRepository.class);
        bind(ShareRepository.class);
        bind(LogRepository.class);
        bind(NotificationRepository.class);
    }

    public String getDefaultDataSourceName() {  
        return defaultDataSourceName != null ? defaultDataSourceName : "No data source provided";  
    }  

    /**
     * 获取数据库URL
     * @return 数据库URL
     */
    public String getDatabaseUrl() {  
        return db_url;  
    }  

    /**
     * 获取数据库用户名
     * @return 数据库用户名
     */
    public String getUsername() {  
        return db_username;  
    }  

    /**
     * 获取数据库密码
     * @return 数据库密码
     */
    public String getPassword() {  
        return db_password;  
    }  
}