package cloud.compan.servlet.web;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.junit.jupiter.api.BeforeEach;
import cloud.compan.servlet.config.AppModule;
import cloud.compan.servlet.config.DatabaseModule;

/**
 * 简化的测试基类
 * 提供基本的Guice容器和依赖注入支持
 */
public abstract class SimpleTestBase {

    protected Injector injector;

    @BeforeEach
    public void setUp() {
        // 加载测试配置文件
        Properties testProperties = loadTestProperties();

        // 初始化Guice容器
        injector = Guice.createInjector(
                new ConfigModule(testProperties),
                new AppModule(),
                new DatabaseModule()
        );
    }

    /**
     * 配置模块 - 专门用于绑定配置属性
     */
    private static class ConfigModule extends AbstractModule {
        private final Properties properties;

        public ConfigModule(Properties properties) {
            this.properties = properties;
        }

        @Override
        protected void configure() {
            // 绑定所有配置属性
            Names.bindProperties(binder(), properties);

            // 显式绑定关键属性
            bindConstant().annotatedWith(Names.named("db.default.name"))
                    .to(properties.getProperty("db.default.name"));
            bindConstant().annotatedWith(Names.named("db.default.url"))
                    .to(properties.getProperty("db.default.url"));
            bindConstant().annotatedWith(Names.named("db.default.username"))
                    .to(properties.getProperty("db.default.username"));
            bindConstant().annotatedWith(Names.named("db.default.password"))
                    .to(properties.getProperty("db.default.password"));
        }
    }

    /**
     * 加载测试配置文件
     */
    private Properties loadTestProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new RuntimeException("测试配置文件 application.properties 未找到");
            }

            properties.load(input);

            // 验证必需属性存在
            validateRequiredProperties(properties);

            return properties;
        } catch (IOException e) {
            throw new RuntimeException("加载测试配置文件失败", e);
        }
    }

    /**
     * 验证必需属性存在
     */
    private void validateRequiredProperties(Properties properties) {
        String[] requiredProps = {
                "db.default.name",
                "db.default.url",
                "db.default.username",
                "jwt.secret"
        };

        for (String prop : requiredProps) {
            if (!properties.containsKey(prop)) {
                throw new RuntimeException("缺少必需的配置属性: " + prop);
            }
        }
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