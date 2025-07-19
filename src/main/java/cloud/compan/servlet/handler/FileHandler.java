package cloud.compan.servlet.handler;

import cloud.compan.servlet.service.FileService;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.annotations.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.util.Map;

/**
 * 文件管理处理器 - 重构版本
 * 只负责HTTP请求解析和响应，具体业务逻辑在FileService中实现
 *
 * 根据RESTFUL API文档实现：
 * POST /api/v1/files/upload - 上传文件
 * GET /api/v1/files/{file_id}/download - 下载文件
 * GET /api/v1/files - 获取文件列表
 * PUT /api/v1/files/{file_id} - 更新文件
 * DELETE /api/v1/files/{file_id} - 删除文件
 * POST /api/v1/files/{file_id}/move - 移动文件
 */
@Service
public class FileHandler extends BaseHandler {

    private final FileService fileService;
    private final AuthService authService;

    public FileHandler() {
        this.fileService = new FileService();
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
                if (requestURI.endsWith("/upload")) {
                    handleUpload(request, response, userId);
                } else if (requestURI.matches(".*/files/\\d+/move$")) {
                    handleMoveFile(request, response, userId);
                } else {
                    sendNotFound(response, "未找到对应的文件接口");
                }
                break;

            case "GET":
                if (requestURI.matches(".*/files/\\d+/download$")) {
                    handleDownload(request, response, userId);
                } else if (requestURI.endsWith("/files")) {
                    handleGetFileList(request, response, userId);
                } else {
                    sendNotFound(response, "未找到对应的文件接口");
                }
                break;

            case "PUT":
                if (requestURI.matches(".*/files/\\d+$")) {
                    handleUpdateFile(request, response, userId);
                } else {
                    sendNotFound(response, "未找到对应的文件接口");
                }
                break;

            case "DELETE":
                if (requestURI.matches(".*/files/\\d+$")) {
                    handleDeleteFile(request, response, userId);
                } else {
                    sendNotFound(response, "未找到对应的文件接口");
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
        return "/api/v1/files/*";
    }

    /**
     * 处理文件上传
     */
    private void handleUpload(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 提取上传参数
            String folderIdStr = request.getParameter("folder_id");
            String description = request.getParameter("description");

            Long folderId = folderIdStr != null ? Long.parseLong(folderIdStr) : null;

            // 2. 获取上传的文件
            Part filePart = request.getPart("file");
            if (filePart == null) {
                sendError(response, "未找到上传文件", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // 3. 调用service层处理上传
            Map<String, Object> result = fileService.uploadFile(userId, folderId, filePart, description);

            // 4. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_CREATED);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (NumberFormatException e) {
            sendError(response, "无效的文件夹ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("文件上传失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理文件下载
     */
    private void handleDownload(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 从URL中提取文件ID
            Long fileId = extractFileIdFromPath(request.getRequestURI());

            // 2. 调用service层处理下载（重构后的方法直接处理response）
            fileService.downloadFile(userId, fileId, response);

            // 下载成功时不需要额外处理，文件内容已直接写入response
        } catch (NumberFormatException e) {
            sendError(response, "无效的文件ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("文件下载失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理获取文件列表
     */
    private void handleGetFileList(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 提取查询参数
            String folderIdStr = request.getParameter("folder_id");
            String page = request.getParameter("page");
            String size = request.getParameter("size");

            Long folderId = folderIdStr != null ? Long.parseLong(folderIdStr) : null;
            int pageNum = page != null ? Integer.parseInt(page) : 1;
            int pageSize = size != null ? Integer.parseInt(size) : 20;

            // 2. 调用service层获取文件列表
            Map<String, Object> result = fileService.getFileList(userId, folderId, pageNum, pageSize);

            // 3. 返回响应
            sendJsonResponse(response, result, HttpServletResponse.SC_OK);

        } catch (NumberFormatException e) {
            sendError(response, "无效的参数格式", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("获取文件列表失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理文件更新
     */
    private void handleUpdateFile(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 从URL中提取文件ID
            Long fileId = extractFileIdFromPath(request.getRequestURI());

            // 2. 提取更新参数
            String filename = getParameterFromRequestBody(request, "filename");
            String description = getParameterFromRequestBody(request, "description");

            // 3. 调用service层处理更新
            Map<String, Object> result = fileService.updateFile(userId, fileId, filename, description);

            // 4. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (NumberFormatException e) {
            sendError(response, "无效的文件ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("文件更新失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理文件删除
     */
    private void handleDeleteFile(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 从URL中提取文件ID
            Long fileId = extractFileIdFromPath(request.getRequestURI());

            // 2. 调用service层处理删除
            Map<String, Object> result = fileService.deleteFile(userId, fileId);

            // 3. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (NumberFormatException e) {
            sendError(response, "无效的文件ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("文件删除失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 处理文件移动
     */
    private void handleMoveFile(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 从URL中提取文件ID
            Long fileId = extractFileIdFromPath(request.getRequestURI());

            // 2. 提取目标文件夹ID
            String targetFolderIdStr = getParameterFromRequestBody(request, "target_folder_id");
            Long targetFolderId = targetFolderIdStr != null ? Long.parseLong(targetFolderIdStr) : null;

            // 3. 调用service层处理移动
            Map<String, Object> result = fileService.moveFile(userId, fileId, targetFolderId);

            // 4. 返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (NumberFormatException e) {
            sendError(response, "无效的文件ID或文件夹ID", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("文件移动失败: " + e.getMessage());
            sendInternalServerError(response, "服务器内部错误");
        }
    }

    /**
     * 从URL路径中提取文件ID
     */
    private Long extractFileIdFromPath(String requestURI) {
        // 匹配 /api/v1/files/{file_id} 或 /api/v1/files/{file_id}/xxx
        String[] parts = requestURI.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("files".equals(parts[i]) && i + 1 < parts.length) {
                return Long.parseLong(parts[i + 1]);
            }
        }
        throw new IllegalArgumentException("无法从URL中提取文件ID");
    }
}
