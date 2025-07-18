package cloud.compan.servlet.handler;

import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.annotations.component.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 用户认证处理器 - 重构版本
 * 只负责HTTP请求解析和响应，具体业务逻辑在AuthService中实现
 */
@Service
public class AuthHandler extends BaseHandler {

    private final AuthService authService;

    public AuthHandler() {
        this.authService = new AuthService();
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod().toUpperCase();

        if ("POST".equals(method)) {
            if (requestURI.endsWith("/register")) {
                handleRegister(request, response);
            } else if (requestURI.endsWith("/login")) {
                handleLogin(request, response);
            } else if (requestURI.endsWith("/logout")) {
                handleLogout(request, response);
            } else {
                sendNotFound(response, "未找到对应的认证接口");
            }
        } else {
            sendError(response, "不支持的HTTP方法: " + method, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    public String[] getSupportedMethods() {
        return new String[]{"POST"};
    }

    @Override
    public String getPathPattern() {
        return "/api/v1/auth/*";
    }

    /**
     * 处理用户注册
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // 1. 提取请求参数
            String username = getParameterFromRequestBody(request, "username");
            String email = getParameterFromRequestBody(request, "email");
            String password = getParameterFromRequestBody(request, "password");

            // 2. 调用service层处理注册逻辑
            Map<String, Object> result = authService.register(username, email, password);

            // 3. 根据结果返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_CREATED);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (Exception e) {
            System.err.println("注册请求处理失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理用户登录
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // 1. 提取请求参数
            String username = getParameterFromRequestBody(request, "username");
            String password = getParameterFromRequestBody(request, "password");

            // 2. 调用service层处理登录逻辑
            Map<String, Object> result = authService.login(username, password, request.getSession());

            // 3. 根据结果返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 401);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (Exception e) {
            System.err.println("登录请求处理失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理用户登出
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // 1. 调用service层处理登出逻辑
            Map<String, Object> result = authService.logout(request.getSession());

            // 2. 返回响应
            sendJsonResponse(response, result, HttpServletResponse.SC_OK);

        } catch (Exception e) {
            System.err.println("登出请求处理失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 从请求体中获取参数（支持JSON和表单数据）
     */
    private String getParameterFromRequestBody(HttpServletRequest request, String paramName) {
        String value = request.getParameter(paramName);
        if (value != null) {
            return value;
        }
        // TODO: 后续可以添加JSON解析支持
        return null;
    }
}
