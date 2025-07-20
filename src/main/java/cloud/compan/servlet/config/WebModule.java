package cloud.compan.servlet.config;

import cloud.compan.servlet.web.RouteRegistry;
import cloud.compan.servlet.web.RequestDispatcher;
import cloud.compan.servlet.web.ControllerScanner;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * Web模块 - 管理所有Web相关组件的依赖注入
 * 负责路由分发、控制器扫描、请求处理等核心Web功能的配置
 */
public class WebModule extends AbstractModule {
    
    @Override
    protected void configure() {
        System.out.println(" 配置WebModule...");
        
        // 1. 绑定路由注册器 - 单例模式，线程安全
        bind(RouteRegistry.class).in(Singleton.class);
        System.out.println("RouteRegistry 已绑定为单例");
        
        // 2. 绑定请求分发器 - 单例模式，处理所有HTTP请求
        bind(RequestDispatcher.class).in(Singleton.class);
        System.out.println("RequestDispatcher 已绑定为单例");
        
        // 3. 绑定控制器扫描器 - 单例模式，启动时扫描控制器
        bind(ControllerScanner.class).in(Singleton.class);
        System.out.println("ControllerScanner 已绑定为单例");
        
        System.out.println("WebModule 配置完成");
    }
} 