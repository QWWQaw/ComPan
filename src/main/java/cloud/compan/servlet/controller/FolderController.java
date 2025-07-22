package cloud.compan.servlet.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.DeleteMapping;
import cloud.compan.servlet.annotations.GetMapping;
import cloud.compan.servlet.annotations.PatchMapping;
import cloud.compan.servlet.annotations.PathVariable;
import cloud.compan.servlet.annotations.PostMapping;
import cloud.compan.servlet.annotations.RequestBody;
import cloud.compan.servlet.annotations.RestController;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.model.Folder;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.service.FolderService;
import cloud.compan.servlet.utils.ControllerUtils;
import cloud.compan.servlet.web.response.ApiResponseWrapper;

/**
 * 文件夹控制器
 * 处理文件夹管理相关的HTTP请求
 * 
 * 职责：
 * - 处理HTTP请求参数
 * - 调用Service层接口
 * - 统一响应格式
 * 
 * 实现API 1.3文档中的文件夹管理路由：
 * - POST /api/folders - 创建文件夹
 * - GET /api/folders - 获取文件夹列表
 * - GET /api/folders/{id} - 获取文件夹详情
 * - PATCH /api/folders/{id} - 重命名文件夹
 * - DELETE /api/folders/{id} - 删除文件夹
 * - POST /api/folders/batch - 批量操作文件夹
 */
@RestController("/api/v1/folders")
@Singleton
public class FolderController {
    
    @Inject
    private FolderService folderService;
    
    @Inject
    private AuthService authService;
    
    // ============ 文件夹创建相关 ============
    
    /**
     * 创建文件夹
     * POST /api/folders
     */
    @PostMapping
    public ApiResponseWrapper createFolder(@RequestBody Map<String, Object> requestData,
                                          HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String folderName = (String) requestData.get("folder_name");
        Number parentFolderIdObj = (Number) requestData.get("parent_folder_id");
        
        if (folderName == null || folderName.trim().isEmpty()) {
            return ControllerUtils.error(400, "文件夹名称不能为空");
        }
        
        Long parentFolderId = parentFolderIdObj != null ? parentFolderIdObj.longValue() : null;
        
        ServiceResult<Folder> result = folderService.createFolder(folderName, parentFolderId, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 批量创建文件夹
     * POST /api/folders/batch
     */
    @PostMapping(path = "/batch")
    public ApiResponseWrapper createFolders(@RequestBody Map<String, Object> requestData,
                                           HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        @SuppressWarnings("unchecked")
        List<String> folderNames = (List<String>) requestData.get("folder_names");
        Number parentFolderIdObj = (Number) requestData.get("parent_folder_id");
        
        if (folderNames == null || folderNames.isEmpty()) {
            return ControllerUtils.error(400, "文件夹名称列表不能为空");
        }
        
        Long parentFolderId = parentFolderIdObj != null ? parentFolderIdObj.longValue() : null;
        
        ServiceResult<List<Folder>> result = folderService.createFolders(folderNames, parentFolderId, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    // ============ 文件夹查询相关 ============
    
    /**
     * 获取文件夹列表
     * GET /api/folders?parent_folder_id=123&page=1&size=20
     */
    @GetMapping
    public ApiResponseWrapper getFolders(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String parentFolderIdStr = request.getParameter("parent_folder_id");
        int page = ControllerUtils.parseIntParam(request, "page", 1);
        int size = ControllerUtils.parseIntParam(request, "size", 20);
        String sortBy = request.getParameter("sort_by");
        String sortOrder = request.getParameter("sort_order");
        
        Long parentFolderId = parentFolderIdStr != null ? Long.parseLong(parentFolderIdStr) : null;
        
        ServiceResult<PageResultDTO<Folder>> result = folderService.getFolders(userId, parentFolderId, page, size, sortBy, sortOrder);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取文件夹详情
     * GET /api/folders/{id}
     */
    @GetMapping(path = "/{id}")
    public ApiResponseWrapper getFolderDetails(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Folder> result = folderService.getFolderDetails(id, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取文件夹路径
     * GET /api/folders/{id}/path
     */
    @GetMapping(path = "/{id}/path")
    public ApiResponseWrapper getFolderPath(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<List<Folder>> result = folderService.getFolderPath(id, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 搜索文件夹
     * GET /api/folders/search?keyword=test
     */
    @GetMapping(path = "/search")
    public ApiResponseWrapper searchFolders(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String keyword = request.getParameter("keyword");
        String parentFolderIdStr = request.getParameter("parent_folder_id");
        int page = ControllerUtils.parseIntParam(request, "page", 1);
        int size = ControllerUtils.parseIntParam(request, "size", 20);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return ControllerUtils.error(400, "搜索关键词不能为空");
        }
        
        Long parentFolderId = parentFolderIdStr != null ? Long.parseLong(parentFolderIdStr) : null;
        
        ServiceResult<PageResultDTO<Folder>> result = folderService.searchFolders(
            userId, keyword, parentFolderId, page, size);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取文件夹树结构
     * GET /api/folders/tree?root_folder_id=123
     */
    @GetMapping(path = "/tree")
    public ApiResponseWrapper getFolderTree(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String rootFolderIdStr = request.getParameter("root_folder_id");
        Long rootFolderId = rootFolderIdStr != null ? Long.parseLong(rootFolderIdStr) : null;
        
        ServiceResult<Map<String, Object>> result = folderService.getFolderTree(userId, rootFolderId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    // ============ 文件夹操作相关 ============
    
    /**
     * 重命名文件夹
     * PATCH /api/folders/{id}
     */
    @PatchMapping(path = "/{id}")
    public ApiResponseWrapper renameFolder(@PathVariable("id") Long id,
                                          @RequestBody Map<String, Object> requestData,
                                          HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String newName = (String) requestData.get("name");
        if (newName == null || newName.trim().isEmpty()) {
            return ControllerUtils.error(400, "新文件夹名称不能为空");
        }
        
        ServiceResult<Folder> result = folderService.renameFolder(id, newName, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 移动文件夹
     * PATCH /api/folders/{id}/move
     */
    @PatchMapping(path = "/{id}/move")
    public ApiResponseWrapper moveFolder(@PathVariable("id") Long id,
                                        @RequestBody Map<String, Object> requestData,
                                        HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        Number targetParentIdObj = (Number) requestData.get("target_parent_id");
        if (targetParentIdObj == null) {
            return ControllerUtils.error(400, "目标父文件夹ID不能为空");
        }
        
        Long targetParentId = targetParentIdObj.longValue();
        ServiceResult<Folder> result = folderService.moveFolder(id, targetParentId, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 复制文件夹
     * POST /api/folders/{id}/copy
     */
    @PostMapping(path = "/{id}/copy")
    public ApiResponseWrapper copyFolder(@PathVariable("id") Long id,
                                        @RequestBody Map<String, Object> requestData,
                                        HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        Number targetParentIdObj = (Number) requestData.get("target_parent_id");
        String newName = (String) requestData.get("new_name");
        Boolean includeSubfolders = (Boolean) requestData.get("include_subfolders");
        
        if (targetParentIdObj == null) {
            return ControllerUtils.error(400, "目标父文件夹ID不能为空");
        }
        
        Long targetParentId = targetParentIdObj.longValue();
        if (includeSubfolders == null) {
            includeSubfolders = true;
        }
        
        ServiceResult<Folder> result = folderService.copyFolder(id, targetParentId, newName, includeSubfolders, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 删除文件夹
     * DELETE /api/folders/{id}
     */
    @DeleteMapping(path = "/{id}")
    public ApiResponseWrapper deleteFolder(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        Boolean recursive = ControllerUtils.parseBooleanParam(request, "recursive", false);
        
        ServiceResult<Boolean> result = folderService.deleteFolder(id, recursive, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 批量操作文件夹
     * POST /api/folders/batch-operation
     */
    @PostMapping(path = "/batch-operation")
    public ApiResponseWrapper batchOperateFolders(@RequestBody Map<String, Object> requestData,
                                                  HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String operation = (String) requestData.get("operation");
        @SuppressWarnings("unchecked")
        List<Long> folderIds = (List<Long>) requestData.get("folder_ids");
        
        if (operation == null || folderIds == null || folderIds.isEmpty()) {
            return ControllerUtils.error(400, "缺少必需参数: operation, folder_ids");
        }
        
        ServiceResult<Map<String, Object>> result = folderService.batchOperateFolders(operation, folderIds, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    // ============ 文件夹权限和统计相关 ============
    
    /**
     * 获取文件夹权限
     * GET /api/folders/{id}/permissions
     */
    @GetMapping(path = "/{id}/permissions")
    public ApiResponseWrapper getFolderPermissions(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = folderService.getFolderPermissions(id, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取文件夹统计信息
     * GET /api/folders/{id}/statistics
     */
    @GetMapping(path = "/{id}/statistics")
    public ApiResponseWrapper getFolderStatistics(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = folderService.getFolderStatistics(id, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取根文件夹
     * GET /api/folders/root
     */
    @GetMapping(path = "/root")
    public ApiResponseWrapper getRootFolder(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Folder> result = folderService.getRootFolder(userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 从token中获取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = ControllerUtils.extractTokenFromRequest(request);
        if (token == null) {
            return null;
        }
        
        ServiceResult<Long> result = authService.extractUserIdFromToken(token);
        if (!result.isSuccess()) {
            return null;
        }
        
        return result.getData();
    }
} 