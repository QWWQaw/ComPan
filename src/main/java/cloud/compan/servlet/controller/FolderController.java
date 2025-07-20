package cloud.compan.servlet.controller;

import cloud.compan.servlet.annotations.*;
import cloud.compan.servlet.service.FolderService;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.dto.*;
import cloud.compan.servlet.model.Folder;
import cloud.compan.servlet.web.response.ApiResponseWrapper;
import com.google.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.List;

/**
 * 文件夹控制器
 * 继承BaseController，处理文件夹管理相关的HTTP请求
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
@Controller("/api/folders")
@ResponseBody
public class FolderController extends BaseController {
    
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
            return error(401, "未认证");
        }
        
        String folderName = (String) requestData.get("folder_name");
        Number parentFolderIdObj = (Number) requestData.get("parent_folder_id");
        
        if (folderName == null || folderName.trim().isEmpty()) {
            return error(400, "文件夹名称不能为空");
        }
        
        Long parentFolderId = parentFolderIdObj != null ? parentFolderIdObj.longValue() : null;
        
        ServiceResult<Folder> result = folderService.createFolder(folderName, parentFolderId, userId);
        return handleServiceResult(result);
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
            return error(401, "未认证");
        }
        
        @SuppressWarnings("unchecked")
        List<String> folderNames = (List<String>) requestData.get("folder_names");
        Number parentFolderIdObj = (Number) requestData.get("parent_folder_id");
        
        if (folderNames == null || folderNames.isEmpty()) {
            return error(400, "文件夹名称列表不能为空");
        }
        
        Long parentFolderId = parentFolderIdObj != null ? parentFolderIdObj.longValue() : null;
        
        ServiceResult<List<Folder>> result = folderService.createFolders(folderNames, parentFolderId, userId);
        return handleServiceResult(result);
    }
    
    // ============ 文件夹查询相关 ============
    
    /**
     * 获取文件夹列表
     * GET /api/folders
     */
    @GetMapping
    public ApiResponseWrapper getFolders(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        String parentFolderIdStr = request.getParameter("parent_folder_id");
        String sortBy = request.getParameter("sort");
        String sortOrder = request.getParameter("order");
        
        int page = parseIntParam(request, "page", 1);
        int size = parseIntParam(request, "per_page", 20);
        
        Long parentFolderId = parentFolderIdStr != null ? Long.parseLong(parentFolderIdStr) : null;
        
        ServiceResult<PageResultDTO<Folder>> result = folderService.getSubFolders(
            parentFolderId, userId, page, size, sortBy, sortOrder);
            
        return handleServiceResult(result);
    }
    
    /**
     * 获取文件夹详情
     * GET /api/folders/{id}
     */
    @GetMapping(path = "/{id}")
    public ApiResponseWrapper getFolderDetails(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件夹ID不能为空");
        }
        
        ServiceResult<Folder> result = folderService.getFolderDetails(id, userId);
        return handleServiceResult(result);
    }
    
    /**
     * 获取文件夹路径（面包屑导航）
     * GET /api/folders/{id}/path
     */
    @GetMapping(path = "/{id}/path")
    public ApiResponseWrapper getFolderPath(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件夹ID不能为空");
        }
        
        ServiceResult<List<Map<String, Object>>> result = folderService.getFolderPath(id, userId);
        return handleServiceResult(result);
    }
    
    /**
     * 搜索文件夹
     * GET /api/folders/search
     */
    @GetMapping(path = "/search")
    public ApiResponseWrapper searchFolders(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        String keyword = request.getParameter("keyword");
        if (keyword == null || keyword.trim().isEmpty()) {
            return error(400, "搜索关键词不能为空");
        }
        
        int page = parseIntParam(request, "page", 1);
        int size = parseIntParam(request, "per_page", 20);
        
        ServiceResult<PageResultDTO<Folder>> result = folderService.searchFolders(keyword, userId, page, size);
        return handleServiceResult(result);
    }
    
    /**
     * 获取文件夹树形结构
     * GET /api/folders/tree
     */
    @GetMapping(path = "/tree")
    public ApiResponseWrapper getFolderTree(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        String rootFolderIdStr = request.getParameter("root_folder_id");
        int maxDepth = parseIntParam(request, "max_depth", 3);
        
        Long rootFolderId = rootFolderIdStr != null ? Long.parseLong(rootFolderIdStr) : null;
        
        ServiceResult<Map<String, Object>> result = folderService.getFolderTree(rootFolderId, userId, maxDepth);
        return handleServiceResult(result);
    }
    
    // ============ 文件夹操作相关 ============
    
    /**
     * 重命名文件夹
     * PATCH /api/folders/{id}
     */
    @PatchMapping(path = "/{id}")
    public ApiResponseWrapper renameFolder(@PathVariable Long id,
                                          @RequestBody Map<String, Object> requestData,
                                          HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件夹ID不能为空");
        }
        
        String newFolderName = (String) requestData.get("folder_name");
        if (newFolderName == null || newFolderName.trim().isEmpty()) {
            return error(400, "新文件夹名称不能为空");
        }
        
        ServiceResult<Folder> result = folderService.renameFolder(id, newFolderName, userId);
        return handleServiceResult(result);
    }
    
    /**
     * 移动文件夹
     * PATCH /api/folders/{id}/move
     */
    @PatchMapping(path = "/{id}/move")
    public ApiResponseWrapper moveFolder(@PathVariable Long id,
                                        @RequestBody Map<String, Object> requestData,
                                        HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件夹ID不能为空");
        }
        
        Number targetParentFolderIdObj = (Number) requestData.get("target_parent_folder_id");
        if (targetParentFolderIdObj == null) {
            return error(400, "目标父文件夹ID不能为空");
        }
        
        Long targetParentFolderId = targetParentFolderIdObj.longValue();
        
        ServiceResult<Folder> result = folderService.moveFolder(id, targetParentFolderId, userId);
        return handleServiceResult(result);
    }
    
    /**
     * 复制文件夹
     * POST /api/folders/{id}/copy
     */
    @PostMapping(path = "/{id}/copy")
    public ApiResponseWrapper copyFolder(@PathVariable Long id,
                                        @RequestBody Map<String, Object> requestData,
                                        HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件夹ID不能为空");
        }
        
        Number targetParentFolderIdObj = (Number) requestData.get("target_parent_folder_id");
        String newFolderName = (String) requestData.get("new_folder_name");
        Boolean copyContents = (Boolean) requestData.get("copy_contents");
        
        if (targetParentFolderIdObj == null) {
            return error(400, "目标父文件夹ID不能为空");
        }
        
        Long targetParentFolderId = targetParentFolderIdObj.longValue();
        boolean shouldCopyContents = copyContents != null ? copyContents : true;
        
        ServiceResult<Folder> result = folderService.copyFolder(
            id, targetParentFolderId, newFolderName, userId, shouldCopyContents);
        return handleServiceResult(result);
    }
    
    /**
     * 删除文件夹
     * DELETE /api/folders/{id}
     */
    @DeleteMapping(path = "/{id}")
    public ApiResponseWrapper deleteFolder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件夹ID不能为空");
        }
        
        String deleteContentsStr = request.getParameter("delete_contents");
        boolean deleteContents = "true".equals(deleteContentsStr);
        
        ServiceResult<Boolean> result = folderService.deleteFolder(id, userId, deleteContents);
        return handleServiceResult(result);
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
            return error(401, "未认证");
        }
        
        String action = (String) requestData.get("action");
        @SuppressWarnings("unchecked")
        List<Number> folderIdNumbers = (List<Number>) requestData.get("folder_ids");
        Number targetParentFolderIdObj = (Number) requestData.get("target_parent_folder_id");
        
        if (action == null || action.trim().isEmpty()) {
            return error(400, "操作类型不能为空");
        }
        
        if (folderIdNumbers == null || folderIdNumbers.isEmpty()) {
            return error(400, "文件夹ID列表不能为空");
        }
        
        List<Long> folderIds = folderIdNumbers.stream()
            .map(Number::longValue)
            .toList();
            
        Long targetParentFolderId = targetParentFolderIdObj != null ? targetParentFolderIdObj.longValue() : null;
        
        ServiceResult<Map<String, Object>> result = folderService.batchOperateFolders(
            folderIds, action, targetParentFolderId, userId);
            
        return handleServiceResult(result);
    }
    
    // ============ 文件夹权限和统计相关 ============
    
    /**
     * 获取文件夹权限列表
     * GET /api/folders/{id}/permissions
     */
    @GetMapping(path = "/{id}/permissions")
    public ApiResponseWrapper getFolderPermissions(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件夹ID不能为空");
        }
        
        ServiceResult<List<Map<String, Object>>> result = folderService.getFolderPermissions(id, userId);
        return handleServiceResult(result);
    }
    
    /**
     * 获取文件夹统计信息
     * GET /api/folders/{id}/statistics
     */
    @GetMapping(path = "/{id}/statistics")
    public ApiResponseWrapper getFolderStatistics(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件夹ID不能为空");
        }
        
        ServiceResult<Map<String, Object>> result = folderService.getFolderStatistics(id, userId);
        return handleServiceResult(result);
    }
    
    /**
     * 获取用户根目录
     * GET /api/folders/root
     */
    @GetMapping(path = "/root")
    public ApiResponseWrapper getRootFolder(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        ServiceResult<Folder> result = folderService.getRootFolder(userId);
        return handleServiceResult(result);
    }
    
    // ============ 辅助方法 ============
    
    /**
     * 从请求中获取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token == null) {
            return null;
        }
        
        ServiceResult<Long> result = authService.extractUserIdFromToken(token);
        return result.isSuccess() ? result.getData() : null;
    }
    
    /**
     * 从请求头中提取JWT token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
} 