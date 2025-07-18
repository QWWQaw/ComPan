package cloud.compan.servlet.handler;

import cloud.compan.servlet.service.UserService;
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

    public UserHandler() {
        this.userService = new UserService();
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod().toUpperCase();

        // 检查用户是否已登录
        if (!isUserLoggedIn(request)) {
            sendErrorResponse(response, 401, "请先登录", null);
            return;
        }

        Long userId = (Long) request.getSession().getAttribute("userId");

        // 根据HTTP方法和路径分发请求
        switch (method) {
            case "GET":
                if (requestURI.endsWith("/profile")) {
                    getUserProfile(request, response, userId);
                } else if (requestURI.endsWith("/storage-stats")) {
                    getUserStorageStats(request, response, userId);
                } else if (requestURI.endsWith("/activity-log")) {
                    getUserActivityLog(request, response, userId);
                } else {
                    sendErrorResponse(response, 404, "未找到对应的用户接口", null);
                }
                break;

            case "PUT":
                if (requestURI.endsWith("/update-profile")) {
                    updateUserProfile(request, response, userId);
                } else if (requestURI.endsWith("/password")) {
                    changePassword(request, response, userId);
                } else {
                    sendErrorResponse(response, 404, "未找到对应的用户接口", null);
                }
                break;

            default:
                sendErrorResponse(response, 405, "不支持的HTTP方法: " + method, null);
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
     * 获取用户信息
     * GET /api/v1/users/profile
     */
    private void getUserProfile(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            Map<String, Object> result = userService.getUserProfile(userId);

            if ((Boolean) result.get("success")) {
                sendSuccessResponse(response, 200, "获取用户信息成功", result.get("data"));
            } else {
                sendErrorResponse(response, 404, (String) result.get("message"), null);
            }

        } catch (Exception e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "获取用户信息失败", null);
        }
    }

    /**
     * 更新用户信息
     * PUT /api/v1/users/update-profile
     */
    private void updateUserProfile(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            String username = request.getParameter("username");
            String email = request.getParameter("email");

            Map<String, Object> result = userService.updateUserProfile(userId, username, email);

            if ((Boolean) result.get("success")) {
                sendSuccessResponse(response, 200, "用户信息更新成功", result.get("data"));
            } else {
                sendErrorResponse(response, 400, (String) result.get("message"), null);
            }

        } catch (Exception e) {
            System.err.println("更新用户信息失败: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "更新用户信息失败", null);
        }
    }

    /**
     * 修改密码
     * PUT /api/v1/users/me/password
     */
    private void changePassword(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            String oldPassword = request.getParameter("old_password");
            String newPassword = request.getParameter("new_password");

            if (oldPassword == null || newPassword == null) {
                sendErrorResponse(response, 400, "旧密码和新密码不能为空", null);
                return;
            }

            Map<String, Object> result = userService.changePassword(userId, oldPassword, newPassword);

            if ((Boolean) result.get("success")) {
                sendSuccessResponse(response, 200, "密码修改成功", null);
            } else {
                String message = (String) result.get("message");
                if (message.contains("当前密码错误")) {
                    sendErrorResponse(response, 400, "当前密码错误", null);
                } else {
                    sendErrorResponse(response, 400, message, null);
                }
            }

        } catch (Exception e) {
            System.err.println("修改密码失败: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "修改密码失败", null);
        }
    }

    /**
     * 获取用户存储统计
     * GET /api/v1/users/storage-stats
     */
    private void getUserStorageStats(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            Map<String, Object> result = userService.getUserStorageStats(userId);

            if ((Boolean) result.get("success")) {
                sendSuccessResponse(response, 200, "获取存储统计成功", result.get("data"));
            } else {
                sendErrorResponse(response, 500, (String) result.get("message"), null);
            }

        } catch (Exception e) {
            System.err.println("获取存储统计失败: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "获取存储统计失败", null);
        }
    }

    /**
     * 获取用户活动日志
     * GET /api/v1/users/activity-log
     */
    private void getUserActivityLog(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            String pageStr = request.getParameter("page");
            String perPageStr = request.getParameter("per_page");
            String operation = request.getParameter("operation");

            int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
            int perPage = perPageStr != null ? Integer.parseInt(perPageStr) : 20;

            Map<String, Object> result = userService.getUserActivityLog(userId, page, perPage, operation);

            if ((Boolean) result.get("success")) {
                sendSuccessResponse(response, 200, "获取活动日志成功", result.get("data"));
            } else {
                sendErrorResponse(response, 500, (String) result.get("message"), null);
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(response, 400, "无效的页码参数", null);
        } catch (Exception e) {
            System.err.println("获取活动日志失败: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, 500, "获取活动日志失败", null);
        }
    }

    /**
     * 检查用户是否已登录
     */
    private boolean isUserLoggedIn(HttpServletRequest request) {
        return request.getSession().getAttribute("userId") != null;
    }
}
