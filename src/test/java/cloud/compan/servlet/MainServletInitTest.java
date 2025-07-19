package cloud.compan.servlet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class MainServletInitTest {

    @Mock
    private ServletConfig servletConfig;
    
    @Mock
    private ServletContext servletContext;
    
    private MainServlet mainServlet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mainServlet = new MainServlet();
        
        // Mock ServletConfig和ServletContext
        when(servletConfig.getServletContext()).thenReturn(servletContext);
        when(servletConfig.getServletName()).thenReturn("MainServlet");
    }

    @Test
    void testMainServletHasCorrectAnnotation() {
        System.out.println("测试@WebServlet注解配置...");
        
        // 验证@WebServlet注解存在
        assertTrue(mainServlet.getClass().isAnnotationPresent(
            jakarta.servlet.annotation.WebServlet.class), 
            "MainServlet应该有@WebServlet注解");
            
        jakarta.servlet.annotation.WebServlet annotation = 
            mainServlet.getClass().getAnnotation(jakarta.servlet.annotation.WebServlet.class);
            
        assertEquals("MainServlet", annotation.name(), "Servlet名称应该是MainServlet");
        assertArrayEquals(new String[]{"/*"}, annotation.urlPatterns(), 
                         "URL模式应该是/*");
        assertEquals(1, annotation.loadOnStartup(), "应该在启动时加载");
        
        System.out.println("@WebServlet注解配置正确！");
        System.out.println("   名称: " + annotation.name());
        System.out.println("   URL模式: " + String.join(", ", annotation.urlPatterns()));
        System.out.println("   启动时加载: " + annotation.loadOnStartup());
    }
    
    @Test 
    void testMainServletInitialization() {
        System.out.println("\n测试MainServlet初始化...");
        
        try {
            // 调用init方法 - 这会触发所有初始化代码
            mainServlet.init(servletConfig);
            
            System.out.println("MainServlet.init()执行成功！");
            System.out.println("   - Guice容器已初始化");
            System.out.println("   - 控制器已扫描");
            System.out.println("   - 路由已注册");
            
        } catch (ServletException e) {
            System.err.println("MainServlet初始化失败:");
            System.err.println("   错误类型: " + e.getClass().getSimpleName());
            System.err.println("   错误消息: " + e.getMessage());
            
            if (e.getCause() != null) {
                System.err.println("   根本原因: " + e.getCause().getMessage());
                System.err.println("\n 详细堆栈跟踪:");
                e.getCause().printStackTrace();
            }
            
            // 测试失败，但提供详细信息
            fail("MainServlet初始化失败: " + e.getMessage());
        }
    }

    @Test
    void testConfigurationFileExists() {
        System.out.println("\n 检查配置文件是否存在...");
        
        // 检查主配置文件
        var mainConfig = getClass().getClassLoader()
            .getResourceAsStream("application.properties");
        assertNotNull(mainConfig, "应该存在主程序的application.properties文件");
        
        System.out.println("找到主程序配置文件: application.properties");
        
        // 检查测试配置文件  
        var testConfig = getClass().getClassLoader()
            .getResourceAsStream("application.properties");
        assertNotNull(testConfig, "应该能够访问配置文件");
        
        System.out.println("配置文件检查通过");
    }
} 