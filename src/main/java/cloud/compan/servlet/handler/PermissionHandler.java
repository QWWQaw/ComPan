package cloud.compan.servlet.handler;

import cloud.compan.servlet.service.PermissionService;
import cloud.compan.servlet.annotations.component.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 权限管理处理器
 *
 * 根据RESTFUL API文档实现：
 * POST /api/v1/permissions - 设置文件/文件夹权限
 * GET /api/v1/permissions - 获取权限列表
 * PUT /api/v1/permissions/{permission_id} - 更新权限
 * DELETE /api/v1/permissions/{permission_id} - 删除权限
 */
@Service
public class PermissionHandler extends BaseHandler {

    private final PermissionService permissionService;

    public PermissionHandler() {
        this.permissionService = new PermissionService();
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String method = request.getMethod().toUpperCase();
        String requestURI = request.getRequestURI();

        switch (method) {
            case "POST":
                handleSetPermission(request, response);
                break;
            case "GET":
                handleGetPermissions(request, response);
                break;
            case "PUT":
                handleUpdatePermission(request, response);
                break;
            case "DELETE":
                handleDeletePermission(request, response);
                break;
            default:
                sendError(response, "不支持的HTTP方法: " + method, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    public String[] getSupportedMethods() {
        return new String[]{"GET", "POST", "PUT", "DELETE"};
    }

    @Override
    public String getPathPattern() {
        return "/api/v1/permissions/*";
    }

    /**
     * 处理设置权限请求
     */
    private void handleSetPermission(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // 1. 提取请求参数
            String resourceType = getParameterFromRequestBody(request, "resource_type");
            String resourceIdStr = getParameterFromRequestBody(request, "resource_id");
            String userIdStr = getParameterFromRequestBody(request, "user_id");
            String permission = getParameterFromRequestBody(request, "permission");

            Long resourceId = Long.parseLong(resourceIdStr);
            Long userId = Long.parseLong(userIdStr);

            // 2. 调用service层处理设置权限逻辑
            Map<String, Object> result = permissionService.setPermission(resourceType, resourceId, userId, permission);

            // 3. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (NumberFormatException e) {
            sendError(response, "无效的ID格式", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("设置权限失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理获取权限列表请求
     */
    private void handleGetPermissions(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // 1. 提取查询参数
            String resourceType = request.getParameter("resource_type");
            String resourceIdStr = request.getParameter("resource_id");

            Map<String, Object> result;

            if (resourceIdStr != null) {
                // 获取特定资源的权限
                Long resourceId = Long.parseLong(resourceIdStr);
                result = permissionService.getResourcePermissions(resourceType, resourceId);
            } else {
                // 获取用户的所有权限
                Long userId = getCurrentUserId(request);
                result = permissionService.getUserPermissions(userId);
            }

            // 2. 返回响应
            sendJsonResponse(response, result, HttpServletResponse.SC_OK);

        } catch (NumberFormatException e) {
            sendError(response, "无效的ID格式", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("获取权限失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理更新权限请求
     */
    private void handleUpdatePermission(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // 1. 从URL路径中提取权限ID
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                sendError(response, "缺少权限ID", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String permissionIdStr = pathInfo.substring(1); // 去掉开头的'/'
            Long permissionId = Long.parseLong(permissionIdStr);

            // 2. 提取更新参数
            String permission = getParameterFromRequestBody(request, "permission");

            // 3. 调用service层处理更新权限逻辑
            Map<String, Object> result = permissionService.updatePermission(permissionId, permission);

            // 4. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (NumberFormatException e) {
            sendError(response, "无效的权限ID格式", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("更新权限失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理删除权限请求
     */
    private void handleDeletePermission(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // 1. 从URL路径中提取权限ID
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                sendError(response, "缺少权限ID", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String permissionIdStr = pathInfo.substring(1); // 去掉开头的'/'
            Long permissionId = Long.parseLong(permissionIdStr);

            // 2. 调用service层处理删除权限逻辑
            Map<String, Object> result = permissionService.deletePermission(permissionId);

            // 3. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (NumberFormatException e) {
            sendError(response, "无效的权限ID格式", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("删除权限失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        // 从session中获取用户ID
        Object userIdObj = request.getSession().getAttribute("user_id");
        if (userIdObj != null) {
            return Long.parseLong(userIdObj.toString());
        }
        throw new RuntimeException("用户未登录");
    }
}
