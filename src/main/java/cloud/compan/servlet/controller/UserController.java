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
 * 用户管理控制器 - 处理用户信息管理相关操作
 */
@Controller
@RequestMapping(path = "/api/v1/users")
public class UserController extends BaseController {

    /**
     * 获取用户信息
     * GET /api/v1/users/profile
     */
    @GetMapping(path = "/profile")
    public void getProfile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 验证token (简化处理)
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供有效的认证token");
                return;
            }

            // 模拟用户信息，这个
            Map<String, Object> userData = new HashMap<>();
            userData.put("user_id", 123);
            userData.put("username", "john_doe");
            userData.put("email", "john@example.com");
            userData.put("storage_limit", 10737418240L);
            userData.put("storage_used", 1073741824L);
            userData.put("status", "active");
            userData.put("created_at", "2025-07-10T10:30:00Z");
            userData.put("updated_at", "2025-07-17T09:00:00Z");
            userData.put("last_login", "2025-07-17T10:30:00Z");

            // 用户组信息
            List<Map<String, Object>> groups = new ArrayList<>();
            Map<String, Object> group = new HashMap<>();
            group.put("group_id", 1);
            group.put("group_name", "开发团队");
            group.put("role", "member");
            groups.add(group);
            userData.put("groups", groups);

            sendSuccessResponse(response, userData, "获取用户信息成功");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     * PUT /api/v1/users/update-profile
     */
    @PutMapping(path = "/update-profile")
    public void updateProfile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 验证token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供有效的认证token");
                return;
            }

            String username = getParameter(request, "username");
            String email = getParameter(request, "email");

            // 模拟更新用户信息
            Map<String, Object> updatedUser = new HashMap<>();
            updatedUser.put("user_id", 123);
            updatedUser.put("username", username != null ? username : "john_doe");
            updatedUser.put("email", email != null ? email : "john@example.com");
            updatedUser.put("storage_limit", 10737418240L);
            updatedUser.put("storage_used", 1073741824L);
            updatedUser.put("status", "active");
            updatedUser.put("updated_at", java.time.ZonedDateTime.now().format(java.time.format.DateTimeFormatter.ISO_INSTANT));

            sendSuccessResponse(response, updatedUser, "用户信息更新成功");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     * PUT /api/v1/users/me/password
     */
    @PutMapping(path = "/me/password")
    public void changePassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 验证token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供有效的认证token");
                return;
            }

            String oldPassword = getParameter(request, "old_password");
            String newPassword = getParameter(request, "new_password");

            if (isParameterMissing(oldPassword, newPassword)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "当前密码和新密码不能为空");
                return;
            }

            // 模拟验证当前密码
            if (!"current_password".equals(oldPassword)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "当前密码错误");
                return;
            }

            // 模拟密码修改成功
            sendSuccessResponse(response, null, "密码修改成功");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户存储统计
     * GET /api/v1/users/storage-stats
     */
    @GetMapping(path = "/storage-stats")
    public void getStorageStats(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 验证token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供有效的认证token");
                return;
            }

            // 模拟存储统计数据
            Map<String, Object> storageStats = new HashMap<>();
            storageStats.put("storage_limit", 10737418240L);
            storageStats.put("storage_used", 1073741824L);
            storageStats.put("storage_available", 9663676416L);
            storageStats.put("storage_percentage", 10.0);
            storageStats.put("file_count", 156);
            storageStats.put("folder_count", 23);

            // 文件类型分布
            Map<String, Object> breakdown = new HashMap<>();

            Map<String, Object> documents = new HashMap<>();
            documents.put("count", 45);
            documents.put("size", 104857600L);
            breakdown.put("documents", documents);

            Map<String, Object> images = new HashMap<>();
            images.put("count", 67);
            images.put("size", 536870912L);
            breakdown.put("images", images);

            Map<String, Object> videos = new HashMap<>();
            videos.put("count", 15);
            videos.put("size", 402653184L);
            breakdown.put("videos", videos);

            Map<String, Object> others = new HashMap<>();
            others.put("count", 29);
            others.put("size", 29360128L);
            breakdown.put("others", others);

            storageStats.put("breakdown", breakdown);

            sendSuccessResponse(response, storageStats, "获取存储统计成功");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取存储统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户活动日志
     * GET /api/v1/users/activity-log
     */
    @GetMapping(path = "/activity-log")
    public void getActivityLog(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 验证token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供有效的认证token");
                return;
            }

            int page = getIntParameter(request, "page", 1);
            int perPage = getIntParameter(request, "per_page", 20);
            String operation = getParameter(request, "operation");

            // 模拟活动日志数据
            List<Map<String, Object>> activities = new ArrayList<>();

            Map<String, Object> activity1 = new HashMap<>();
            activity1.put("id", 1001);
            activity1.put("operation", "file_upload");
            Map<String, Object> details1 = new HashMap<>();
            details1.put("file_name", "document.pdf");
            details1.put("file_size", 1024000);
            details1.put("folder_path", "/工作文档");
            activity1.put("details", details1);
            activity1.put("ip_address", "192.168.1.100");
            activity1.put("performed_at", "2025-07-17T10:30:00Z");
            activities.add(activity1);

            Map<String, Object> activity2 = new HashMap<>();
            activity2.put("id", 1000);
            activity2.put("operation", "folder_create");
            Map<String, Object> details2 = new HashMap<>();
            details2.put("folder_name", "新建文件夹");
            details2.put("parent_path", "/我的文档");
            activity2.put("details", details2);
            activity2.put("ip_address", "192.168.1.100");
            activity2.put("performed_at", "2025-07-17T10:25:00Z");
            activities.add(activity2);

            // 创建分页响应
            PaginationInfo pagination = new PaginationInfo(page, perPage, 156);
            PaginationResponse<Map<String, Object>> paginatedResponse = new PaginationResponse<>(activities, pagination);

            sendSuccessResponse(response, paginatedResponse, "获取活动日志成功");

        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取活动日志失败: " + e.getMessage());
        }
    }
}
