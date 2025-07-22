package cloud.compan.servlet.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.GetMapping;
import cloud.compan.servlet.annotations.PostMapping;
import cloud.compan.servlet.annotations.RequestBody;
import cloud.compan.servlet.annotations.RestController;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.service.StorageService;
import cloud.compan.servlet.utils.ControllerUtils;
import cloud.compan.servlet.web.response.ApiResponseWrapper;

/**
 * 存储控制器
 * 处理存储统计和管理相关的HTTP请求
 * 
 * 实现存储管理路由：
 * - GET /api/storage/statistics - 获取存储统计
 * - GET /api/storage/quota - 获取存储配额
 * - GET /api/storage/analysis - 存储分析
 */
@RestController("/api/v1/storage")
@Singleton
public class StorageController {
    
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
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = storageService.getUserStorageStatistics(userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取存储使用明细
     * GET /api/storage/usage-details
     */
    @GetMapping(path = "/usage-details")
    public ApiResponseWrapper getStorageUsageDetails(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String period = request.getParameter("period");
        if (period == null) {
            period = "month";
        }
        
        ServiceResult<List<Map<String, Object>>> result = storageService.getStorageUsageDetails(userId, period);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取文件类型分布
     * GET /api/storage/file-type-distribution
     */
    @GetMapping(path = "/file-type-distribution")
    public ApiResponseWrapper getFileTypeDistribution(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = storageService.getFileTypeDistribution(userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取文件夹大小统计
     * GET /api/storage/folder-size
     */
    @GetMapping(path = "/folder-size")
    public ApiResponseWrapper getFolderSizeStatistics(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String folderIdStr = request.getParameter("folder_id");
        Long folderId = folderIdStr != null ? Long.parseLong(folderIdStr) : null;
        
        ServiceResult<Map<String, Object>> result = storageService.getFolderSizeStatistics(userId, folderId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取存储使用趋势
     * GET /api/storage/usage-trend
     */
    @GetMapping(path = "/usage-trend")
    public ApiResponseWrapper getStorageUsageTrend(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        int days = ControllerUtils.parseIntParam(request, "days", 30);
        
        // 简化实现，返回模拟数据
        return ControllerUtils.success("获取存储使用趋势成功", Map.of(
            "period", days + "天",
            "data", List.of(
                Map.of("date", "2024-01-01", "usage", 1024L),
                Map.of("date", "2024-01-02", "usage", 2048L)
            )
        ));
    }
    
    /**
     * 获取用户存储配额
     * GET /api/storage/quota
     */
    @GetMapping(path = "/quota")
    public ApiResponseWrapper getUserStorageQuota(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = storageService.getUserStorageQuota(userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取配额警告
     * GET /api/storage/quota-warnings
     */
    @GetMapping(path = "/quota-warnings")
    public ApiResponseWrapper getQuotaWarnings(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        // 简化实现，返回模拟数据
        return ControllerUtils.success("获取配额警告成功", List.of(
            Map.of("type", "storage_quota", "message", "存储空间使用率超过80%", "level", "warning")
        ));
    }
    
    /**
     * 查找重复文件
     * GET /api/storage/analysis/duplicates
     */
    @GetMapping(path = "/analysis/duplicates")
    public ApiResponseWrapper findDuplicateFiles(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String folderIdStr = request.getParameter("folder_id");
        Long folderId = folderIdStr != null ? Long.parseLong(folderIdStr) : null;
        
        ServiceResult<List<Map<String, Object>>> result = storageService.findDuplicateFiles(userId, folderId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 查找大文件
     * GET /api/storage/analysis/large-files
     */
    @GetMapping(path = "/analysis/large-files")
    public ApiResponseWrapper findLargeFiles(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        long threshold = ControllerUtils.parseLongParam(request, "threshold", 100 * 1024 * 1024); // 100MB
        String folderIdStr = request.getParameter("folder_id");
        Long folderId = folderIdStr != null ? Long.parseLong(folderIdStr) : null;
        
        ServiceResult<List<Map<String, Object>>> result = storageService.findLargeFiles(userId, threshold, folderId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 查找空文件夹
     * GET /api/storage/analysis/empty-folders
     */
    @GetMapping(path = "/analysis/empty-folders")
    public ApiResponseWrapper findEmptyFolders(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String folderIdStr = request.getParameter("folder_id");
        Long folderId = folderIdStr != null ? Long.parseLong(folderIdStr) : null;
        
        ServiceResult<List<Map<String, Object>>> result = storageService.findEmptyFolders(userId, folderId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 查找旧文件
     * GET /api/storage/analysis/old-files
     */
    @GetMapping(path = "/analysis/old-files")
    public ApiResponseWrapper findOldFiles(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        int days = ControllerUtils.parseIntParam(request, "days", 365);
        String folderIdStr = request.getParameter("folder_id");
        Long folderId = folderIdStr != null ? Long.parseLong(folderIdStr) : null;
        
        ServiceResult<List<Map<String, Object>>> result = storageService.findOldFiles(userId, days, folderId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取存储优化建议
     * GET /api/storage/optimization-suggestions
     */
    @GetMapping(path = "/optimization-suggestions")
    public ApiResponseWrapper getStorageOptimizationSuggestions(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<List<Map<String, Object>>> result = storageService.getStorageOptimizationSuggestions(userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 清理临时文件
     * POST /api/storage/cleanup/temporary
     */
    @PostMapping(path = "/cleanup/temporary")
    public ApiResponseWrapper cleanupTemporaryFiles(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = storageService.cleanupTemporaryFiles(userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 清理过期的回收站文件
     * POST /api/storage/cleanup/expired-recycled
     */
    @PostMapping(path = "/cleanup/expired-recycled")
    public ApiResponseWrapper cleanupExpiredRecycledFiles(@RequestBody Map<String, Object> requestData,
                                                         HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        Number daysObj = (Number) requestData.get("days");
        int days = daysObj != null ? daysObj.intValue() : 30;
        
        ServiceResult<Map<String, Object>> result = storageService.cleanupExpiredRecycledFiles(userId, days);
        return ControllerUtils.handleServiceResult(result);
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
            return ControllerUtils.error(401, "未认证");
        }
        
        @SuppressWarnings("unchecked")
        List<Long> fileIds = (List<Long>) requestData.get("file_ids");
        String compressionLevel = (String) requestData.get("compression_level");
        
        if (fileIds == null || fileIds.isEmpty()) {
            return ControllerUtils.error(400, "文件ID列表不能为空");
        }
        
        if (compressionLevel == null) {
            compressionLevel = "medium";
        }
        
        ServiceResult<Map<String, Object>> result = storageService.compressFiles(userId, fileIds, compressionLevel);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 存储碎片整理
     * POST /api/storage/defragment
     */
    @PostMapping(path = "/defragment")
    public ApiResponseWrapper defragmentStorage(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String folderIdStr = request.getParameter("folder_id");
        Long folderId = folderIdStr != null ? Long.parseLong(folderIdStr) : null;
        
        ServiceResult<Map<String, Object>> result = storageService.defragmentStorage(userId, folderId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取系统存储统计
     * GET /api/storage/system/statistics
     */
    @GetMapping(path = "/system/statistics")
    public ApiResponseWrapper getSystemStorageStatistics(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        // 检查管理员权限
        // TODO: 实现管理员权限检查
        
        ServiceResult<Map<String, Object>> result = storageService.getSystemStorageStatistics();
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取用户存储排名
     * GET /api/storage/system/user-ranking
     */
    @GetMapping(path = "/system/user-ranking")
    public ApiResponseWrapper getUserStorageRanking(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        // 检查管理员权限
        // TODO: 实现管理员权限检查
        
        int limit = ControllerUtils.parseIntParam(request, "limit", 10);
        String sortBy = request.getParameter("sort_by");
        if (sortBy == null) {
            sortBy = "storage_used";
        }
        
        ServiceResult<List<Map<String, Object>>> result = storageService.getUserStorageRanking(limit, sortBy);
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