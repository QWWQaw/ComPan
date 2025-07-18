package cloud.compan.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import cloud.compan.servlet.web.Dispatcher;
import javax.servlet.annotation.WebServlet;

import java.io.IOException;

@WebServlet(urlPatterns = "/api/*", loadOnStartup = 1)
public class MainServlet extends HttpServlet{
    private Dispatcher dispatcher;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.dispatcher = new Dispatcher();
        // 在这里可以进行初始化操作
        System.out.println("MainServlet initialized");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 使用dispatcher来处理请求
        dispatcher.dispatch(req, resp);
    }
}
