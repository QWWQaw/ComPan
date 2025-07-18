package cloud.compan.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import cloud.compan.servlet.web.Dispatcher;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = "/")
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
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        dispatcher.dispatch(req, resp);
    }
}
