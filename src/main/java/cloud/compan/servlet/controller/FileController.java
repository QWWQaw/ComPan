package cloud.compan.servlet.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Controller;
import cloud.compan.servlet.annotations.DeleteMapping;
import cloud.compan.servlet.annotations.GetMapping;
import cloud.compan.servlet.annotations.PatchMapping;
import cloud.compan.servlet.annotations.PathVariable;
import cloud.compan.servlet.annotations.PostMapping;
import cloud.compan.servlet.annotations.RequestBody;
import cloud.compan.servlet.annotations.RequestParam;
import cloud.compan.servlet.annotations.ResponseBody;
import cloud.compan.servlet.dto.FileDTO;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.service.FileService;
import cloud.compan.servlet.web.response.ApiResponseWrapper;

/**
 * 文件控制器
 * 继承BaseController，处理文件管理相关的HTTP请求
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
@Controller("/api/files")
@ResponseBody
@Singleton
public class FileController extends BaseController {
    
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
            return error(401, "未认证");
        }
        
        // 提取参数
        String fileName = request.getParameter("file_name");
        String fileSizeStr = request.getParameter("file_size");
        String fileHash = request.getParameter("file_hash");
        String folderIdStr = request.getParameter("folder_id");
        String mimeType = request.getParameter("mime_type");
        
        // 基本参数验证
        if (fileName == null || fileSizeStr == null) {
            return error(400, "缺少必需参数: file_name, file_size");
        }
        
        try {
            long fileSize = Long.parseLong(fileSizeStr);
            Long folderId = folderIdStr != null ? Long.parseLong(folderIdStr) : null;
            
            // 直接调用Service层处理文件上传
            ServiceResult<FileDTO> result = fileService.uploadFile(
                fileName, fileSize, mimeType, folderId, null, fileHash, userId);
                
            return handleServiceResult(result);
            
        } catch (NumberFormatException e) {
            return error(400, "参数格式错误: " + e.getMessage());
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
            return error(401, "未认证");
        }
        
        // 提取参数
        String fileName = (String) requestData.get("file_name");
        Number fileSizeObj = (Number) requestData.get("file_size");
        String mimeType = (String) requestData.get("mime_type");
        Number folderIdObj = (Number) requestData.get("folder_id");
        Number chunkSizeObj = (Number) requestData.get("chunk_size");
        
        // 基本参数验证
        if (fileName == null || fileSizeObj == null) {
            return error(400, "缺少必需参数: file_name, file_size");
        }
        
        long fileSize = fileSizeObj.longValue();
        Long folderId = folderIdObj != null ? folderIdObj.longValue() : null;
        int chunkSize = chunkSizeObj != null ? chunkSizeObj.intValue() : 1048576; // 默认1MB
        
        // 直接调用Service层处理分片上传初始化
        ServiceResult<Map<String, Object>> result = fileService.initMultipartUpload(
            fileName, fileSize, mimeType, folderId, chunkSize, userId);
            
        return handleServiceResult(result);
    }
    
    // ============ 文件查询相关 ============
    
    /**
     * 获取文件列表
     * GET /api/files
     */
    @GetMapping
    public ApiResponseWrapper getFiles(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        // 解析查询参数
        String folderIdStr = request.getParameter("folder_id");
        String search = request.getParameter("search");
        String mimeType = request.getParameter("mime_type");
        String sortBy = request.getParameter("sort");
        String sortOrder = request.getParameter("order");
        
        int page = parseIntParam(request, "page", 1);
        int size = parseIntParam(request, "per_page", 20);
        
        Long folderId = folderIdStr != null ? Long.parseLong(folderIdStr) : null;
        
        // 直接调用Service层获取文件列表
        ServiceResult<PageResultDTO<FileDTO>> result = fileService.getFilesByFolder(
            folderId, userId, page, size, sortBy, sortOrder, search, mimeType);
            
        return handleServiceResult(result);
    }
    
    /**
     * 获取文件详情
     * GET /api/files/{id}
     */
    @GetMapping(path = "/{id}")
    public ApiResponseWrapper getFileDetails(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件ID不能为空");
        }
        
        // 直接调用Service层获取文件详情
        ServiceResult<FileDTO> result = fileService.getFileDetails(id, userId);
        return handleServiceResult(result);
    }
    
    /**
     * 搜索文件
     * GET /api/files/search
     */
    @GetMapping(path = "/search")
    public ApiResponseWrapper searchFiles(HttpServletRequest request) {
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
        
        // 直接调用Service层搜索文件
        ServiceResult<PageResultDTO<FileDTO>> result = fileService.searchFiles(keyword, userId, page, size);
        return handleServiceResult(result);
    }
    
    // ============ 文件下载相关 ============
    
    /**
     * 文件下载
     * GET /api/files/{id}/download
     */
    @GetMapping(path = "/{id}/download")
    public ApiResponseWrapper downloadFile(@PathVariable("id") Long id, HttpServletRequest request, 
                                          HttpServletResponse response) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件ID不能为空");
        }
        
        // 直接调用Service层处理文件下载（包含权限检查和日志记录）
        ServiceResult<Map<String, Object>> result = fileService.getDownloadInfo(id, userId);
        
        // 记录访问日志
        if (result.isSuccess()) {
            fileService.logFileAccess(id, userId, "download", request);
        }
        
        return handleServiceResult(result);
    }
    
    /**
     * 文件预览
     * GET /api/files/{id}/preview
     */
    @GetMapping(path = "/{id}/preview")
    public ApiResponseWrapper previewFile(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件ID不能为空");
        }
        
        // 直接调用Service层获取预览信息
        ServiceResult<Map<String, Object>> result = fileService.getPreviewInfo(id, userId);
        return handleServiceResult(result);
    }
    
    /**
     * 获取文件缩略图
     * GET /api/files/{id}/thumbnail
     */
    @GetMapping(path = "/{id}/thumbnail")
    public ApiResponseWrapper getThumbnail(@PathVariable("id") Long id, 
                                          @RequestParam(defaultValue = "small") String size,
                                          HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件ID不能为空");
        }
        
        // 直接调用Service层获取缩略图信息
        ServiceResult<Map<String, Object>> result = fileService.getThumbnailInfo(id, size, userId);
        return handleServiceResult(result);
    }
    
    // ============ 文件操作相关 ============
    
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
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件ID不能为空");
        }
        
        String newFileName = (String) requestData.get("file_name");
        if (newFileName == null || newFileName.trim().isEmpty()) {
            return error(400, "新文件名不能为空");
        }
        
        // 直接调用Service层重命名文件
        ServiceResult<FileDTO> result = fileService.renameFile(id, newFileName, userId);
        return handleServiceResult(result);
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
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件ID不能为空");
        }
        
        Number targetFolderIdObj = (Number) requestData.get("target_folder_id");
        if (targetFolderIdObj == null) {
            return error(400, "目标文件夹ID不能为空");
        }
        
        Long targetFolderId = targetFolderIdObj.longValue();
        
        // 直接调用Service层移动文件
        ServiceResult<FileDTO> result = fileService.moveFile(id, targetFolderId, userId);
        return handleServiceResult(result);
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
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件ID不能为空");
        }
        
        Number targetFolderIdObj = (Number) requestData.get("target_folder_id");
        String newName = (String) requestData.get("new_name");
        
        if (targetFolderIdObj == null) {
            return error(400, "目标文件夹ID不能为空");
        }
        
        Long targetFolderId = targetFolderIdObj.longValue();
        
        // 直接调用Service层复制文件
        ServiceResult<FileDTO> result = fileService.copyFile(id, targetFolderId, newName, userId);
        return handleServiceResult(result);
    }
    
    /**
     * 删除文件
     * DELETE /api/files/{id}
     */
    @DeleteMapping(path = "/{id}")
    public ApiResponseWrapper deleteFile(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件ID不能为空");
        }
        
        // 直接调用Service层删除文件
        ServiceResult<Boolean> result = fileService.deleteFile(id, userId);
        return handleServiceResult(result);
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
            return error(401, "未认证");
        }
        
        String action = (String) requestData.get("action");
        @SuppressWarnings("unchecked")
        List<Number> fileIdNumbers = (List<Number>) requestData.get("file_ids");
        Number targetFolderIdObj = (Number) requestData.get("target_folder_id");
        
        // 基本参数验证
        if (action == null || action.trim().isEmpty()) {
            return error(400, "操作类型不能为空");
        }
        
        if (fileIdNumbers == null || fileIdNumbers.isEmpty()) {
            return error(400, "文件ID列表不能为空");
        }
        
        List<Long> fileIds = fileIdNumbers.stream()
            .map(Number::longValue)
            .toList();
            
        Long targetFolderId = targetFolderIdObj != null ? targetFolderIdObj.longValue() : null;
        
        // 直接调用Service层批量操作文件
        ServiceResult<Map<String, Object>> result = fileService.batchOperateFiles(
            fileIds, action, targetFolderId, userId);
            
        return handleServiceResult(result);
    }
    
    // ============ 文件权限和统计相关 ============
    
    /**
     * 获取文件权限列表
     * GET /api/files/{id}/permissions
     */
    @GetMapping(path = "/{id}/permissions")
    public ApiResponseWrapper getFilePermissions(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "文件ID不能为空");
        }
        
        // 直接调用Service层获取文件权限
        ServiceResult<List<Map<String, Object>>> result = fileService.getFilePermissions(id, userId);
        return handleServiceResult(result);
    }
    
    /**
     * 获取文件类型统计
     * GET /api/files/statistics/types
     */
    @GetMapping(path = "/statistics/types")
    public ApiResponseWrapper getFileTypeStatistics(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        // 直接调用Service层获取文件类型统计
        ServiceResult<Map<String, Object>> result = fileService.getFileTypeStatistics(userId);
        return handleServiceResult(result);
    }
    
    /**
     * 获取用户所有文件
     * GET /api/files/all
     */
    @GetMapping(path = "/all")
    public ApiResponseWrapper getAllUserFiles(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        int page = parseIntParam(request, "page", 1);
        int size = parseIntParam(request, "per_page", 20);
        
        // 直接调用Service层获取用户所有文件
        ServiceResult<PageResultDTO<FileDTO>> result = fileService.getAllUserFiles(userId, page, size);
        return handleServiceResult(result);
    }
    
    // ============ 辅助方法 ============
    
    /**
     * 从请求中获取用户ID（简化版）
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