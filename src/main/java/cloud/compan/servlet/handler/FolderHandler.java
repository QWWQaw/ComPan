package cloud.compan.servlet.handler;

import cloud.compan.servlet.service.FolderService;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.annotations.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 文件夹管理处理器
 *
 * 根据RESTFUL API文档实现：
 * POST /api/v1/folders - 创建文件夹
 * GET /api/v1/folders - 获取文件夹列表
 * GET /api/v1/folders/{id} - 获取文件夹详情
 * PUT /api/v1/folders/{id} - 重命名文件夹
 * PUT /api/v1/folders/{id}/move - 移动文件夹
 * DELETE /api/v1/folders/{id} - 删除文件夹
 */
@Service
public class FolderHandler extends BaseHandler {

    private final FolderService folderService;
    private final AuthService authService;

    public FolderHandler() {
        this.folderService = new FolderService();
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
            case "POST":
                if (requestURI.endsWith("/folders")) {
                    handleCreateFolder(request, response, userId);
                } else {
                    sendNotFound(response, "未找到对应的文件夹接口");
                }
                break;

            case "GET":
                if (requestURI.matches(".*/folders/\\d+$")) {
                    handleGetFolderDetails(request, response, userId);
                } else if (requestURI.endsWith("/folders")) {
                    handleGetFolderList(request, response, userId);
                } else {
                    sendNotFound(response, "未找到对应的文件夹接口");
                }
                break;

            case "PUT":
                if (requestURI.matches(".*/folders/\\d+/move$")) {
                    handleMoveFolder(request, response, userId);
                } else if (requestURI.matches(".*/folders/\\d+$")) {
                    handleRenameFolder(request, response, userId);
                } else {
                    sendNotFound(response, "未找到对应的文件夹接口");
                }
                break;

            case "DELETE":
                if (requestURI.matches(".*/folders/\\d+$")) {
                    handleDeleteFolder(request, response, userId);
                } else {
                    sendNotFound(response, "未找到对应的文件夹接口");
                }
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
        return "/api/v1/folders/*";
    }

    /**
     * 处理创建文件夹请求
     */
    private void handleCreateFolder(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 提取请求参数
            String folderName = getParameterFromRequestBody(request, "folder_name");
            String parentFolderIdStr = getParameterFromRequestBody(request, "parent_folder_id");
            String description = getParameterFromRequestBody(request, "description");

            Long parentFolderId = parentFolderIdStr != null ? Long.parseLong(parentFolderIdStr) : null;

            // 2. 调用service层处理创建文件夹
            Map<String, Object> result = folderService.createFolder(userId, folderName, parentFolderId, description);

            // 3. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_CREATED);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (NumberFormatException e) {
            sendError(response, "无效的父文件夹ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("创建文件夹失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理获取文件夹列表请求
     */
    private void handleGetFolderList(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 提取查询参数
            String parentFolderIdStr = request.getParameter("parent_folder_id");
            String page = request.getParameter("page");
            String size = request.getParameter("size");

            Long parentFolderId = parentFolderIdStr != null ? Long.parseLong(parentFolderIdStr) : null;
            int pageNum = page != null ? Integer.parseInt(page) : 1;
            int pageSize = size != null ? Integer.parseInt(size) : 20;

            // 2. 调用service层获取文件夹列表
            Map<String, Object> result = folderService.getFolderList(userId, parentFolderId, pageNum, pageSize);

            // 3. 返回响应
            sendJsonResponse(response, result, HttpServletResponse.SC_OK);

        } catch (NumberFormatException e) {
            sendError(response, "无效的参数格式", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("获取文件夹列表失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理获取文件夹详情请求
     */
    private void handleGetFolderDetails(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 从URL中提取文件夹ID
            Long folderId = extractFolderIdFromPath(request.getRequestURI());

            // 2. 调用service层获取文件夹详情
            Map<String, Object> result = folderService.getFolderDetails(userId, folderId);

            // 3. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 404);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (NumberFormatException e) {
            sendError(response, "无效的文件夹ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("获取文件夹详情失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理重命名文件夹请求
     */
    private void handleRenameFolder(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 从URL中提取文件夹ID
            Long folderId = extractFolderIdFromPath(request.getRequestURI());

            // 2. 提取新的文件夹名称
            String newFolderName = getParameterFromRequestBody(request, "folder_name");
            String description = getParameterFromRequestBody(request, "description");

            // 3. 调用service层处理重命名
            Map<String, Object> result = folderService.renameFolder(userId, folderId, newFolderName, description);

            // 4. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (NumberFormatException e) {
            sendError(response, "无效的文件夹ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("重命名文件夹失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理移动文件夹请求
     */
    private void handleMoveFolder(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 从URL中提取文件夹ID
            Long folderId = extractFolderIdFromPath(request.getRequestURI());

            // 2. 提取目标父文件夹ID
            String targetParentIdStr = getParameterFromRequestBody(request, "target_parent_id");
            Long targetParentId = targetParentIdStr != null ? Long.parseLong(targetParentIdStr) : null;

            // 3. 调用service层处理移动
            Map<String, Object> result = folderService.moveFolder(userId, folderId, targetParentId);

            // 4. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (NumberFormatException e) {
            sendError(response, "无效的文件夹ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("移动文件夹失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理删除文件夹请求
     */
    private void handleDeleteFolder(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 从URL中提取文件夹ID
            Long folderId = extractFolderIdFromPath(request.getRequestURI());

            // 2. 调用service层处理删除
            Map<String, Object> result = folderService.deleteFolder(userId, folderId);

            // 3. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (NumberFormatException e) {
            sendError(response, "无效的文件夹ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("删除文件夹失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 从URL路径中提取文件夹ID
     */
    private Long extractFolderIdFromPath(String requestURI) {
        // 匹配 /api/v1/folders/{folder_id} 或 /api/v1/folders/{folder_id}/xxx
        String[] parts = requestURI.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("folders".equals(parts[i]) && i + 1 < parts.length) {
                return Long.parseLong(parts[i + 1]);
            }
        }
        throw new IllegalArgumentException("无法从URL中提取文件夹ID");
    }
}
