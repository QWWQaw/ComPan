package cloud.compan.servlet.web;

import cloud.compan.servlet.config.AppModule;
import cloud.compan.servlet.config.DatabaseModule;
import cloud.compan.servlet.web.RequestDispatcher;
import cloud.compan.servlet.web.ControllerScanner;
import cloud.compan.servlet.web.RouteRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

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
        
        // 初始化Guice容器
        injector = Guice.createInjector(
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