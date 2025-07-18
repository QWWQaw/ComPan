package cloud.compan.servlet.service;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务类 - 处理所有认证相关的业务逻辑
 * 包括用户注册、登录、登出等功能
 */
public class AuthService {

    private final UserService userService;

    public AuthService() {
        this.userService = new UserService();
    }

    /**
     * 用户注册业务逻辑
     */
    public Map<String, Object> register(String username, String email, String password) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 参数验证
            Map<String, Object> validationResult = validateRegisterInput(username, email, password);
            if (!(Boolean) validationResult.get("valid")) {
                result.put("success", false);
                result.put("message", validationResult.get("message"));
                result.put("status_code", 400);
                result.put("data", validationResult.get("errors"));
                return result;
            }

            // 2. 调用用户服务进行注册
            Map<String, Object> registerResult = userService.registerUser(username, email, password);

            if ((Boolean) registerResult.get("success")) {
                // 注册成功
                Map<String, Object> userData = (Map<String, Object>) registerResult.get("data");

                result.put("success", true);
                result.put("message", "注册成功");
                result.put("status_code", 201);
                result.put("data", Map.of(
                    "user_id", userData.get("user_id"),
                    "username", userData.get("username"),
                    "email", userData.get("email"),
                    "created_at", userData.get("created_at")
                ));
            } else {
                // 注册失败
                String message = (String) registerResult.get("message");
                result.put("success", false);
                result.put("message", "注册失败");

                if (message.contains("用户名已存在")) {
                    result.put("status_code", 409);
                    result.put("data", Map.of(
                        "errors", new Object[]{
                            Map.of("field", "username", "message", "用户名已存在")
                        }
                    ));
                } else if (message.contains("邮箱已存在")) {
                    result.put("status_code", 409);
                    result.put("data", Map.of(
                        "errors", new Object[]{
                            Map.of("field", "email", "message", "邮箱已存在")
                        }
                    ));
                } else {
                    result.put("status_code", 400);
                    result.put("message", message);
                }
            }

        } catch (Exception e) {
            System.err.println("注册业务逻辑处理失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 用户登录业务逻辑
     */
    public Map<String, Object> login(String username, String password, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 参数验证
            Map<String, Object> validationResult = validateLoginInput(username, password);
            if (!(Boolean) validationResult.get("valid")) {
                result.put("success", false);
                result.put("message", validationResult.get("message"));
                result.put("status_code", 400);
                return result;
            }

            // 2. 调用用户服务进行登录验证
            Map<String, Object> loginResult = userService.loginUser(username, password);

            if ((Boolean) loginResult.get("success")) {
                // 登录成功
                Map<String, Object> userData = (Map<String, Object>) loginResult.get("data");

                // 创建会话
                session.setAttribute("user", userData);
                session.setAttribute("userId", userData.get("user_id"));
                session.setAttribute("username", userData.get("username"));

                // 设置会话超时时间（7天）
                session.setMaxInactiveInterval(7 * 24 * 60 * 60);

                result.put("success", true);
                result.put("message", "登录成功");
                result.put("status_code", 200);
                result.put("data", Map.of(
                    "token", "session_based_token", // 基于Session的简化实现
                    "expires_in", 604800, // 7天
                    "user", Map.of(
                        "user_id", userData.get("user_id"),
                        "username", userData.get("username"),
                        "email", userData.get("email"),
                        "storage_limit", userData.get("storage_limit"),
                        "storage_used", userData.get("storage_used")
                    )
                ));
            } else {
                // 登录失败
                String message = (String) loginResult.get("message");
                result.put("success", false);

                if (message.contains("用户名或密码错误")) {
                    result.put("message", "用户名或密码错误");
                    result.put("status_code", 401);
                } else if (message.contains("账户已被禁用")) {
                    result.put("message", "账户已被禁用，请联系管理员");
                    result.put("status_code", 403);
                    result.put("data", Map.of(
                        "status", "banned",
                        "contact", "support@kepan.com"
                    ));
                } else {
                    result.put("message", message);
                    result.put("status_code", 400);
                }
            }

        } catch (Exception e) {
            System.err.println("登录业务逻辑处理失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 用户登出业务逻辑
     */
    public Map<String, Object> logout(HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查是否已登录
            if (session.getAttribute("userId") == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                result.put("status_code", 401);
                return result;
            }

            // 销毁会话
            session.invalidate();

            result.put("success", true);
            result.put("message", "成功退出账号");
            result.put("status_code", 200);
            result.put("data", Map.of(
                "logged_out_at", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            System.err.println("登出业务逻辑处理失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误");
            result.put("status_code", 500);
        }

        return result;
    }

    /**
     * 检查用户是否已登录
     */
    public boolean isUserLoggedIn(HttpSession session) {
        return session != null && session.getAttribute("userId") != null;
    }

    /**
     * 获取当前登录用户ID
     */
    public Long getCurrentUserId(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Long) session.getAttribute("userId");
    }

    /**
     * 获取当前登录用户信息
     */
    public Map<String, Object> getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Map<String, Object>) session.getAttribute("user");
    }

    // ================== 私有辅助方法 ==================

    /**
     * 验证注册输入参数
     */
    private Map<String, Object> validateRegisterInput(String username, String email, String password) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);

        // 收集所有验证错误
        Map<String, String> errors = new HashMap<>();

        // 验证用户名
        if (username == null || username.trim().isEmpty()) {
            errors.put("username", "用户名不能为空");
        } else if (username.length() < 3) {
            errors.put("username", "用户名长度不能少于3个字符");
        } else if (username.length() > 20) {
            errors.put("username", "用户名长度不能超过20个字符");
        } else if (!username.matches("^[a-zA-Z0-9_]+$")) {
            errors.put("username", "用户名只能包含字母、数字和下划线");
        }

        // 验证邮箱
        if (email == null || email.trim().isEmpty()) {
            errors.put("email", "邮箱不能为空");
        } else if (!isValidEmail(email)) {
            errors.put("email", "邮箱格式不正确");
        }

        // 验证密码
        if (password == null || password.trim().isEmpty()) {
            errors.put("password", "密码不能为空");
        } else if (password.length() < 6) {
            errors.put("password", "密码长度不能少于6个字符");
        } else if (password.length() > 50) {
            errors.put("password", "密码长度不能超过50个字符");
        }

        if (!errors.isEmpty()) {
            result.put("valid", false);
            result.put("message", "输入参数验证失败");
            result.put("errors", errors.entrySet().stream()
                .map(entry -> Map.of("field", entry.getKey(), "message", entry.getValue()))
                .toArray());
        }

        return result;
    }

    /**
     * 验证登录输入参数
     */
    private Map<String, Object> validateLoginInput(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);

        if (username == null || username.trim().isEmpty()) {
            result.put("valid", false);
            result.put("message", "用户名不能为空");
        } else if (password == null || password.trim().isEmpty()) {
            result.put("valid", false);
            result.put("message", "密码不能为空");
        }

        return result;
    }

    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }
}
