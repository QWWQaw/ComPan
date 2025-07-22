package cloud.compan.servlet.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.DeleteMapping;
import cloud.compan.servlet.annotations.GetMapping;
import cloud.compan.servlet.annotations.PathVariable;
import cloud.compan.servlet.annotations.PostMapping;
import cloud.compan.servlet.annotations.PutMapping;
import cloud.compan.servlet.annotations.RequestBody;
import cloud.compan.servlet.annotations.RestController;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.model.Acl;
import cloud.compan.servlet.service.AclService;
import cloud.compan.servlet.service.AuthService;
import cloud.compan.servlet.utils.ControllerUtils;
import cloud.compan.servlet.web.response.ApiResponseWrapper;

/**
 * 权限控制器
 * 处理权限管理相关的HTTP请求
 * 
 * 实现权限管理路由：
 * - POST /api/acl/permissions - 设置权限
 * - GET /api/acl/permissions - 获取权限列表
 * - PUT /api/acl/permissions/{id} - 更新权限
 * - DELETE /api/acl/permissions/{id} - 删除权限
 * - GET /api/acl/check - 检查权限
 */
@RestController("/api/v1/acl")
@Singleton
public class AclController {
    
    @Inject
    private AclService aclService;
    
    @Inject
    private AuthService authService;
    
    /**
     * 设置资源权限
     * POST /api/acl/permissions
     */
    @PostMapping(path = "/permissions")
    public ApiResponseWrapper setPermission(@RequestBody Map<String, Object> requestData,
                                           HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String resourceType = (String) requestData.get("resource_type");
        Number resourceIdObj = (Number) requestData.get("resource_id");
        Number targetUserIdObj = (Number) requestData.get("target_user_id");
        Number groupIdObj = (Number) requestData.get("group_id");
        String permission = (String) requestData.get("permission");
        
        if (resourceType == null || resourceIdObj == null || permission == null) {
            return ControllerUtils.error(400, "缺少必需参数: resource_type, resource_id, permission");
        }
        
        Long resourceId = resourceIdObj.longValue();
        
        ServiceResult<Acl> result;
        if (targetUserIdObj != null) {
            // 设置用户权限
            Long targetUserId = targetUserIdObj.longValue();
            result = aclService.setPermission(resourceType, resourceId, targetUserId, permission, userId);
        } else if (groupIdObj != null) {
            // 设置用户组权限
            Long groupId = groupIdObj.longValue();
            result = aclService.setGroupPermission(resourceType, resourceId, groupId, permission, userId);
        } else {
            return ControllerUtils.error(400, "必须指定target_user_id或group_id");
        }
        
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 批量设置权限
     * POST /api/acl/permissions/batch
     */
    @PostMapping(path = "/permissions/batch")
    public ApiResponseWrapper setBatchPermissions(@RequestBody Map<String, Object> requestData,
                                                 HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> permissions = (List<Map<String, Object>>) requestData.get("permissions");
        
        if (permissions == null || permissions.isEmpty()) {
            return ControllerUtils.error(400, "权限列表不能为空");
        }
        
        ServiceResult<List<Acl>> result = aclService.setBatchPermissions(permissions, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取资源权限列表
     * GET /api/acl/permissions?resource_type=file&resource_id=123
     */
    @GetMapping(path = "/permissions")
    public ApiResponseWrapper getResourcePermissions(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String resourceType = request.getParameter("resource_type");
        String resourceIdStr = request.getParameter("resource_id");
        
        if (resourceType == null || resourceIdStr == null) {
            return ControllerUtils.error(400, "缺少必需参数: resource_type, resource_id");
        }
        
        try {
            Long resourceId = Long.parseLong(resourceIdStr);
            ServiceResult<List<Acl>> result = aclService.getResourcePermissions(resourceType, resourceId, userId);
            return ControllerUtils.handleServiceResult(result);
        } catch (NumberFormatException e) {
            return ControllerUtils.error(400, "resource_id必须是有效的数字");
        }
    }
    
    /**
     * 检查权限
     * GET /api/acl/check?resource_type=file&resource_id=123&permission=read
     */
    @GetMapping(path = "/check")
    public ApiResponseWrapper checkPermission(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String resourceType = request.getParameter("resource_type");
        String resourceIdStr = request.getParameter("resource_id");
        String permission = request.getParameter("permission");
        
        if (resourceType == null || resourceIdStr == null || permission == null) {
            return ControllerUtils.error(400, "缺少必需参数: resource_type, resource_id, permission");
        }
        
        try {
            Long resourceId = Long.parseLong(resourceIdStr);
            ServiceResult<Boolean> result = aclService.checkPermission(resourceType, resourceId, userId, permission);
            return ControllerUtils.handleServiceResult(result);
        } catch (NumberFormatException e) {
            return ControllerUtils.error(400, "resource_id必须是有效的数字");
        }
    }
    
    /**
     * 获取用户可访问的资源
     * GET /api/acl/accessible-resources?resource_type=file&permission=read
     */
    @GetMapping(path = "/accessible-resources")
    public ApiResponseWrapper getUserAccessibleResources(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String resourceType = request.getParameter("resource_type");
        String permission = request.getParameter("permission");
        
        int page = ControllerUtils.parseIntParam(request, "page", 1);
        int size = ControllerUtils.parseIntParam(request, "size", 20);
        ServiceResult<PageResultDTO<Map<String, Object>>> result = aclService.getUserAccessibleResources(
            userId, resourceType, permission, page, size);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取分享给用户的资源
     * GET /api/acl/shared-to-me?resource_type=file
     */
    @GetMapping(path = "/shared-to-me")
    public ApiResponseWrapper getSharedToUserResources(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String resourceType = request.getParameter("resource_type");
        
        int page = ControllerUtils.parseIntParam(request, "page", 1);
        int size = ControllerUtils.parseIntParam(request, "size", 20);
        ServiceResult<PageResultDTO<Map<String, Object>>> result = aclService.getSharedToUserResources(
            userId, page, size);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 更新权限
     * PUT /api/acl/{id}
     */
    @PutMapping(path = "/{id}")
    public ApiResponseWrapper updatePermission(@PathVariable("id") Long id,
                                              @RequestBody Map<String, Object> requestData,
                                              HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String permission = (String) requestData.get("permission");
        if (permission == null) {
            return ControllerUtils.error(400, "缺少必需参数: permission");
        }
        
        ServiceResult<Acl> result = aclService.updatePermission(id, permission, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 移除资源权限
     * DELETE /api/acl/{resourceId}/permissions
     */
    @DeleteMapping(path = "/{resourceId}/permissions")
    public ApiResponseWrapper removeResourcePermission(@PathVariable("resourceId") Long resourceId, 
                                                      @RequestBody Map<String, Object> requestData,
                                                      HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String resourceType = (String) requestData.get("resource_type");
        Number targetUserIdObj = (Number) requestData.get("target_user_id");
        Number groupIdObj = (Number) requestData.get("group_id");
        
        if (resourceType == null) {
            return ControllerUtils.error(400, "缺少必需参数: resource_type");
        }
        
        // 简化实现，只支持通过ACL ID删除
        Number aclIdObj = (Number) requestData.get("acl_id");
        if (aclIdObj == null) {
            return ControllerUtils.error(400, "缺少必需参数: acl_id");
        }
        
        Long aclId = aclIdObj.longValue();
        ServiceResult<Boolean> result = aclService.removePermission(aclId, userId);
        
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 删除权限记录
     * DELETE /api/acl/{id}
     */
    @DeleteMapping(path = "/{id}")
    public ApiResponseWrapper removePermission(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Boolean> result = aclService.removePermission(id, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 批量删除权限
     * DELETE /api/acl/permissions/batch
     */
    @DeleteMapping(path = "/permissions/batch")
    public ApiResponseWrapper removeBatchPermissions(@RequestBody Map<String, Object> requestData,
                                                    HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        @SuppressWarnings("unchecked")
        List<Long> permissionIds = (List<Long>) requestData.get("permission_ids");
        
        if (permissionIds == null || permissionIds.isEmpty()) {
            return ControllerUtils.error(400, "权限ID列表不能为空");
        }
        
        ServiceResult<Map<String, Object>> result = aclService.removeBatchPermissions(permissionIds, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 转移资源所有权
     * POST /api/acl/transfer-ownership
     */
    @PostMapping(path = "/transfer-ownership")
    public ApiResponseWrapper transferOwnership(@RequestBody Map<String, Object> requestData,
                                               HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        String resourceType = (String) requestData.get("resource_type");
        Number resourceIdObj = (Number) requestData.get("resource_id");
        Number newOwnerIdObj = (Number) requestData.get("new_owner_id");
        
        if (resourceType == null || resourceIdObj == null || newOwnerIdObj == null) {
            return ControllerUtils.error(400, "缺少必需参数: resource_type, resource_id, new_owner_id");
        }
        
        Long resourceId = resourceIdObj.longValue();
        Long newOwnerId = newOwnerIdObj.longValue();
        
        ServiceResult<Boolean> result = aclService.transferOwnership(resourceType, resourceId, newOwnerId, userId);
        return ControllerUtils.handleServiceResult(result);
    }
    
    /**
     * 获取权限统计信息
     * GET /api/acl/statistics
     */
    @GetMapping(path = "/statistics")
    public ApiResponseWrapper getPermissionStatistics(HttpServletRequest request) {
        Long userId = getUserIdFromToken(request);
        if (userId == null) {
            return ControllerUtils.error(401, "未认证");
        }
        
        ServiceResult<Map<String, Object>> result = aclService.getPermissionStatistics(userId);
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