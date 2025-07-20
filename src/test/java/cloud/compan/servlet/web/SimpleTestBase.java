package cloud.compan.servlet.web;

import cloud.compan.servlet.config.AppModule;
import cloud.compan.servlet.config.DatabaseModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 简化的测试基类
 * 提供基本的Guice容器和依赖注入支持
 */
public abstract class SimpleTestBase {
    
    protected Injector injector;
    
    @BeforeEach
    public void setUp() {
        // 初始化Guice容器
        injector = Guice.createInjector(
            new AppModule(),
            new DatabaseModule()
        );
    }
    
    /**
     * 获取服务实例
     */
    protected <T> T getService(Class<T> serviceClass) {
        return injector.getInstance(serviceClass);
    }
    
    /**
     * 获取控制器实例
     */
    protected <T> T getController(Class<T> controllerClass) {
        return injector.getInstance(controllerClass);
    }
    
    /**
     * 验证实例不为空
     */
    protected void assertInjected(Object instance, String message) {
        assertNotNull(instance, message);
    }
} 