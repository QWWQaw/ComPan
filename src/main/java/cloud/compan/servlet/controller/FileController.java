package cloud.compan.servlet.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.DeleteMapping;
import cloud.compan.servlet.annotations.GetMapping;
import cloud.compan.servlet.annotations.PatchMapping;
import cloud.compan.servlet.annotations.PathVariable;
import cloud.compan.servlet.annotations.PostMapping;
import cloud.compan.servlet.annotations.RequestBody;
import cloud.compan.servlet.annotations.RequestParam;
import cloud.compan.servlet.annotations.RestController;
import cloud.compan.servlet.dto.FileDTO;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.service.FileService;
import cloud.compan.servlet.utils.ControllerUtils;
import cloud.compan.servlet.web.response.ApiResponseWrapper;

/**
 * 文件控制器
 * 处理文件管理相关的HTTP请求
 * 
 * 职责：
 * - 处理HTTP请求参数
 * - 调用Service层接口
 * - 统一响应格式
 * 
 * 实现API 1.3文档中的文件管理路由：
 * - POST /api/files/upload - 文件上传
 * - GET /api/files - 获取文件列表
 * - GET /api/files/{id} - 获取文件详情
 * - GET /api/files/{id}/download - 文件下载
 * - PATCH /api/files/{id} - 重命名文件
 * - DELETE /api/files/{id} - 删除文件
 * - POST /api/files/batch - 批量操作文件
 */
@RestController("/api/v1/files")
@Singleton
public class FileController {
    
    @Inject
    private FileService fileService;
    
    @Inject
    private AuthService authService;
    
    // ============ 文件上传相关 ============
    
    /**
     * 文件上传
     * POST /api/files/upload
     */
    @PostMapping(path = "/upload")
    public ApiResponseWrapper uploadFile(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        // 提取参数
        String fileName = request.getParameter("file_name");
        String fileSizeStr = request.getParameter("file_size");
        String fileHash = request.getParameter("file_hash");
        String folderIdStr = request.getParameter("folder_id");
        String mimeType = request.getParameter("mime_type");
        
        // 基本参数验证
        if (fileName == null || fileSizeStr == null) {
            return ControllerUtils.error(400, "缺少必需参数: file_name, file_size");
        }
        
        try {
            long fileSize = Long.parseLong(fileSizeStr);
            Long folderId = folderIdStr != null ? Long.parseLong(folderIdStr) : null;
            
            // 直接调用Service层处理文件上传
            ServiceResult<FileDTO> result = fileService.uploadFile(
                fileName, fileSize, mimeType, folderId, null, fileHash, userId);
                
            return ControllerUtils.handleServiceResult(result);
            
        } catch (NumberFormatException e) {
            return ControllerUtils.error(400, "参数格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 分片上传初始化
     * POST /api/files/multipart-upload/init
     */
    @PostMapping(path = "/multipart-upload/init")
    public ApiResponseWrapper initMultipartUpload(@RequestBody Map<String, Object> requestData,
                                                  HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String fileName = (String) requestData.get("file_name");
        String fileSizeStr = (String) requestData.get("file_size");
        String folderIdStr = (String) requestData.get("folder_id");
        String mimeType = (String) requestData.get("mime_type");
        
        if (fileName == null || fileSizeStr == null) {
            return ControllerUtils.error(400, "缺少必需参数: file_name, file_size");
        }
        
        try {
            long fileSize = Long.parseLong(fileSizeStr);
            Long folderId = folderIdStr != null ? Long.parseLong(folderIdStr) : null;
            
            ServiceResult<Map<String, Object>> result = fileService.initMultipartUpload(
                fileName, fileSize, mimeType, folderId, userId);
                
            return ControllerUtils.handleServiceResult(result);
            
        } catch (NumberFormatException e) {
            return ControllerUtils.error(400, "参数格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 分片上传
     * POST /api/files/multipart-upload/{sessionId}/chunk
     */
    @PostMapping(path = "/multipart-upload/{sessionId}/chunk")
    public ApiResponseWrapper uploadChunk(@PathVariable("sessionId") String sessionId,
                                         @RequestBody Map<String, Object> requestData,
                                         HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        Number chunkIndexObj = (Number) requestData.get("chunk_index");
        Number totalChunksObj = (Number) requestData.get("total_chunks");
        String chunkData = (String) requestData.get("chunk_data");
        
        if (chunkIndexObj == null || totalChunksObj == null || chunkData == null) {
            return ControllerUtils.error(400, "缺少必需参数: chunk_index, total_chunks, chunk_data");
        }
        
        int chunkIndex = chunkIndexObj.intValue();
        int totalChunks = totalChunksObj.intValue();
        
        ServiceResult<Boolean> result = fileService.uploadChunk(sessionId, chunkIndex, totalChunks, chunkData, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 完成分片上传
     * POST /api/files/multipart-upload/{sessionId}/complete
     */
    @PostMapping(path = "/multipart-upload/{sessionId}/complete")
    public ApiResponseWrapper completeMultipartUpload(@PathVariable("sessionId") String sessionId,
                                                      HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<FileDTO> result = fileService.completeMultipartUpload(sessionId, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    // ============ 文件查询相关 ============
    
    /**
     * 获取文件列表
     * GET /api/files?folder_id=123&page=1&size=20
     */
    @GetMapping
    public ApiResponseWrapper getFiles(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String folderIdStr = request.getParameter("folder_id");
        int page = ControllerUtils.parseIntParam(request, "page", 1);
        int size = ControllerUtils.parseIntParam(request, "size", 20);
        String sortBy = request.getParameter("sort_by");
        String sortOrder = request.getParameter("sort_order");
        
        Long folderId = folderIdStr != null ? Long.parseLong(folderIdStr) : null;
        
        ServiceResult<PageResultDTO<FileDTO>> result = fileService.getFiles(userId, folderId, page, size, sortBy, sortOrder);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取文件详情
     * GET /api/files/{id}
     */
    @GetMapping(path = "/{id}")
    public ApiResponseWrapper getFileDetails(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<FileDTO> result = fileService.getFileDetails(id, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 搜索文件
     * GET /api/files/search?keyword=test&type=image
     */
    @GetMapping(path = "/search")
    public ApiResponseWrapper searchFiles(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String keyword = request.getParameter("keyword");
        String fileType = request.getParameter("type");
        String folderIdStr = request.getParameter("folder_id");
        int page = ControllerUtils.parseIntParam(request, "page", 1);
        int size = ControllerUtils.parseIntParam(request, "size", 20);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return ControllerUtils.error(400, "搜索关键词不能为空");
        }
        
        Long folderId = folderIdStr != null ? Long.parseLong(folderIdStr) : null;
        
        ServiceResult<PageResultDTO<FileDTO>> result = fileService.searchFiles(
            userId, keyword, fileType, folderId, page, size);
        return ControllerUtils.handleServiceResult(result);
    }
    
    // ============ 文件操作相关 ============
    
    /**
     * 文件下载
     * GET /api/files/{id}/download
     */
    @GetMapping(path = "/{id}/download")
    public ApiResponseWrapper downloadFile(@PathVariable("id") Long id, HttpServletRequest request, 
                                          HttpServletResponse response) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = fileService.downloadFile(id, userId, response);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 文件预览
     * GET /api/files/{id}/preview
     */
    @GetMapping(path = "/{id}/preview")
    public ApiResponseWrapper previewFile(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = fileService.previewFile(id, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取缩略图
     * GET /api/files/{id}/thumbnail?size=small
     */
    @GetMapping(path = "/{id}/thumbnail")
    public ApiResponseWrapper getThumbnail(@PathVariable("id") Long id, 
                                          @RequestParam(defaultValue = "small") String size,
                                          HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = fileService.getThumbnail(id, size, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 重命名文件
     * PATCH /api/files/{id}
     */
    @PatchMapping(path = "/{id}")
    public ApiResponseWrapper renameFile(@PathVariable("id") Long id, 
                                        @RequestBody Map<String, Object> requestData,
                                        HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String newName = (String) requestData.get("name");
        if (newName == null || newName.trim().isEmpty()) {
            return ControllerUtils.error(400, "新文件名不能为空");
        }
        
        ServiceResult<FileDTO> result = fileService.renameFile(id, newName, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 移动文件
     * PATCH /api/files/{id}/move
     */
    @PatchMapping(path = "/{id}/move")
    public ApiResponseWrapper moveFile(@PathVariable("id") Long id,
                                      @RequestBody Map<String, Object> requestData,
                                      HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        Number targetFolderIdObj = (Number) requestData.get("target_folder_id");
        if (targetFolderIdObj == null) {
            return ControllerUtils.error(400, "目标文件夹ID不能为空");
        }
        
        Long targetFolderId = targetFolderIdObj.longValue();
        ServiceResult<FileDTO> result = fileService.moveFile(id, targetFolderId, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 复制文件
     * POST /api/files/{id}/copy
     */
    @PostMapping(path = "/{id}/copy")
    public ApiResponseWrapper copyFile(@PathVariable("id") Long id,
                                      @RequestBody Map<String, Object> requestData,
                                      HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        Number targetFolderIdObj = (Number) requestData.get("target_folder_id");
        String newName = (String) requestData.get("new_name");
        
        if (targetFolderIdObj == null) {
            return ControllerUtils.error(400, "目标文件夹ID不能为空");
        }
        
        Long targetFolderId = targetFolderIdObj.longValue();
        ServiceResult<FileDTO> result = fileService.copyFile(id, targetFolderId, newName, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 删除文件
     * DELETE /api/files/{id}
     */
    @DeleteMapping(path = "/{id}")
    public ApiResponseWrapper deleteFile(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Boolean> result = fileService.deleteFile(id, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 批量操作文件
     * POST /api/files/batch
     */
    @PostMapping(path = "/batch")
    public ApiResponseWrapper batchOperateFiles(@RequestBody Map<String, Object> requestData,
                                               HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String operation = (String) requestData.get("operation");
        @SuppressWarnings("unchecked")
        List<Long> fileIds = (List<Long>) requestData.get("file_ids");
        
        if (operation == null || fileIds == null || fileIds.isEmpty()) {
            return ControllerUtils.error(400, "缺少必需参数: operation, file_ids");
        }
        
        ServiceResult<Map<String, Object>> result = fileService.batchOperateFiles(operation, fileIds, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    // ============ 文件权限相关 ============
    
    /**
     * 获取文件权限
     * GET /api/files/{id}/permissions
     */
    @GetMapping(path = "/{id}/permissions")
    public ApiResponseWrapper getFilePermissions(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = fileService.getFilePermissions(id, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    // ============ 文件统计相关 ============
    
    /**
     * 获取文件类型统计
     * GET /api/files/statistics/types
     */
    @GetMapping(path = "/statistics/types")
    public ApiResponseWrapper getFileTypeStatistics(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = fileService.getFileTypeStatistics(userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取所有用户文件（管理员功能）
     * GET /api/files/all
     */
    @GetMapping(path = "/all")
    public ApiResponseWrapper getAllUserFiles(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        // TODO: 检查管理员权限
        
        int page = ControllerUtils.parseIntParam(request, "page", 1);
        int size = ControllerUtils.parseIntParam(request, "size", 20);
        
        ServiceResult<PageResultDTO<FileDTO>> result = fileService.getAllUserFiles(page, size);
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