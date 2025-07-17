package cloud.compan.servlet.config;

import com.google.inject.Provider;

public class AppConfigProvider implements Provider<AppModule> {  

    /**  
     * Guice 将调用此方法来获取一个 AppConfig 实例。  
     */  
    @Override  
    public AppModule get() {  
        // 1. 创建一个普通的 AppConfig 实例  
        AppModule config = new AppModule();  
        
        // 2. 使用您现有的静态工具类来填充它  
        ConfigLoader.inject(config);  
        
        // 3. 返回完全配置好的实例  
        return config;  
    }  
}  