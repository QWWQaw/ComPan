package cloud.compan.servlet.handler;

import cloud.compan.servlet.service.FileService;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.annotations.component.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.util.Map;

/**
 * 文件管理处理器 - 重构版本
 * 只负责HTTP请求解析和响应，具体业务逻辑在FileService中实现
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
                } else {
                    sendNotFound(response, "未找到对应的文件接口");
                }
                break;

            case "GET":
                if (requestURI.matches(".*/files/\\d+/download$")) {
                    String fileId = extractFileIdFromDownloadPath(requestURI);
                    handleDownload(request, response, fileId, userId);
                } else if (requestURI.matches(".*/files/\\d+$")) {
                    String fileId = extractFileIdFromPath(requestURI);
                    handleGetFileInfo(request, response, fileId, userId);
                } else if (requestURI.endsWith("/files")) {
                    handleGetFileList(request, response, userId);
                } else {
                    sendNotFound(response, "未找到对应的文件接口");
                }
                break;

            case "PUT":
                if (requestURI.matches(".*/files/\\d+/move$")) {
                    String fileId = extractFileIdFromMovePath(requestURI);
                    handleMoveFile(request, response, fileId, userId);
                } else if (requestURI.matches(".*/files/\\d+$")) {
                    String fileId = extractFileIdFromPath(requestURI);
                    handleRenameFile(request, response, fileId, userId);
                } else {
                    sendNotFound(response, "未找到对应的文件接口");
                }
                break;

            case "DELETE":
                if (requestURI.matches(".*/files/\\d+$")) {
                    String fileId = extractFileIdFromPath(requestURI);
                    handleDeleteFile(request, response, fileId, userId);
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
        return "/api/v1/files*";
    }

    /**
     * 处理文件上传
     */
    private void handleUpload(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 提取请求参数
            Part filePart = request.getPart("file");
            String fileName = request.getParameter("file_name");
            String folderIdStr = request.getParameter("folder_id");

            // 2. 调用service层处理上传逻辑
            Map<String, Object> result = fileService.handleFileUpload(filePart, fileName, folderIdStr, userId);

            // 3. 根据结果返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_CREATED);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (Exception e) {
            System.err.println("文件上传请求处理失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "文件上传失败");
        }
    }

    /**
     * 处理获取文件列表
     */
    private void handleGetFileList(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 1. 提取请求参数
            String folderIdStr = request.getParameter("folder_id");
            String pageStr = request.getParameter("page");
            String perPageStr = request.getParameter("per_page");
            String sortBy = request.getParameter("sort_by");
            String sortOrder = request.getParameter("sort_order");

            // 2. 调用service层处理逻辑
            Map<String, Object> result = fileService.handleGetFileList(folderIdStr, pageStr, perPageStr, sortBy, sortOrder, userId);

            // 3. 返回响应
            sendJsonResponse(response, result, HttpServletResponse.SC_OK);

        } catch (Exception e) {
            System.err.println("获取文件列表请求处理失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "获取文件列表失败");
        }
    }

    /**
     * 处理获取文件详情
     */
    private void handleGetFileInfo(HttpServletRequest request, HttpServletResponse response, String fileIdStr, Long userId) throws Exception {
        try {
            // 调用service层处理逻辑
            Map<String, Object> result = fileService.handleGetFileInfo(fileIdStr, userId);

            // 根据结果返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 404);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (Exception e) {
            System.err.println("获取文件详情请求处理失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "获取文件详情失败");
        }
    }

    /**
     * 处理文件下载
     */
    private void handleDownload(HttpServletRequest request, HttpServletResponse response, String fileIdStr, Long userId) throws Exception {
        try {
            // 调用service层处理下载逻辑
            fileService.handleFileDownload(fileIdStr, userId, response);

        } catch (Exception e) {
            System.err.println("文件下载请求处理失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "文件下载失败");
        }
    }

    /**
     * 处理文件重命名
     */
    private void handleRenameFile(HttpServletRequest request, HttpServletResponse response, String fileIdStr, Long userId) throws Exception {
        try {
            // 1. 提取请求参数
            String newName = request.getParameter("file_name");

            // 2. 调用service层处理逻辑
            Map<String, Object> result = fileService.handleRenameFile(fileIdStr, newName, userId);

            // 3. 根据结果返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (Exception e) {
            System.err.println("重命名文件请求处理失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "重命名文件失败");
        }
    }

    /**
     * 处理移动文件
     */
    private void handleMoveFile(HttpServletRequest request, HttpServletResponse response, String fileIdStr, Long userId) throws Exception {
        try {
            // 1. 提取请求参数
            String targetFolderIdStr = request.getParameter("target_folder_id");

            // 2. 调用service层处理逻辑
            Map<String, Object> result = fileService.handleMoveFile(fileIdStr, targetFolderIdStr, userId);

            // 3. 根据结果返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 400);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (Exception e) {
            System.err.println("移动文件请求处理失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "移动文件失败");
        }
    }

    /**
     * 处理删除文件
     */
    private void handleDeleteFile(HttpServletRequest request, HttpServletResponse response, String fileIdStr, Long userId) throws Exception {
        try {
            // 调用service层处理逻辑
            Map<String, Object> result = fileService.handleDeleteFile(fileIdStr, userId);

            // 根据结果返回响应
            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                int statusCode = (Integer) result.getOrDefault("status_code", 404);
                sendJsonResponse(response, result, statusCode);
            }

        } catch (Exception e) {
            System.err.println("删除文件请求处理失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "删除文件失败");
        }
    }

    // ================== 工具方法 ==================

    /**
     * 从路径中提取文件ID
     */
    private String extractFileIdFromPath(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }

    /**
     * 从下载路径中提取文件ID
     */
    private String extractFileIdFromDownloadPath(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 2]; // download前面的ID
    }

    /**
     * 从移动路径中提取文件ID
     */
    private String extractFileIdFromMovePath(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 2]; // move前面的ID
    }
}
