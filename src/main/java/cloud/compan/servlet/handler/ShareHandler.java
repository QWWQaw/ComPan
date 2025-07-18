package cloud.compan.servlet.handler;

import cloud.compan.servlet.service.ShareService;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.annotations.component.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 分享管理处理器
 *
 * 根据RESTFUL API文档实现：
 * POST /api/v1/shares - 创建分享链接
 * GET /api/v1/shares - 获取我的分享列表
 * GET /api/v1/shares/{share_link} - 获取分享详情
 * POST /api/v1/shares/{share_link}/access - 访问分享内容
 * DELETE /api/v1/shares/{share_id} - 删除分享
 */
@Service
public class ShareHandler extends BaseHandler {

    private final ShareService shareService;
    private final AuthService authService;

    public ShareHandler() {
        this.shareService = new ShareService();
        this.authService = new AuthService();
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod().toUpperCase();

        // 根据HTTP方法和路径分发请求
        switch (method) {
            case "POST":
                if (requestURI.endsWith("/shares")) {
                    handleCreateShare(request, response);
                } else if (requestURI.matches(".*/shares/[^/]+/access$")) {
                    handleAccessShare(request, response);
                } else {
                    sendNotFound(response, "未找到对应的分享接口");
                }
                break;

            case "GET":
                if (requestURI.endsWith("/shares")) {
                    handleGetMyShares(request, response);
                } else if (requestURI.matches(".*/shares/[^/]+$")) {
                    handleGetShareDetails(request, response);
                } else {
                    sendNotFound(response, "未找到对应的分享接口");
                }
                break;

            case "DELETE":
                if (requestURI.matches(".*/shares/\\d+$")) {
                    handleDeleteShare(request, response);
                } else {
                    sendNotFound(response, "未找到对应的分享接口");
                }
                break;

            default:
                sendError(response, "不支持的HTTP方法: " + method, HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    public String[] getSupportedMethods() {
        return new String[]{"GET", "POST", "DELETE"};
    }

    @Override
    public String getPathPattern() {
        return "/api/v1/shares/*";
    }

    /**
     * 处理创建分享请求
     */
    private void handleCreateShare(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // 检查用户是否已登录
            if (!authService.isUserLoggedIn(request.getSession())) {
                sendError(response, "请先登录", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Long userId = authService.getCurrentUserId(request.getSession());

            // 1. 提取请求参数
            String resourceType = getParameterFromRequestBody(request, "resource_type");
            String resourceIdStr = getParameterFromRequestBody(request, "resource_id");
            String expiryDays = getParameterFromRequestBody(request, "expiry_days");
            String password = getParameterFromRequestBody(request, "password");
            String description = getParameterFromRequestBody(request, "description");

            Long resourceId = Long.parseLong(resourceIdStr);
            Integer expiry = expiryDays != null ? Integer.parseInt(expiryDays) : null;

            // 2. 调用service层处理创建分享
            Map<String, Object> result = shareService.createShare(userId, resourceType, resourceId, expiry, password, description);

            // 3. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_CREATED);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (NumberFormatException e) {
            sendError(response, "无效的参数格式", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("创建分享失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理获取我的分享列表请求
     */
    private void handleGetMyShares(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // 检查用户是否已登录
            if (!authService.isUserLoggedIn(request.getSession())) {
                sendError(response, "请先登录", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Long userId = authService.getCurrentUserId(request.getSession());

            // 1. 提取查询参数
            String page = request.getParameter("page");
            String size = request.getParameter("size");

            int pageNum = page != null ? Integer.parseInt(page) : 1;
            int pageSize = size != null ? Integer.parseInt(size) : 20;

            // 2. 调用service层获取分享列表
            Map<String, Object> result = shareService.getUserShares(userId, pageNum, pageSize);

            // 3. 返回响应
            sendJsonResponse(response, result, HttpServletResponse.SC_OK);

        } catch (NumberFormatException e) {
            sendError(response, "无效的参数格式", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("获取分享列表失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理获取分享详情请求
     */
    private void handleGetShareDetails(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // 1. 从URL中提取分享链接
            String shareLink = extractShareLinkFromPath(request.getRequestURI());

            // 2. 调用service层获取分享详情
            Map<String, Object> result = shareService.getShareDetails(shareLink);

            // 3. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 404);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (Exception e) {
            System.err.println("获取分享详情失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理访问分享内容请求
     */
    private void handleAccessShare(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // 1. 从URL中提取分享链接
            String shareLink = extractShareLinkFromPath(request.getRequestURI());

            // 2. 提取访问密码（如果有）
            String password = getParameterFromRequestBody(request, "password");

            // 3. 调用service层处理访问分享
            Map<String, Object> result = shareService.accessShare(shareLink, password);

            // 4. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (Exception e) {
            System.err.println("访问分享失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理删除分享请求
     */
    private void handleDeleteShare(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // 检查用户是否已登录
            if (!authService.isUserLoggedIn(request.getSession())) {
                sendError(response, "请先登录", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Long userId = authService.getCurrentUserId(request.getSession());

            // 1. 从URL中提取分享ID
            Long shareId = extractShareIdFromPath(request.getRequestURI());

            // 2. 调用service层处理删除分享
            Map<String, Object> result = shareService.deleteShare(userId, shareId);

            // 3. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (NumberFormatException e) {
            sendError(response, "无效的分享ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("删除分享失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 从URL路径中提取分享链接
     */
    private String extractShareLinkFromPath(String requestURI) {
        // 匹配 /api/v1/shares/{share_link} 或 /api/v1/shares/{share_link}/xxx
        String[] parts = requestURI.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("shares".equals(parts[i]) && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        throw new IllegalArgumentException("无法从URL中提取分享链接");
    }

    /**
     * 从URL路径中提取分享ID
     */
    private Long extractShareIdFromPath(String requestURI) {
        // 匹配 /api/v1/shares/{share_id}
        String[] parts = requestURI.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("shares".equals(parts[i]) && i + 1 < parts.length) {
                return Long.parseLong(parts[i + 1]);
            }
        }
        throw new IllegalArgumentException("无法从URL中提取分享ID");
    }
}
