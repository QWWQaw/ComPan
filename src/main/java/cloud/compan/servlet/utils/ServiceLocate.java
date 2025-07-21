package cloud.compan.servlet.utils;

import com.google.inject.Injector;

/**
 * 服务定位器
 * 使用主应用的Guice注入器来获取服务实例
 * 提供静态访问点，直接使用注入器获取实例
 */
public class ServiceLocate {
    private static Injector injector;
    private static volatile boolean initialized = false;

    /**
     * 初始化服务定位器，设置主应用的注入器
     * 这个方法应该在应用启动时由MainServlet调用
     * 
     * @param mainInjector 主应用的Guice注入器
     */
    public static void initialize(Injector mainInjector) {
        if (initialized) {
            return; // 避免重复初始化
        }
        
        synchronized (ServiceLocate.class) {
            if (!initialized) {
                injector = mainInjector;
                initialized = true;
                System.out.println("ServiceLocate initialized with main injector");
            }
        }
    }

    /**
     * 获取服务实例
     * 直接从注入器获取，不进行缓存
     * 
     * @param serviceClass 服务接口类
     * @return 服务实现实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> serviceClass) {
        if (!initialized) {
            throw new IllegalStateException("ServiceLocate not initialized. Call initialize() first.");
        }
        
        try {
            return injector.getInstance(serviceClass);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get service instance for " + serviceClass.getName(), e);
        }
    }

    /**
     * 检查是否已初始化
     * 
     * @return 是否已初始化
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * 获取当前注入器（主要用于调试）
     * 
     * @return 当前注入器
     */
    public static Injector getInjector() {
        return injector;
    }
}