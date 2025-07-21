package cloud.compan.servlet.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Value;
import cloud.compan.servlet.utils.HashUtil;
import cloud.compan.servlet.utils.JdbcExecutor;
import cloud.compan.servlet.utils.JsonUtils;
import cloud.compan.servlet.utils.JwtUtil;
import cloud.compan.servlet.utils.JwtUtilImpl;
import cloud.compan.servlet.utils.ValidationUtil;
import cloud.compan.servlet.web.ControllerScanner;
import cloud.compan.servlet.web.RequestDispatcher;
import cloud.compan.servlet.web.RouteRegistry;

/**
 * 主应用模块
 * 使用AutoScanModule自动注册Service和Controller组件
 */
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
        
        // 安装自动扫描模块，自动注册@Service和@Controller注解的类
        install(new AutoScanModule("cloud.compan.servlet"));
        
        bind(AppModule.class).toProvider(AppConfigProvider.class).in(Singleton.class);  
        
        // ============ Web组件绑定 ============
        // 路由分发、控制器扫描等核心Web功能
        bind(RouteRegistry.class).asEagerSingleton();
        bind(RequestDispatcher.class).asEagerSingleton();
        bind(ControllerScanner.class).asEagerSingleton();
        bind(cloud.compan.servlet.converter.JsonHttpMessageConverter.class).asEagerSingleton();
         
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
        // Repository组件已由AutoScanModule自动扫描和注册
        // 不再需要手动绑定
        

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
        
        if (Boolean.getBoolean("debug.components")) System.out.println("ObjectMapper configuration completed");
        return mapper;
    }
}