package cloud.compan.servlet.handler;

import cloud.compan.servlet.service.UserService;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.annotations.component.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 用户信息管理处理器
 *
 * 根据RESTFUL API文档实现：
 * GET /api/v1/users/profile - 获取用户信息
 * PUT /api/v1/users/update-profile - 更新用户信息
 * PUT /api/v1/users/me/password - 修改密码
 * GET /api/v1/users/storage-stats - 获取用户存储统计
 * GET /api/v1/users/activity-log - 获取用户活动日志
 */
@Service
public class UserHandler extends BaseHandler {

    private final UserService userService;
    private final AuthService authService;

    public UserHandler() {
        this.userService = new UserService();
        this.authService = new AuthService();
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod().toUpperCase();

        // 检查用户是否已登录
        if (!authService.isUserLoggedIn(request.getSession())) {
            sendError(response, "请先登录", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Long userId = authService.getCurrentUserId(request.getSession());

        // 根据HTTP方法和路径分发请求
        switch (method) {
            case "GET":
                if (requestURI.endsWith("/profile")) {
                    handleGetUserProfile(request, response, userId);
                } else if (requestURI.endsWith("/storage-stats")) {
                    handleGetStorageStats(request, response, userId);
                } else if (requestURI.endsWith("/activity-log")) {
                    handleGetActivityLog(request, response, userId);
                } else {
                    sendNotFound(response, "未找到对应的用户接口");
                }
                break;

            case "PUT":
                if (requestURI.endsWith("/update-profile")) {
                    handleUpdateProfile(request, response, userId);
                } else if (requestURI.endsWith("/me/password")) {
                    handleChangePassword(request, response, userId);
                } else {
                    sendNotFound(response, "未找到对应的用户接口");
                }
                break;

            default:
                sendError(response, "不支持的HTTP方法: " + method, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    public String[] getSupportedMethods() {
        return new String[]{"GET", "PUT"};
    }

    @Override
    public String getPathPattern() {
        return "/api/v1/users/*";
    }

    /**
     * 处理获取用户资料请求
     */
    private void handleGetUserProfile(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 调用service层获取用户资料
            Map<String, Object> result = userService.getUserProfile(userId);

            // 2. 返回响应
            sendJsonResponse(response, result, HttpServletResponse.SC_OK);

        } catch (Exception e) {
            System.err.println("获取用户资料失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理更新用户资料请求
     */
    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 提取更新参数
            String username = getParameterFromRequestBody(request, "username");
            String email = getParameterFromRequestBody(request, "email");
            String phone = getParameterFromRequestBody(request, "phone");
            String avatar = getParameterFromRequestBody(request, "avatar");

            // 2. 调用service层处理更新
            Map<String, Object> result = userService.updateUserProfile(userId, username, email, phone, avatar);

            // 3. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (Exception e) {
            System.err.println("更新用户资料失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理修改密码请求
     */
    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 提取密码参数
            String oldPassword = getParameterFromRequestBody(request, "old_password");
            String newPassword = getParameterFromRequestBody(request, "new_password");
            String confirmPassword = getParameterFromRequestBody(request, "confirm_password");

            // 2. 调用service层处理密码修改
            Map<String, Object> result = userService.changePassword(userId, oldPassword, newPassword, confirmPassword);

            // 3. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (Exception e) {
            System.err.println("修改密码失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理获取存储统计请求
     */
    private void handleGetStorageStats(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 调用service层获取存储统计
            Map<String, Object> result = userService.getUserStorageStats(userId);

            // 2. 返回响应
            sendJsonResponse(response, result, HttpServletResponse.SC_OK);

        } catch (Exception e) {
            System.err.println("获取存储统计失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理获取活动日志请求
     */
    private void handleGetActivityLog(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 提取查询参数
            String page = request.getParameter("page");
            String size = request.getParameter("size");
            String actionType = request.getParameter("action_type");

            int pageNum = page != null ? Integer.parseInt(page) : 1;
            int pageSize = size != null ? Integer.parseInt(size) : 20;

            // 2. 调用service层获取活动日志
            Map<String, Object> result = userService.getUserActivityLog(userId, pageNum, pageSize, actionType);

            // 3. 返回响应
            sendJsonResponse(response, result, HttpServletResponse.SC_OK);

        } catch (NumberFormatException e) {
            sendError(response, "无效的参数格式", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("获取活动日志失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }
}
