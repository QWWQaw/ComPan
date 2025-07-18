package cloud.compan.servlet.controller;

import cloud.compan.servlet.annotations.component.*;
import cloud.compan.servlet.controller.BaseController;
import cloud.compan.servlet.service.UserService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器 - 处理用户注册、登录、登出等认证相关操作，这里用到了UserService
 */
@Controller
@RequestMapping(path = "/api/v1/auth")
public class AuthController extends BaseController {

    private UserService userService;

    public AuthController() {
        this.userService = new UserService();
    }

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

            // 检查用户名是否已存在 - 使用真实数据库查询
            if (userService.isUsernameExists(username)) {
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

            // 检查邮箱是否已存在
            if (userService.isEmailExists(email)) {
                Map<String, Object> errorData = new HashMap<>();
                List<Map<String, String>> errors = new ArrayList<>();
                Map<String, String> error = new HashMap<>();
                error.put("field", "email");
                error.put("message", "邮箱已存在");
                errors.add(error);
                errorData.put("errors", errors);

                sendErrorResponse(response, 409, "注册失败", errorData);
                return;
            }

            // 创建新用户 - 使用真实数据库操作
            String passwordHash = userService.hashPassword(password);
            Map<String, Object> userData = userService.createUser(username, email, passwordHash);

            if (userData != null) {
                sendCreatedResponse(response, userData, "注册成功");
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "注册失败，请稍后重试");
            }

        } catch (Exception e) {
            System.err.println("注册过程中发生错误: " + e.getMessage());
            e.printStackTrace();
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

            // 使用真实数据库验证用户凭据
            Map<String, Object> userData = userService.authenticateUser(username, password);

            if (userData == null) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "用户名或密码错误");
                return;
            }

            // 检查账户状态
            String status = (String) userData.get("status");
            if ("banned".equals(status)) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("status", "banned");
                errorData.put("contact", "support@example.com");
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "账户已被禁用，请联系管理员", errorData);
                return;
            }

            // 生成JWT Token（这里使用简单的token生成，实际项目中应该使用JWT库）
            String token = generateToken(userData);

            Map<String, Object> loginData = new HashMap<>();
            loginData.put("token", token);
            loginData.put("expires_in", 604800); // 7天
            loginData.put("user", userData);

            sendSuccessResponse(response, loginData, "登录成功");

        } catch (Exception e) {
            System.err.println("登录过程中发生错误: " + e.getMessage());
            e.printStackTrace();
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

            // 模拟token失效处理（实际项目中应该将token加入黑名单）
            sendSuccessResponse(response, null, "成功退出账号");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "登出失败: " + e.getMessage());
        }
    }

    /**
     * 生成简单的token（实际项目中应该使用JWT库）
     */
    private String generateToken(Map<String, Object> userData) {
        Long userId = (Long) userData.get("user_id");
        String username = (String) userData.get("username");
        long timestamp = System.currentTimeMillis();

        // 简单的token生成（实际项目中应该使用JWT）
        return "token_" + userId + "_" + username + "_" + timestamp;
    }
}
