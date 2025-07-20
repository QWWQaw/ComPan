package cloud.compan.servlet.controller;

import cloud.compan.servlet.annotations.*;
import cloud.compan.servlet.service.StorageService;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.dto.*;
import cloud.compan.servlet.web.response.ApiResponseWrapper;
import com.google.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.List;

/**
 * 存储控制器
 * 继承BaseController，处理存储统计和管理相关的HTTP请求
 * 
 * 实现存储管理路由：
 * - GET /api/storage/statistics - 获取存储统计
 * - GET /api/storage/quota - 获取存储配额
 * - GET /api/storage/analysis - 存储分析
 */
@Controller("/api/storage")
@ResponseBody
public class StorageController extends BaseController {
    
    @Inject
    private StorageService storageService;
    
    @Inject
    private AuthService authService;
    
    /**
     * 获取用户存储统计
     * GET /api/storage/statistics
     */
    @GetMapping(path = "/statistics")
    public ApiResponseWrapper getUserStorageStatistics(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = storageService.getUserStorageStatistics(userId);
        return handleServiceResult(result);
    }
    
    /**
     * 获取存储使用明细
     * GET /api/storage/usage-details
     */
    @GetMapping(path = "/usage-details")
    public ApiResponseWrapper getStorageUsageDetails(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        String period = request.getParameter("period");
        if (period == null) {
            period = "month";
        }
        
        ServiceResult<List<Map<String, Object>>> result = storageService.getStorageUsageDetails(userId, period);
        return handleServiceResult(result);
    }
    
    /**
     * 获取文件类型分布
     * GET /api/storage/file-type-distribution
     */
    @GetMapping(path = "/file-type-distribution")
    public ApiResponseWrapper getFileTypeDistribution(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = storageService.getFileTypeDistribution(userId);
        return handleServiceResult(result);
    }
    
    /**
     * 获取文件夹大小统计
     * GET /api/storage/folder-size
     */
    @GetMapping(path = "/folder-size")
    public ApiResponseWrapper getFolderSizeStatistics(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        String folderIdStr = request.getParameter("folder_id");
        Long folderId = folderIdStr != null ? Long.parseLong(folderIdStr) : null;
        
        ServiceResult<Map<String, Object>> result = storageService.getFolderSizeStatistics(userId, folderId);
        return handleServiceResult(result);
    }
    
    /**
     * 获取存储使用趋势
     * GET /api/storage/usage-trend
     */
    @GetMapping(path = "/usage-trend")
    public ApiResponseWrapper getStorageUsageTrend(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        int days = parseIntParam(request, "days", 30);
        
        ServiceResult<List<Map<String, Object>>> result = storageService.getStorageUsageTrend(userId, days);
        return handleServiceResult(result);
    }
    
    /**
     * 获取存储配额信息
     * GET /api/storage/quota
     */
    @GetMapping(path = "/quota")
    public ApiResponseWrapper getUserStorageQuota(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = storageService.getUserStorageQuota(userId);
        return handleServiceResult(result);
    }
    
    /**
     * 获取配额警告信息
     * GET /api/storage/quota-warnings
     */
    @GetMapping(path = "/quota-warnings")
    public ApiResponseWrapper getQuotaWarnings(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = storageService.getQuotaWarnings(userId);
        return handleServiceResult(result);
    }
    
    /**
     * 查找重复文件
     * GET /api/storage/analysis/duplicates
     */
    @GetMapping(path = "/analysis/duplicates")
    public ApiResponseWrapper findDuplicateFiles(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        long minFileSize = parseIntParam(request, "min_file_size", 1024); // 默认1KB
        
        ServiceResult<List<Map<String, Object>>> result = storageService.findDuplicateFiles(userId, minFileSize);
        return handleServiceResult(result);
    }
    
    /**
     * 查找大文件
     * GET /api/storage/analysis/large-files
     */
    @GetMapping(path = "/analysis/large-files")
    public ApiResponseWrapper findLargeFiles(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        long minSize = parseIntParam(request, "min_size", 10485760); // 默认10MB
        int limit = parseIntParam(request, "limit", 50);
        
        ServiceResult<List<Map<String, Object>>> result = storageService.findLargeFiles(userId, minSize, limit);
        return handleServiceResult(result);
    }
    
    /**
     * 查找空文件夹
     * GET /api/storage/analysis/empty-folders
     */
    @GetMapping(path = "/analysis/empty-folders")
    public ApiResponseWrapper findEmptyFolders(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        ServiceResult<List<Map<String, Object>>> result = storageService.findEmptyFolders(userId);
        return handleServiceResult(result);
    }
    
    /**
     * 查找老文件
     * GET /api/storage/analysis/old-files
     */
    @GetMapping(path = "/analysis/old-files")
    public ApiResponseWrapper findOldFiles(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        int daysAgo = parseIntParam(request, "days_ago", 365); // 默认一年前
        
        ServiceResult<List<Map<String, Object>>> result = storageService.findOldFiles(userId, daysAgo);
        return handleServiceResult(result);
    }
    
    /**
     * 获取存储优化建议
     * GET /api/storage/optimization-suggestions
     */
    @GetMapping(path = "/optimization-suggestions")
    public ApiResponseWrapper getStorageOptimizationSuggestions(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = storageService.getStorageOptimizationSuggestions(userId);
        return handleServiceResult(result);
    }
    
    /**
     * 清理临时文件
     * POST /api/storage/cleanup/temporary
     */
    @PostMapping(path = "/cleanup/temporary")
    public ApiResponseWrapper cleanupTemporaryFiles(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = storageService.cleanupTemporaryFiles(userId);
        return handleServiceResult(result);
    }
    
    /**
     * 清理过期回收站文件
     * POST /api/storage/cleanup/expired-recycled
     */
    @PostMapping(path = "/cleanup/expired-recycled")
    public ApiResponseWrapper cleanupExpiredRecycledFiles(@RequestBody Map<String, Object> requestData,
                                                         HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        Number retentionDaysObj = (Number) requestData.get("retention_days");
        int retentionDays = retentionDaysObj != null ? retentionDaysObj.intValue() : 30;
        
        ServiceResult<Map<String, Object>> result = storageService.cleanupExpiredRecycledFiles(userId, retentionDays);
        return handleServiceResult(result);
    }
    
    /**
     * 压缩文件
     * POST /api/storage/compress
     */
    @PostMapping(path = "/compress")
    public ApiResponseWrapper compressFiles(@RequestBody Map<String, Object> requestData,
                                           HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        @SuppressWarnings("unchecked")
        List<Number> fileIdNumbers = (List<Number>) requestData.get("file_ids");
        
        if (fileIdNumbers == null || fileIdNumbers.isEmpty()) {
            return error(400, "文件ID列表不能为空");
        }
        
        List<Long> fileIds = fileIdNumbers.stream()
            .map(Number::longValue)
            .toList();
            
        ServiceResult<Map<String, Object>> result = storageService.compressFiles(userId, fileIds);
        return handleServiceResult(result);
    }
    
    /**
     * 执行存储整理
     * POST /api/storage/defragment
     */
    @PostMapping(path = "/defragment")
    public ApiResponseWrapper defragmentStorage(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = storageService.defragmentStorage(userId);
        return handleServiceResult(result);
    }
    
    // ============ 管理员功能 ============
    
    /**
     * 获取系统存储统计（管理员）
     * GET /api/storage/system/statistics
     */
    @GetMapping(path = "/system/statistics")
    public ApiResponseWrapper getSystemStorageStatistics(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        // TODO: 检查管理员权限
        
        ServiceResult<Map<String, Object>> result = storageService.getSystemStorageStatistics(userId);
        return handleServiceResult(result);
    }
    
    /**
     * 获取用户存储排行榜（管理员）
     * GET /api/storage/system/user-ranking
     */
    @GetMapping(path = "/system/user-ranking")
    public ApiResponseWrapper getUserStorageRanking(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        // TODO: 检查管理员权限
        
        int limit = parseIntParam(request, "limit", 20);
        
        ServiceResult<List<Map<String, Object>>> result = storageService.getUserStorageRanking(userId, limit);
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