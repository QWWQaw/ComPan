package cloud.compan.servlet.controller;

import cloud.compan.servlet.annotations.*;
import cloud.compan.servlet.service.ShareService;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.dto.*;
import cloud.compan.servlet.model.Share;
import cloud.compan.servlet.web.response.ApiResponseWrapper;
import com.google.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;

/**
 * 分享控制器
 * 继承BaseController，处理文件分享相关的HTTP请求
 * 
 * 职责：
 * - 处理HTTP请求参数
 * - 调用Service层接口
 * - 统一响应格式
 * 
 * 实现API 1.3文档中的分享管理路由：
 * - POST /api/shares - 创建分享
 * - GET /api/shares - 获取分享列表
 * - GET /api/shares/{id} - 获取分享详情
 * - PATCH /api/shares/{id} - 更新分享设置
 * - DELETE /api/shares/{id} - 删除分享
 * - GET /s/{shareCode} - 访问分享链接
 */
@Controller("/api/shares")
@ResponseBody
public class ShareController extends BaseController {
    
    @Inject
    private ShareService shareService;
    
    @Inject
    private AuthService authService;
    
    // ============ 分享创建相关 ============
    
    /**
     * 创建分享链接
     * POST /api/shares
     */
    @PostMapping
    public ApiResponseWrapper createShare(@RequestBody Map<String, Object> requestData,
                                         HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        String resourceType = (String) requestData.get("resource_type"); // file or folder
        Number resourceIdObj = (Number) requestData.get("resource_id");
        String shareType = (String) requestData.get("share_type"); // public, private, password
        String password = (String) requestData.get("password");
        String expireTimeStr = (String) requestData.get("expire_time");
        Number maxDownloadsObj = (Number) requestData.get("max_downloads");
        Boolean allowDownload = (Boolean) requestData.get("allow_download");
        
        if (resourceType == null || resourceIdObj == null || shareType == null) {
            return error(400, "缺少必需参数: resource_type, resource_id, share_type");
        }
        
        Long resourceId = resourceIdObj.longValue();
        LocalDateTime expireTime = parseDateTime(expireTimeStr);
        Integer maxDownloads = maxDownloadsObj != null ? maxDownloadsObj.intValue() : null;
        
        ServiceResult<Share> result;
        if ("file".equals(resourceType)) {
            result = shareService.createFileShare(resourceId, shareType, password, 
                                                 expireTime, maxDownloads, userId);
        } else if ("folder".equals(resourceType)) {
            result = shareService.createFolderShare(resourceId, shareType, password, 
                                                   expireTime, allowDownload, userId);
        } else {
            return error(400, "无效的资源类型: " + resourceType);
        }
        
        return handleServiceResult(result);
    }
    
    /**
     * 批量创建分享
     * POST /api/shares/batch
     */
    @PostMapping(path = "/batch")
    public ApiResponseWrapper createBatchShares(@RequestBody Map<String, Object> requestData,
                                               HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        @SuppressWarnings("unchecked")
        List<Number> resourceIdNumbers = (List<Number>) requestData.get("resource_ids");
        String resourceType = (String) requestData.get("resource_type");
        String shareType = (String) requestData.get("share_type");
        String expireTimeStr = (String) requestData.get("expire_time");
        
        if (resourceIdNumbers == null || resourceIdNumbers.isEmpty() || 
            resourceType == null || shareType == null) {
            return error(400, "缺少必需参数");
        }
        
        List<Long> resourceIds = resourceIdNumbers.stream()
            .map(Number::longValue)
            .toList();
            
        LocalDateTime expireTime = parseDateTime(expireTimeStr);
        
        ServiceResult<List<Share>> result = shareService.createBatchShares(
            resourceIds, resourceType, shareType, expireTime, userId);
            
        return handleServiceResult(result);
    }
    
    // ============ 分享查询相关 ============
    
    /**
     * 获取用户分享列表
     * GET /api/shares
     */
    @GetMapping
    public ApiResponseWrapper getUserShares(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        String shareType = request.getParameter("share_type");
        String status = request.getParameter("status");
        
        int page = parseIntParam(request, "page", 1);
        int size = parseIntParam(request, "per_page", 20);
        
        ServiceResult<PageResultDTO<Share>> result = shareService.getUserShares(
            userId, page, size, shareType, status);
            
        return handleServiceResult(result);
    }
    
    /**
     * 获取分享详情
     * GET /api/shares/{id}
     */
    @GetMapping(path = "/{id}")
    public ApiResponseWrapper getShareDetails(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "分享ID不能为空");
        }
        
        ServiceResult<Share> result = shareService.getShareDetails(id, userId);
        return handleServiceResult(result);
    }
    
    /**
     * 搜索分享
     * GET /api/shares/search
     */
    @GetMapping(path = "/search")
    public ApiResponseWrapper searchShares(HttpServletRequest request) {
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
        
        ServiceResult<PageResultDTO<Share>> result = shareService.searchShares(keyword, userId, page, size);
        return handleServiceResult(result);
    }
    
    /**
     * 获取分享访问记录
     * GET /api/shares/{id}/logs
     */
    @GetMapping(path = "/{id}/logs")
    public ApiResponseWrapper getShareAccessLogs(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "分享ID不能为空");
        }
        
        int page = parseIntParam(request, "page", 1);
        int size = parseIntParam(request, "per_page", 20);
        
        ServiceResult<PageResultDTO<Map<String, Object>>> result = shareService.getShareAccessLogs(
            id, userId, page, size);
            
        return handleServiceResult(result);
    }
    
    // ============ 分享操作相关 ============
    
    /**
     * 更新分享设置
     * PATCH /api/shares/{id}
     */
    @PatchMapping(path = "/{id}")
    public ApiResponseWrapper updateShareSettings(@PathVariable Long id,
                                                  @RequestBody Map<String, Object> requestData,
                                                  HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "分享ID不能为空");
        }
        
        String shareType = (String) requestData.get("share_type");
        String password = (String) requestData.get("password");
        String expireTimeStr = (String) requestData.get("expire_time");
        Number maxDownloadsObj = (Number) requestData.get("max_downloads");
        
        LocalDateTime expireTime = parseDateTime(expireTimeStr);
        Integer maxDownloads = maxDownloadsObj != null ? maxDownloadsObj.intValue() : null;
        
        ServiceResult<Share> result = shareService.updateShareSettings(
            id, shareType, password, expireTime, maxDownloads, userId);
            
        return handleServiceResult(result);
    }
    
    /**
     * 启用/禁用分享
     * PATCH /api/shares/{id}/toggle
     */
    @PatchMapping(path = "/{id}/toggle")
    public ApiResponseWrapper toggleShareStatus(@PathVariable Long id,
                                               @RequestBody Map<String, Object> requestData,
                                               HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "分享ID不能为空");
        }
        
        Boolean enabled = (Boolean) requestData.get("enabled");
        if (enabled == null) {
            return error(400, "enabled参数不能为空");
        }
        
        ServiceResult<Share> result = shareService.toggleShareStatus(id, enabled, userId);
        return handleServiceResult(result);
    }
    
    /**
     * 删除分享
     * DELETE /api/shares/{id}
     */
    @DeleteMapping(path = "/{id}")
    public ApiResponseWrapper deleteShare(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "分享ID不能为空");
        }
        
        ServiceResult<Boolean> result = shareService.deleteShare(id, userId);
        return handleServiceResult(result);
    }
    
    /**
     * 批量删除分享
     * DELETE /api/shares/batch
     */
    @DeleteMapping(path = "/batch")
    public ApiResponseWrapper deleteBatchShares(@RequestBody Map<String, Object> requestData,
                                               HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        @SuppressWarnings("unchecked")
        List<Number> shareIdNumbers = (List<Number>) requestData.get("share_ids");
        
        if (shareIdNumbers == null || shareIdNumbers.isEmpty()) {
            return error(400, "分享ID列表不能为空");
        }
        
        List<Long> shareIds = shareIdNumbers.stream()
            .map(Number::longValue)
            .toList();
            
        ServiceResult<Map<String, Object>> result = shareService.deleteBatchShares(shareIds, userId);
        return handleServiceResult(result);
    }
    
    /**
     * 重新生成分享码
     * POST /api/shares/{id}/regenerate
     */
    @PostMapping(path = "/{id}/regenerate")
    public ApiResponseWrapper regenerateShareCode(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "分享ID不能为空");
        }
        
        ServiceResult<Share> result = shareService.regenerateShareCode(id, userId);
        return handleServiceResult(result);
    }
    
    /**
     * 获取分享统计信息
     * GET /api/shares/{id}/statistics
     */
    @GetMapping(path = "/{id}/statistics")
    public ApiResponseWrapper getShareStatistics(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "分享ID不能为空");
        }
        
        ServiceResult<Map<String, Object>> result = shareService.getShareStatistics(id, userId);
        return handleServiceResult(result);
    }
    
    // ============ 辅助方法 ============
    
    /**
     * 解析日期时间字符串
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }
    
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

/**
 * 分享访问控制器
 * 处理公开分享链接的访问（不需要认证）
 */
@Controller("/s")
@ResponseBody
class ShareAccessController extends BaseController {
    
    @Inject
    private ShareService shareService;
    
    /**
     * 访问分享链接
     * GET /s/{shareCode}
     */
    @GetMapping(path = "/{shareCode}")
    public ApiResponseWrapper accessShare(@PathVariable String shareCode,
                                         HttpServletRequest request) {
        if (shareCode == null || shareCode.trim().isEmpty()) {
            return error(400, "分享码不能为空");
        }
        
        String password = request.getParameter("password");
        
        ServiceResult<Map<String, Object>> result = shareService.accessShare(shareCode, password, request);
        return handleServiceResult(result);
    }
    
    /**
     * 下载分享的文件
     * GET /s/{shareCode}/download
     */
    @GetMapping(path = "/{shareCode}/download")
    public ApiResponseWrapper downloadSharedFile(@PathVariable String shareCode,
                                                HttpServletRequest request) {
        if (shareCode == null || shareCode.trim().isEmpty()) {
            return error(400, "分享码不能为空");
        }
        
        String fileIdStr = request.getParameter("file_id");
        String password = request.getParameter("password");
        
        Long fileId = fileIdStr != null ? Long.parseLong(fileIdStr) : null;
        
        ServiceResult<Map<String, Object>> result = shareService.downloadSharedFile(
            shareCode, fileId, password, request);
            
        return handleServiceResult(result);
    }
    
    /**
     * 获取分享文件夹内容
     * GET /s/{shareCode}/folder
     */
    @GetMapping(path = "/{shareCode}/folder")
    public ApiResponseWrapper getSharedFolderContents(@PathVariable String shareCode,
                                                     HttpServletRequest request) {
        if (shareCode == null || shareCode.trim().isEmpty()) {
            return error(400, "分享码不能为空");
        }
        
        String folderIdStr = request.getParameter("folder_id");
        String password = request.getParameter("password");
        
        int page = parseIntParam(request, "page", 1);
        int size = parseIntParam(request, "per_page", 20);
        
        Long folderId = folderIdStr != null ? Long.parseLong(folderIdStr) : null;
        
        ServiceResult<PageResultDTO<Map<String, Object>>> result = shareService.getSharedFolderContents(
            shareCode, folderId, password, page, size);
            
        return handleServiceResult(result);
    }
} 