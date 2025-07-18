package cloud.compan.servlet.handler;

import cloud.compan.servlet.service.FolderService;
import cloud.compan.servlet.annotations.component.Service;
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

    public FolderHandler() {
        this.folderService = new FolderService();
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
                if (requestURI.endsWith("/folders")) {
                    createFolder(request, response, userId);
                } else {
                    sendNotFound(response, "未找到对应的文件夹接口");
                }
                break;

            case "GET":
                if (requestURI.matches(".*/folders/\\d+$")) {
                    String folderId = extractFolderIdFromPath(requestURI);
                    getFolderInfo(request, response, folderId, userId);
                } else if (requestURI.endsWith("/folders")) {
                    getFolderList(request, response, userId);
                } else {
                    sendNotFound(response, "未找到对应的文件夹接口");
                }
                break;

            case "PUT":
                if (requestURI.matches(".*/folders/\\d+/move$")) {
                    String folderId = extractFolderIdFromMovePath(requestURI);
                    moveFolder(request, response, folderId, userId);
                } else if (requestURI.matches(".*/folders/\\d+$")) {
                    String folderId = extractFolderIdFromPath(requestURI);
                    renameFolder(request, response, folderId, userId);
                } else {
                    sendNotFound(response, "未找到对应的文件夹接口");
                }
                break;

            case "DELETE":
                if (requestURI.matches(".*/folders/\\d+$")) {
                    String folderId = extractFolderIdFromPath(requestURI);
                    deleteFolder(request, response, folderId, userId);
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
        return "/api/v1/folders*";
    }

    /**
     * 创建文件夹
     * POST /api/v1/folders
     */
    private void createFolder(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            // 从参数中读取数据
            String folderName = request.getParameter("folder_name");
            String parentIdStr = request.getParameter("parent_folder_id");

            if (folderName == null || folderName.trim().isEmpty()) {
                sendBadRequest(response, "文件夹名称不能为空");
                return;
            }

            Long parentFolderId = null;
            if (parentIdStr != null && !parentIdStr.trim().isEmpty()) {
                try {
                    parentFolderId = Long.parseLong(parentIdStr);
                } catch (NumberFormatException e) {
                    sendBadRequest(response, "无效的父文件夹ID");
                    return;
                }
            }

            // 调用业务逻辑
            Map<String, Object> result = folderService.createFolder(folderName, parentFolderId, userId);

            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_CREATED);
            } else {
                sendBadRequest(response, (String) result.get("message"));
            }

        } catch (Exception e) {
            System.err.println("创建文件夹失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "创建文件夹失败");
        }
    }

    /**
     * 获取文件夹列表
     * GET /api/v1/folders?parent_id=xxx
     */
    private void getFolderList(HttpServletRequest request, HttpServletResponse response, Long userId) throws Exception {
        try {
            String parentIdStr = request.getParameter("parent_id");
            Long parentFolderId = null;

            if (parentIdStr != null && !parentIdStr.trim().isEmpty()) {
                try {
                    parentFolderId = Long.parseLong(parentIdStr);
                } catch (NumberFormatException e) {
                    sendBadRequest(response, "无效的父文件夹ID");
                    return;
                }
            }

            // 调用业务逻辑
            Map<String, Object> result = folderService.getFolderList(userId, parentFolderId);

            sendJsonResponse(response, result, HttpServletResponse.SC_OK);

        } catch (Exception e) {
            System.err.println("获取文件夹列表失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "获取文件夹列表失败");
        }
    }

    /**
     * 获取文件夹详情
     * GET /api/v1/folders/{id}
     */
    private void getFolderInfo(HttpServletRequest request, HttpServletResponse response, String folderIdStr, Long userId) throws Exception {
        try {
            Long folderId = Long.parseLong(folderIdStr);

            // 调用业务逻辑
            Map<String, Object> result = folderService.getFolderInfo(folderId, userId);

            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                sendNotFound(response, (String) result.get("message"));
            }

        } catch (NumberFormatException e) {
            sendBadRequest(response, "无效的文件夹ID");
        } catch (Exception e) {
            System.err.println("获取文件夹详情失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "获取文件夹详情失败");
        }
    }

    /**
     * 重命名文件夹
     * PUT /api/v1/folders/{id}
     */
    private void renameFolder(HttpServletRequest request, HttpServletResponse response, String folderIdStr, Long userId) throws Exception {
        try {
            Long folderId = Long.parseLong(folderIdStr);
            String newName = request.getParameter("folder_name");

            if (newName == null || newName.trim().isEmpty()) {
                sendBadRequest(response, "文件夹名称不能为空");
                return;
            }

            // 调用业务逻辑
            Map<String, Object> result = folderService.renameFolder(folderId, newName, userId);

            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                sendBadRequest(response, (String) result.get("message"));
            }

        } catch (NumberFormatException e) {
            sendBadRequest(response, "无效的文件夹ID");
        } catch (Exception e) {
            System.err.println("重命名文件夹失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "重命名文件夹失败");
        }
    }

    /**
     * 移动文件夹
     * PUT /api/v1/folders/{id}/move
     */
    private void moveFolder(HttpServletRequest request, HttpServletResponse response, String folderIdStr, Long userId) throws Exception {
        try {
            Long folderId = Long.parseLong(folderIdStr);
            String targetParentIdStr = request.getParameter("target_parent_id");

            Long targetParentId = null;
            if (targetParentIdStr != null && !targetParentIdStr.trim().isEmpty()) {
                try {
                    targetParentId = Long.parseLong(targetParentIdStr);
                } catch (NumberFormatException e) {
                    sendBadRequest(response, "无效的目标父文件夹ID");
                    return;
                }
            }

            // 调用业务逻辑
            Map<String, Object> result = folderService.moveFolder(folderId, targetParentId, userId);

            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                sendBadRequest(response, (String) result.get("message"));
            }

        } catch (NumberFormatException e) {
            sendBadRequest(response, "无效的文件夹ID");
        } catch (Exception e) {
            System.err.println("移动文件夹失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "移动文件夹失败");
        }
    }

    /**
     * 删除文件夹
     * DELETE /api/v1/folders/{id}
     */
    private void deleteFolder(HttpServletRequest request, HttpServletResponse response, String folderIdStr, Long userId) throws Exception {
        try {
            Long folderId = Long.parseLong(folderIdStr);

            // 调用业务逻辑
            Map<String, Object> result = folderService.deleteFolder(folderId, userId);

            if ((Boolean) result.get("success")) {
                sendJsonResponse(response, result, HttpServletResponse.SC_OK);
            } else {
                sendNotFound(response, (String) result.get("message"));
            }

        } catch (NumberFormatException e) {
            sendBadRequest(response, "无效的文件夹ID");
        } catch (Exception e) {
            System.err.println("删除文件夹失败: " + e.getMessage());
            e.printStackTrace();
            sendInternalServerError(response, "删除文件夹失败");
        }
    }

    /**
     * 检查用户是否已登录
     */
    private boolean isUserLoggedIn(HttpServletRequest request) {
        return request.getSession().getAttribute("userId") != null;
    }

    /**
     * 从路径中提取文件夹ID
     */
    private String extractFolderIdFromPath(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 1];
    }

    /**
     * 从移动路径中提取文件夹ID
     */
    private String extractFolderIdFromMovePath(String path) {
        String[] parts = path.split("/");
        return parts[parts.length - 2]; // move前面的ID
    }
}
