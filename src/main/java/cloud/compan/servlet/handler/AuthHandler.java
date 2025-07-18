package cloud.compan.servlet.handler;

import cloud.compan.servlet.service.UserService;
import cloud.compan.servlet.annotations.component.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

/**
 * 用户认证处理器 - 完整版本
 *
 * 根据RESTFUL API文档实现：
 * POST /api/v1/auth/register - 用户注册
 * POST /api/v1/auth/login - 用户登录
 * POST /api/v1/auth/logout - 用户登出
 */
@Service
public class AuthHandler extends BaseHandler {

    private final UserService userService;

    public AuthHandler() {
        this.userService = new UserService();
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod().toUpperCase();

        // 根据路径和方法分发到具体的处理方法
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
     * POST /api/v1/auth/register
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // 读取JSON数据或表单数据
            String username = getParameterFromRequestBody(request, "username");
            String email = getParameterFromRequestBody(request, "email");
            String password = getParameterFromRequestBody(request, "password");

            // 验证输入数据
            if (username == null || username.trim().isEmpty()) {
                sendErrorResponse(response, 400, "用户名不能为空", null);
                return;
            }

            if (email == null || email.trim().isEmpty()) {
                sendErrorResponse(response, 400, "邮箱不能为空", null);
                return;
            }

            if (password == null || password.trim().isEmpty()) {
                sendErrorResponse(response, 400, "密码不能为空", null);
                return;
            }

            // 调用服务层处理注册逻辑
            Map<String, Object> result = userService.registerUser(username, email, password);

            if ((Boolean) result.get("success")) {
                sendSuccessResponse(response, 201, "注册成功", result.get("data"));
            } else {
                String message = (String) result.get("message");
                if (message.contains("用户名已存在")) {
                    sendErrorResponse(response, 409, "注册失败", Map.of(
                        "errors", new Object[]{
                            Map.of("field", "username", "message", "用户名已存在")
                        }
                    ));
                } else if (message.contains("邮箱已存在")) {
                    sendErrorResponse(response, 409, "注册失败", Map.of(
                        "errors", new Object[]{
                            Map.of("field", "email", "message", "邮箱已存在")
                        }
                    ));
                } else {
                    sendErrorResponse(response, 400, message, null);
                }
            }

        } catch (Exception e) {
            System.err.println("注册失败: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "服务器内部错误", null);
        }
    }

    /**
     * 处理用户登录
     * POST /api/v1/auth/login
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String username = getParameterFromRequestBody(request, "username");
            String password = getParameterFromRequestBody(request, "password");

            // 验证输入数据
            if (username == null || username.trim().isEmpty()) {
                sendErrorResponse(response, 400, "用户名不能为空", null);
                return;
            }

            if (password == null || password.trim().isEmpty()) {
                sendErrorResponse(response, 400, "密码不能为空", null);
                return;
            }

            // 调用服务层处理登录逻辑
            Map<String, Object> result = userService.loginUser(username, password);

            if ((Boolean) result.get("success")) {
                Map<String, Object> userData = (Map<String, Object>) result.get("data");

                // 创建会话
                request.getSession().setAttribute("user", userData);
                request.getSession().setAttribute("userId", userData.get("user_id"));

                // 构造登录成功响应
                Map<String, Object> loginData = new HashMap<>();
                loginData.put("token", "session_based_token"); // 基于Session的简化实现
                loginData.put("expires_in", 604800); // 7天
                loginData.put("user", userData);

                sendSuccessResponse(response, 200, "登录成功", loginData);
            } else {
                String message = (String) result.get("message");
                if (message.contains("用户名或密码错误")) {
                    sendErrorResponse(response, 401, "用户名或密码错误", null);
                } else if (message.contains("账户已被禁用")) {
                    sendErrorResponse(response, 403, "账户已被禁用，请联系管理员", Map.of(
                        "status", "banned",
                        "contact", "support@kepan.com"
                    ));
                } else {
                    sendErrorResponse(response, 400, message, null);
                }
            }

        } catch (Exception e) {
            System.err.println("登录失败: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "服务器内部错误", null);
        }
    }

    /**
     * 处理用户登出
     * POST /api/v1/auth/logout
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // 销毁会话
            request.getSession().invalidate();

            sendSuccessResponse(response, 200, "成功退出账号", null);

        } catch (Exception e) {
            System.err.println("登出失败: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "服务器内部错误", null);
        }
    }

    /**
     * 从请求体中获取参数（支持JSON和表单数据）
     */
    private String getParameterFromRequestBody(HttpServletRequest request, String paramName) {
        // 先尝试从表单参数获取
        String value = request.getParameter(paramName);
        if (value != null) {
            return value;
        }

        // TODO: 后续可以添加JSON解析支持
        return null;
    }
}
