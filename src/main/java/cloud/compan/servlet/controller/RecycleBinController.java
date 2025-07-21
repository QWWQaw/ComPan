package cloud.compan.servlet.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Controller;
import cloud.compan.servlet.annotations.DeleteMapping;
import cloud.compan.servlet.annotations.GetMapping;
import cloud.compan.servlet.annotations.PathVariable;
import cloud.compan.servlet.annotations.PostMapping;
import cloud.compan.servlet.annotations.RequestBody;
import cloud.compan.servlet.annotations.ResponseBody;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.service.RecycleBinService;
import cloud.compan.servlet.web.response.ApiResponseWrapper;

/**
 * 回收站控制器
 * 继承BaseController，处理回收站管理相关的HTTP请求
 * 
 * 实现回收站管理路由：
 * - GET /api/recycle-bin - 获取回收站内容
 * - POST /api/recycle-bin/restore - 恢复文件
 * - DELETE /api/recycle-bin/permanent - 永久删除
 * - DELETE /api/recycle-bin/empty - 清空回收站
 */
@Controller("/api/recycle-bin")
@ResponseBody
@Singleton
public class RecycleBinController extends BaseController {
    
    @Inject
    private RecycleBinService recycleBinService;
    
    @Inject
    private AuthService authService;
    
    /**
     * 获取回收站内容
     * GET /api/recycle-bin
     */
    @GetMapping
    public ApiResponseWrapper getRecycleBinContents(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        String resourceType = request.getParameter("resource_type");
        String sortBy = request.getParameter("sort");
        String sortOrder = request.getParameter("order");
        
        int page = parseIntParam(request, "page", 1);
        int size = parseIntParam(request, "per_page", 20);
        
        ServiceResult<PageResultDTO<Map<String, Object>>> result = recycleBinService.getRecycleBinContents(
            userId, page, size, resourceType, sortBy, sortOrder);
            
        return handleServiceResult(result);
    }
    
    /**
     * 搜索回收站内容
     * GET /api/recycle-bin/search
     */
    @GetMapping(path = "/search")
    public ApiResponseWrapper searchRecycleBin(HttpServletRequest request) {
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
        
        ServiceResult<PageResultDTO<Map<String, Object>>> result = recycleBinService.searchRecycleBin(
            userId, keyword, page, size);
            
        return handleServiceResult(result);
    }
    
    /**
     * 获取回收站统计信息
     * GET /api/recycle-bin/statistics
     */
    @GetMapping(path = "/statistics")
    public ApiResponseWrapper getRecycleBinStatistics(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = recycleBinService.getRecycleBinStatistics(userId);
        return handleServiceResult(result);
    }
    
    /**
     * 恢复文件
     * POST /api/recycle-bin/restore
     */
    @PostMapping(path = "/restore")
    public ApiResponseWrapper restoreFromRecycleBin(@RequestBody Map<String, Object> requestData,
                                                    HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) requestData.get("items");
        Number targetFolderIdObj = (Number) requestData.get("target_folder_id");
        
        if (items == null || items.isEmpty()) {
            return error(400, "要恢复的项目列表不能为空");
        }
        
        Long targetFolderId = targetFolderIdObj != null ? targetFolderIdObj.longValue() : null;
        
        ServiceResult<Map<String, Object>> result;
        if (items.size() == 1) {
            // 单个恢复
            Map<String, Object> item = items.get(0);
            String resourceType = (String) item.get("resource_type");
            Number resourceIdObj = (Number) item.get("resource_id");
            
            if (resourceType == null || resourceIdObj == null) {
                return error(400, "项目信息不完整");
            }
            
            Long resourceId = resourceIdObj.longValue();
            result = recycleBinService.restoreFromRecycleBin(resourceType, resourceId, targetFolderId, userId);
        } else {
            // 批量恢复
            result = recycleBinService.batchRestoreFromRecycleBin(items, targetFolderId, userId);
        }
        
        return handleServiceResult(result);
    }
    
    /**
     * 永久删除单个项目
     * DELETE /api/recycle-bin/{id}
     */
    @DeleteMapping(path = "/{id}")
    public ApiResponseWrapper permanentlyDeleteById(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        if (id == null) {
            return error(400, "项目ID不能为空");
        }
        
        // 从查询参数获取资源类型，默认为FILE
        String resourceType = request.getParameter("resource_type");
        if (resourceType == null || resourceType.trim().isEmpty()) {
            resourceType = "FILE"; // 默认为文件类型
        }
        
        // 直接调用Service层永久删除
        ServiceResult<Boolean> result = recycleBinService.permanentlyDelete(resourceType, id, userId);
        return handleServiceResult(result);
    }
    
    /**
     * 永久删除（批量）
     * DELETE /api/recycle-bin/permanent
     */
    @DeleteMapping(path = "/permanent")
    public ApiResponseWrapper permanentlyDelete(@RequestBody Map<String, Object> requestData,
                                               HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) requestData.get("items");
        
        if (items == null || items.isEmpty()) {
            return error(400, "要删除的项目列表不能为空");
        }
        
        ServiceResult<Map<String, Object>> result;
        if (items.size() == 1) {
            // 单个删除
            Map<String, Object> item = items.get(0);
            String resourceType = (String) item.get("resource_type");
            Number resourceIdObj = (Number) item.get("resource_id");
            
            if (resourceType == null || resourceIdObj == null) {
                return error(400, "项目信息不完整");
            }
            
            Long resourceId = resourceIdObj.longValue();
            ServiceResult<Boolean> singleResult = recycleBinService.permanentlyDelete(resourceType, resourceId, userId);
            return handleServiceResult(singleResult);
        } else {
            // 批量删除
            result = recycleBinService.batchPermanentlyDelete(items, userId);
            return handleServiceResult(result);
        }
    }
    
    /**
     * 清空回收站
     * DELETE /api/recycle-bin/empty
     */
    @DeleteMapping(path = "/empty")
    public ApiResponseWrapper emptyRecycleBin(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        String resourceType = request.getParameter("resource_type");
        
        ServiceResult<Map<String, Object>> result = recycleBinService.emptyRecycleBin(userId, resourceType);
        return handleServiceResult(result);
    }
    
    /**
     * 设置自动清理策略
     * POST /api/recycle-bin/auto-clean-policy
     */
    @PostMapping(path = "/auto-clean-policy")
    public ApiResponseWrapper setAutoCleanPolicy(@RequestBody Map<String, Object> requestData,
                                                 HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        Number retentionDaysObj = (Number) requestData.get("retention_days");
        Boolean autoCleanEnabled = (Boolean) requestData.get("auto_clean_enabled");
        
        if (retentionDaysObj == null || autoCleanEnabled == null) {
            return error(400, "缺少必需参数: retention_days, auto_clean_enabled");
        }
        
        int retentionDays = retentionDaysObj.intValue();
        
        ServiceResult<Boolean> result = recycleBinService.setAutoCleanPolicy(userId, retentionDays, autoCleanEnabled);
        return handleServiceResult(result);
    }
    
    /**
     * 获取自动清理策略
     * GET /api/recycle-bin/auto-clean-policy
     */
    @GetMapping(path = "/auto-clean-policy")
    public ApiResponseWrapper getAutoCleanPolicy(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = recycleBinService.getAutoCleanPolicy(userId);
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