package cloud.compan.servlet;

import cloud.compan.servlet.config.AppModule;
import cloud.compan.servlet.config.DatabaseModule;
import cloud.compan.servlet.web.RequestDispatcher;
import cloud.compan.servlet.web.ControllerScanner;
import com.google.inject.Guice;
import com.google.inject.Injector;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * 主Servlet - 应用程序的入口点
 * 负责初始化Guice容器、扫描控制器、分发HTTP请求
 */
@WebServlet(name = "MainServlet", urlPatterns = {"/*"}, loadOnStartup = 1)
public class MainServlet extends HttpServlet {
    
    private Injector injector;
    private RequestDispatcher requestDispatcher;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        System.out.println("正在初始化 MainServlet...");
        super.init(config);


        try {
            // 1. 初始化Guice注入器
            initializeGuice();
            
            // 2. 扫描并注册控制器
            scanControllers();
            
            System.out.println("MainServlet 初始化完成！");
            
        } catch (Exception e) {
            System.err.println("MainServlet 初始化失败: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("MainServlet 初始化失败", e);
        }
    }
    
    /**
     * 初始化Guice依赖注入容器
     */
    private void initializeGuice() {
        System.out.println("初始化Guice容器...");
        
        // 创建Guice注入器，安装所有模块
        injector = Guice.createInjector(
            new AppModule(),        // 主应用模块
            new DatabaseModule()    // 数据库模块
        );
        
        // 获取请求分发器实例
        requestDispatcher = injector.getInstance(RequestDispatcher.class);
        
        System.out.println("Guice容器初始化完成");
    }
    
    /**
     * 扫描并注册所有控制器
     */
    private void scanControllers() {
        System.out.println("开始扫描控制器...");
        
        // 获取控制器扫描器
        ControllerScanner controllerScanner = injector.getInstance(ControllerScanner.class);
        
        // 扫描控制器包（您可以根据实际包名修改）
        String controllerPackage = "cloud.compan.servlet.controller";
        controllerScanner.scanAndRegister(controllerPackage);
        
        System.out.println("控制器扫描完成");
    }
    
    /**
     * 处理所有HTTP请求
     * 将请求分发给RequestDispatcher处理
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 设置响应编码
        response.setCharacterEncoding("UTF-8");
        
        try {
            // 分发请求到相应的控制器方法
            requestDispatcher.dispatch(request, response);
            
        } catch (Exception e) {
            System.err.println("请求处理异常: " + e.getMessage());
            e.printStackTrace();
            
            // 返回500错误
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"error\":\"Internal Server Error\",\"message\":\"" + 
                e.getMessage() + "\",\"status\":500}");
        }
    }
    
    @Override
    public void destroy() {
        System.out.println("MainServlet 正在销毁...");
        
        // 清理资源
        if (injector != null) {
            // Guice 会自动处理单例对象的清理
            System.out.println("Guice容器资源已清理");
        }
        
        super.destroy();
        System.out.println("MainServlet 销毁完成");
    }
}
