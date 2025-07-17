package cloud.compan.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
public class MainServlet extends HttpServlet{
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // 在这里可以进行初始化操作
        System.out.println("MainServlet initialized");
    }

}
