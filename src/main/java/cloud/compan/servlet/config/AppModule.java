package cloud.compan.servlet.config;

import cloud.compan.servlet.utils.HashUtil;
import cloud.compan.servlet.utils.JdbcExecutor;
import cloud.compan.servlet.utils.ValidationUtil;


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
        
        bind(ValidationUtil.class).in(Singleton.class);
         
        bind(GuiceDataSourceProvider.class);  
        bind(JdbcExecutor.class);  
        bind(HashUtil.class);
        bind(cloud.compan.servlet.repository.UserRepository.class);
        bind(cloud.compan.servlet.repository.StorageObjectRepository.class);
        bind(cloud.compan.servlet.repository.UserGroupRepository.class);
        bind(cloud.compan.servlet.repository.UserGroupMemberRepository.class);
        bind(cloud.compan.servlet.repository.FolderRepository.class);
        bind(cloud.compan.servlet.repository.FileRepository.class);
        bind(cloud.compan.servlet.repository.AclRepository.class);
        bind(cloud.compan.servlet.repository.ShareRepository.class);
        bind(cloud.compan.servlet.repository.LogRepository.class);
        bind(cloud.compan.servlet.repository.NotificationRepository.class);
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