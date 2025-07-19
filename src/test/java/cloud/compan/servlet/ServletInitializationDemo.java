package cloud.compan.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

/**
 * Servlet初始化演示 - 验证MainServlet.init()方法是否正常工作
 */
public class ServletInitializationDemo {
    
    @Mock
    private ServletConfig servletConfig;
    
    @Mock 
    private ServletContext servletContext;
    
    public static void main(String[] args) {
        new ServletInitializationDemo().runDemo();
    }
    
    public void runDemo() {
        System.out.println("=".repeat(60));
        System.out.println("MainServlet 初始化演示");
        System.out.println("=".repeat(60));
        
        try {
            // 初始化Mock对象
            MockitoAnnotations.openMocks(this);
            when(servletConfig.getServletContext()).thenReturn(servletContext);
            when(servletConfig.getServletName()).thenReturn("MainServlet");
            
            // 创建MainServlet实例
            MainServlet mainServlet = new MainServlet();
            
            System.out.println("检查@WebServlet注解配置...");
            checkWebServletAnnotation(mainServlet);
            
            System.out.println("\n 开始初始化MainServlet...");
            System.out.println("-".repeat(40));
            
            // 调用init方法
            mainServlet.init(servletConfig);
            
            System.out.println("-".repeat(40));
            System.out.println(" MainServlet初始化成功！");
            
            System.out.println("\n 模拟HTTP请求处理...");
            // 这里可以添加HTTP请求模拟
            
        } catch (ServletException e) {
            System.err.println(" Servlet初始化失败:");
            System.err.println("错误类型: " + e.getClass().getSimpleName());
            System.err.println("错误消息: " + e.getMessage());
            
            if (e.getCause() != null) {
                System.err.println("根本原因: " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }
        } catch (Exception e) {
            System.err.println(" 意外错误:");
            e.printStackTrace();
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println(" 演示结束");
        System.out.println("=".repeat(60));
    }
    
    private void checkWebServletAnnotation(MainServlet servlet) {
        Class<?> servletClass = servlet.getClass();
        
        if (servletClass.isAnnotationPresent(jakarta.servlet.annotation.WebServlet.class)) {
            jakarta.servlet.annotation.WebServlet annotation = 
                servletClass.getAnnotation(jakarta.servlet.annotation.WebServlet.class);
            
            System.out.println(" @WebServlet注解存在");
            System.out.println("   名称: " + annotation.name());
            System.out.println("   URL模式: " + String.join(", ", annotation.urlPatterns()));
            System.out.println("   启动时加载: " + annotation.loadOnStartup());
        } else {
            System.out.println(" @WebServlet注解缺失");
        }
    }
} 