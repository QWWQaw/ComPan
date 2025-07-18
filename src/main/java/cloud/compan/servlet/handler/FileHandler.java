package cloud.compan.servlet.handler;

import cloud.compan.servlet.service.FileService;
import cloud.compan.servlet.entity.FileEntity;
import cloud.compan.servlet.annotations.component.Service;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * 文件管理处理器
 *
 * 根据RESTFUL API文档实现：
 * POST /api/v1/files/upload - 上传文件
 * GET /api/v1/files - 获取文件列表
 * GET /api/v1/files/{id} - 获取文件详情
 * GET /api/v1/files/{id}/download - 下载文件
 * PUT /api/v1/files/{id} - 重命名文件
 * PUT /api/v1/files/{id}/move - 移动文件
 * DELETE /api/v1/files/{id} - 删除文件
 */
@Service
public class FileHandler extends BaseHandler {

    private final FileService fileService;

    public FileHandler() {
        this.fileService = new FileService();
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod().toUpperCase();

        // 检查用户是否已登录
        if (!isUserLoggedIn(request)) {
            sendError(response, "请先登录", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Long userId = (Long) request.getSession().getAttribute("userId");

        // 根据HTTP方法和路径分发请求
        switch (method) {
            case "POST":
                if (requestURI.endsWith("/upload")) {
                    uploadFile(request, response, userId);
                } else {
                    sendNotFound(response, "未找到对应的文件接口");
                }
                break;

            case "GET":
                if (requestURI.matches(".*/files/\\d+/download$")) {
                    String fileId = extractFileIdFromDownloadPath(requestURI);
                    downloadFile(request, response, fileId, userId);
                } else if (requestURI.matches(".*/files/\\d+$")) {
                    String fileId = extractFileIdFromPath(requestURI);
                    getFileInfo(request, response, fileId, userId);
                } else if (requestURI.endsWith("/files")) {
                    getFileList(request, response, userId);
                } else {
                    sendNotFound(response, "未找到对应的文件接口");
                }
                break;

            case "PUT":
                if (requestURI.matches(".*/files/\\d+/move$")) {
                    String fileId = extractFileIdFromMovePath(requestURI);
                    moveFile(request, response, fileId, userId);
                } else if (requestURI.matches(".*/files/\\d+$")) {
                    String fileId = extractFileIdFromPath(requestURI);
                    renameFile(request, response, fileId, userId);
                } else {
                    sendNotFound(response, "未找到对应的文件接口");
                }
                break;

            case "DELETE":
                if (requestURI.matches(".*/files/\\d+$")) {
                    String fileId = extractFileIdFromPath(requestURI);
                    deleteFile(request, response, fileId, userId);
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
     * 上传文件
     * POST /api/v1/files/upload
     */
    private void uploadFile(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 获取文件部分
            Part filePart = request.getPart("file");
            String fileName = request.getParameter("file_name");
            String folderIdStr = request.getParameter("folder_id");

            if (filePart == null) {
                sendBadRequest(response, "请选择要上传的文件");
                return;
            }

            // 如果没有指定文件名，使用原始文件名
            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = getFileName(filePart);
            }

            if (fileName == null || fileName.trim().isEmpty()) {
                sendBadRequest(response, "文件名不能为空");
                return;
            }

            // 解析文件夹ID
            Long folderId = 1L; // 默认根文件夹ID
            if (folderIdStr != null && !folderIdStr.trim().isEmpty()) {
                try {
                    folderId = Long.parseLong(folderIdStr);
                } catch (NumberFormatException e) {
                    sendBadRequest(response, "无效的文件夹ID");
                    return;
                }
            }

            // 调用业务逻辑
            Map<String, Object> result = fileService.uploadFile(filePart, fileName, userId, folderId);

            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_CREATED);
            } else {
                sendBadRequest(response, (String) result.get("message"));
            }

        } catch (Exception e) {
            System.err.println("文件上传失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "文件上传失败");
        }
    }

    /**
     * 获取文件列表
     * GET /api/v1/files?folder_id=xxx
     */
    private void getFileList(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            String folderIdStr = request.getParameter("folder_id");
            Long folderId = 1L; // 默认根文件夹ID

            if (folderIdStr != null && !folderIdStr.trim().isEmpty()) {
                try {
                    folderId = Long.parseLong(folderIdStr);
                } catch (NumberFormatException e) {
                    sendBadRequest(response, "无效的文件夹ID");
                    return;
                }
            }

            // 调用业务逻辑
            Map<String, Object> result = fileService.getFileList(userId, folderId);

            sendJsonResponse(response, result, HttpServletResponse.SC_OK);

        } catch (Exception e) {
            System.err.println("获取文件列表失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "获取文件列表失败");
        }
    }

    /**
     * 获取文件详情
     * GET /api/v1/files/{id}
     */
    private void getFileInfo(HttpServletRequest request, HttpServletResponse response, String fileIdStr, Long userId) throws Exception {
        try {
            Long fileId = Long.parseLong(fileIdStr);

            // 调用业务逻辑
            Map<String, Object> result = fileService.getFileInfo(fileId, userId);

            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                sendNotFound(response, (String) result.get("message"));
            }

        } catch (NumberFormatException e) {
            sendBadRequest(response, "无效的文件ID");
        } catch (Exception e) {
            System.err.println("获取文件详情失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "获取文件详情失败");
        }
    }

    /**
     * 下载文件
     * GET /api/v1/files/{id}/download
     */
    private void downloadFile(HttpServletRequest request, HttpServletResponse response, String fileIdStr, Long userId) throws Exception {
        try {
            Long fileId = Long.parseLong(fileIdStr);

            // 获取文件信息
            FileEntity file = fileService.getFileForDownload(fileId, userId);

            if (file == null) {
                sendNotFound(response, "文件不存在或无权限访问");
                return;
            }

            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getFileName() + "\"");
            response.setContentLengthLong(file.getFileSize());

            // 读取文件并写入响应流
            try (FileInputStream fis = new FileInputStream(file.getStoragePath());
                 OutputStream os = response.getOutputStream()) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }

        } catch (NumberFormatException e) {
            sendBadRequest(response, "无效的文件ID");
        } catch (Exception e) {
            System.err.println("文件下载失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "文件下载失败");
        }
    }

    /**
     * 重命名文件
     * PUT /api/v1/files/{id}
     */
    private void renameFile(HttpServletRequest request, HttpServletResponse response, String fileIdStr, Long userId) throws Exception {
        try {
            Long fileId = Long.parseLong(fileIdStr);
            String newName = request.getParameter("file_name");

            if (newName == null || newName.trim().isEmpty()) {
                sendBadRequest(response, "文件名不能为空");
                return;
            }

            // 调用业务逻辑
            Map<String, Object> result = fileService.renameFile(fileId, newName, userId);

            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                sendBadRequest(response, (String) result.get("message"));
            }

        } catch (NumberFormatException e) {
            sendBadRequest(response, "无效的文件ID");
        } catch (Exception e) {
            System.err.println("重命名文件失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "重命名文件失败");
        }
    }

    /**
     * 移动文件
     * PUT /api/v1/files/{id}/move
     */
    private void moveFile(HttpServletRequest request, HttpServletResponse response, String fileIdStr, Long userId) throws Exception {
        try {
            Long fileId = Long.parseLong(fileIdStr);
            String targetFolderIdStr = request.getParameter("target_folder_id");

            if (targetFolderIdStr == null || targetFolderIdStr.trim().isEmpty()) {
                sendBadRequest(response, "目标文件夹ID不能为空");
                return;
            }

            Long targetFolderId = Long.parseLong(targetFolderIdStr);

            // 调用业务逻辑
            Map<String, Object> result = fileService.moveFile(fileId, targetFolderId, userId);

            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                sendBadRequest(response, (String) result.get("message"));
            }

        } catch (NumberFormatException e) {
            sendBadRequest(response, "无效的文件ID或目标文件夹ID");
        } catch (Exception e) {
            System.err.println("移动文件失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "移动文件失败");
        }
    }

    /**
     * 删除文件
     * DELETE /api/v1/files/{id}
     */
    private void deleteFile(HttpServletRequest request, HttpServletResponse response, String fileIdStr, Long userId) throws Exception {
        try {
            Long fileId = Long.parseLong(fileIdStr);

            // 调用业务逻辑
            Map<String, Object> result = fileService.deleteFile(fileId, userId);

            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                sendNotFound(response, (String) result.get("message"));
            }

        } catch (NumberFormatException e) {
            sendBadRequest(response, "无效的文件ID");
        } catch (Exception e) {
            System.err.println("删除文件失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "删除文件失败");
        }
    }

    /**
     * 检查用户是否已登录
     */
    private boolean isUserLoggedIn(HttpServletRequest request) {
        return request.getSession().getAttribute("userId") != null;
    }

    /**
     * 从文件上传请求中获取文件名
     */
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (String content : contentDisposition.split(";")) {
                if (content.trim().startsWith("filename")) {
                    return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
                }
            }
        }
        return null;
    }

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
