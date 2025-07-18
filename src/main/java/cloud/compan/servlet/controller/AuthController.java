package cloud.compan.servlet.controller;

import cloud.compan.servlet.annotations.component.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * 认证控制器 - 处理用户注册、登录、登出等认证相关操作
 */
@Controller
@RequestMapping(path = "/auth")
public class AuthController extends BaseController {

    /**
     * 用户注册
     * POST /api/v1/auth/register
     */
    @PostMapping(path = "/register")
    public void register(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String username = getParameter(request, "username");
            String email = getParameter(request, "email");
            String password = getParameter(request, "password");

            // 参数验证
            if (isParameterMissing(username, email, password)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "用户名、邮箱和密码不能为空");
                return;
            }

            // 模拟检查用户名是否已存在
            if ("admin".equals(username)) {
                Map<String, Object> errorData = new HashMap<>();
                List<Map<String, String>> errors = new ArrayList<>();
                Map<String, String> error = new HashMap<>();
                error.put("field", "username");
                error.put("message", "用户名已存在");
                errors.add(error);
                errorData.put("errors", errors);

                sendErrorResponse(response, 409, "注册失败", errorData);
                return;
            }

            // 模拟用户注册成功
            Map<String, Object> userData = new HashMap<>();
            userData.put("user_id", System.currentTimeMillis());
            userData.put("username", username);
            userData.put("email", email);
            userData.put("storage_limit", 10737418240L); // 10GB
            userData.put("storage_used", 0);
            userData.put("status", "active");
            userData.put("created_at", java.time.ZonedDateTime.now().format(java.time.format.DateTimeFormatter.ISO_INSTANT));

            sendCreatedResponse(response, userData, "注册成功");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录
     * POST /api/v1/auth/login
     */
    @PostMapping(path = "/login")
    public void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String username = getParameter(request, "username");
            String password = getParameter(request, "password");

            if (isParameterMissing(username, password)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "用户名和密码不能为空");
                return;
            }

            // 模拟验证用户凭据
            if (!"admin".equals(username) || !"password".equals(password)) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "用户名或密码错误");
                return;
            }

            // 模拟检查账户状态
            if ("banned_user".equals(username)) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("status", "banned");
                errorData.put("contact", "support@example.com");
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "账户已被禁用，请联系管理员", errorData);
                return;
            }

            // 模拟生成JWT Token
            String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoxLCJ1c2VybmFtZSI6ImFkbWluIn0.token_signature";

            Map<String, Object> user = new HashMap<>();
            user.put("user_id", 1);
            user.put("username", username);
            user.put("email", "admin@example.com");
            user.put("storage_limit", 10737418240L);
            user.put("storage_used", 1024000);
            user.put("status", "active");
            user.put("created_at", "2025-07-10T10:30:00Z");

            Map<String, Object> loginData = new HashMap<>();
            loginData.put("token", token);
            loginData.put("expires_in", 604800); // 7天
            loginData.put("user", user);

            sendSuccessResponse(response, loginData, "登录成功");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     * POST /api/v1/auth/logout
     */
    @PostMapping(path = "/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 这里应该验证Authorization header中的token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供有效的认证token");
                return;
            }

            // 模拟token失效处理
            sendSuccessResponse(response, null, "成功退出账号");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "登出失败: " + e.getMessage());
        }
    }
}
