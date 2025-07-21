package cloud.compan.servlet.web;

import cloud.compan.servlet.config.AppModule;
import cloud.compan.servlet.config.DatabaseModule;
import cloud.compan.servlet.web.RequestDispatcher;
import cloud.compan.servlet.web.ControllerScanner;
import cloud.compan.servlet.web.RouteRegistry;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import static org.mockito.Mockito.*;

/**
 * Web测试基类
 * 为控制器测试提供基础设施，模拟HTTP请求处理流程
 */
public abstract class WebTestBase {

    protected Injector injector;
    protected RequestDispatcher requestDispatcher;
    protected RouteRegistry routeRegistry;

    @Mock
    protected HttpServletRequest request;

    @Mock
    protected HttpServletResponse response;

    protected StringWriter responseWriter;
    protected PrintWriter printWriter;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // 加载测试配置文件
        Properties testProperties = loadTestProperties();

        // 初始化Guice容器
        injector = Guice.createInjector(
                new ConfigModule(testProperties),
                new AppModule(),
                new DatabaseModule()
        );

        // 获取核心组件
        requestDispatcher = injector.getInstance(RequestDispatcher.class);
        routeRegistry = injector.getInstance(RouteRegistry.class);

        // 扫描控制器
        ControllerScanner controllerScanner = injector.getInstance(ControllerScanner.class);
        controllerScanner.scanAndRegister("cloud.compan.servlet.controller");

        // 设置响应写入器
        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(response.getCharacterEncoding()).thenReturn("UTF-8");
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
                "jwt.secretkey"
        };

        for (String prop : requiredProps) {
            if (!properties.containsKey(prop)) {
                throw new RuntimeException("缺少必需的配置属性: " + prop);
            }
        }
    }

    /**
     * 模拟GET请求
     */
    protected MockHttpRequest get(String path) {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn(path);
        return new MockHttpRequest(request, response, requestDispatcher, responseWriter);
    }

    /**
     * 模拟POST请求
     */
    protected MockHttpRequest post(String path) {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn(path);
        return new MockHttpRequest(request, response, requestDispatcher, responseWriter);
    }

    /**
     * 模拟PUT请求
     */
    protected MockHttpRequest put(String path) {
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURI()).thenReturn(path);
        return new MockHttpRequest(request, response, requestDispatcher, responseWriter);
    }

    /**
     * 模拟DELETE请求
     */
    protected MockHttpRequest delete(String path) {
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURI()).thenReturn(path);
        return new MockHttpRequest(request, response, requestDispatcher, responseWriter);
    }

    /**
     * 模拟PATCH请求
     */
    protected MockHttpRequest patch(String path) {
        when(request.getMethod()).thenReturn("PATCH");
        when(request.getRequestURI()).thenReturn(path);
        return new MockHttpRequest(request, response, requestDispatcher, responseWriter);
    }
}