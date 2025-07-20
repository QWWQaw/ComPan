package cloud.compan.servlet.config;

import cloud.compan.servlet.service.impl.UserServiceImpl;
import cloud.compan.servlet.service.impl.AuthServiceImpl;
import cloud.compan.servlet.service.impl.FileServiceImpl;
import cloud.compan.servlet.utils.HashUtil;
import cloud.compan.servlet.utils.JdbcExecutor;
import cloud.compan.servlet.utils.JsonUtils;
import cloud.compan.servlet.utils.JwtUtil;
import cloud.compan.servlet.utils.JwtUtilImpl;
import cloud.compan.servlet.utils.ValidationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import cloud.compan.servlet.repository.*;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.service.FileService;
import cloud.compan.servlet.service.FolderService;
import cloud.compan.servlet.service.ShareService;
import cloud.compan.servlet.service.NotificationService;
import cloud.compan.servlet.service.AclService;
import cloud.compan.servlet.service.RecycleBinService;
import cloud.compan.servlet.service.StorageService;
import cloud.compan.servlet.service.UserService;
import cloud.compan.servlet.web.RouteRegistry;
import cloud.compan.servlet.web.RequestDispatcher;
import cloud.compan.servlet.web.ControllerScanner;


import com.google.inject.AbstractModule;
import cloud.compan.servlet.annotations.Value;
import com.google.inject.Singleton;
import cloud.compan.servlet.service.impl.*;
import cloud.compan.servlet.service.*;
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
        
        // ============ Web组件绑定 ============
        // 路由分发、控制器扫描等核心Web功能
        bind(RouteRegistry.class).in(Singleton.class);
        bind(RequestDispatcher.class).in(Singleton.class);
        bind(ControllerScanner.class).in(Singleton.class);
        bind(cloud.compan.servlet.converter.JsonHttpMessageConverter.class).in(Singleton.class);
         
        // ============ 数据层组件绑定 ============
        bind(GuiceDataSourceProvider.class);  

        // ============ JSON和工具类组件绑定 ============
        // 配置ObjectMapper为单例
        bind(ObjectMapper.class).toInstance(createObjectMapper());
        bind(JsonUtils.class).in(Singleton.class);
        
        bind(JwtUtil.class).to(JwtUtilImpl.class).asEagerSingleton();
        bind(ValidationUtil.class).in(Singleton.class);
        bind(JdbcExecutor.class);  
        bind(HashUtil.class);

        // ============ Repository组件绑定 ============
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
        
        // ============ Service层组件绑定 ============
        bind(UserService.class).to(UserServiceImpl.class).in(Singleton.class);
        bind(AuthService.class).to(AuthServiceImpl.class).in(Singleton.class);
        bind(FileService.class).to(FileServiceImpl.class).in(Singleton.class);
        bind(FolderService.class).to(FolderServiceImpl.class).in(Singleton.class);
        bind(StorageService.class).to(StorageServiceImpl.class).in(Singleton.class);
        bind(ShareService.class).to(ShareServiceImpl.class).in(Singleton.class);
        bind(NotificationService.class).to(NotificationServiceImpl.class).in(Singleton.class);
        bind(RecycleBinService.class).to(RecycleBinServiceImpl.class).in(Singleton.class);
        bind(AclService.class).to(AclServiceImpl.class).in(Singleton.class);
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
    
    /**
     * 创建和配置ObjectMapper实例
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 自动发现并注册模块（如时间处理模块等）
        mapper.findAndRegisterModules();
        
        // 可以添加更多配置
        // mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        System.out.println("🔧 ObjectMapper配置完成");
        return mapper;
    }
}