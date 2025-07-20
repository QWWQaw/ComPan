package cloud.compan.servlet.service;

import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.model.Acl;
import java.util.List;
import java.util.Map;

/**
 * 权限控制服务接口
 * 管理文件和文件夹的访问控制列表（ACL）
 */
public interface AclService {
    
    // ============ 权限创建和设置 ============
    
    /**
     * 为资源设置权限
     * @param resourceType 资源类型（file, folder）
     * @param resourceId 资源ID
     * @param targetUserId 目标用户ID
     * @param permission 权限类型（read, write, delete, share）
     * @param userId 操作用户ID
     * @return 设置结果
     */
    ServiceResult<Acl> setPermission(String resourceType, Long resourceId, Long targetUserId, 
                                    String permission, Long userId);
    
    /**
     * 为用户组设置权限
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param groupId 用户组ID
     * @param permission 权限类型
     * @param userId 操作用户ID
     * @return 设置结果
     */
    ServiceResult<Acl> setGroupPermission(String resourceType, Long resourceId, Long groupId, 
                                         String permission, Long userId);
    
    /**
     * 批量设置权限
     * @param permissions 权限设置列表
     * @param userId 操作用户ID
     * @return 批量设置结果
     */
    ServiceResult<List<Acl>> setBatchPermissions(List<Map<String, Object>> permissions, Long userId);
    
    // ============ 权限查询 ============
    
    /**
     * 获取资源的权限列表
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param userId 查询用户ID
     * @return 权限列表
     */
    ServiceResult<List<Acl>> getResourcePermissions(String resourceType, Long resourceId, Long userId);
    
    /**
     * 检查用户对资源的权限
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param targetUserId 目标用户ID
     * @param permission 权限类型
     * @return 权限检查结果
     */
    ServiceResult<Boolean> checkPermission(String resourceType, Long resourceId, Long targetUserId, String permission);
    
    /**
     * 获取用户有权限的资源列表
     * @param userId 用户ID
     * @param resourceType 资源类型
     * @param permission 权限类型
     * @param page 页码
     * @param size 每页大小
     * @return 资源列表
     */
    ServiceResult<PageResultDTO<Map<String, Object>>> getUserAccessibleResources(Long userId, String resourceType, 
                                                                                 String permission, int page, int size);
    
    /**
     * 获取用户被分享的资源
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分享资源列表
     */
    ServiceResult<PageResultDTO<Map<String, Object>>> getSharedToUserResources(Long userId, int page, int size);
    
    // ============ 权限操作 ============
    
    /**
     * 移除权限
     * @param aclId 权限ID
     * @param userId 操作用户ID
     * @return 移除结果
     */
    ServiceResult<Boolean> removePermission(Long aclId, Long userId);
    
    /**
     * 批量移除权限
     * @param aclIds 权限ID列表
     * @param userId 操作用户ID
     * @return 批量移除结果
     */
    ServiceResult<Map<String, Object>> removeBatchPermissions(List<Long> aclIds, Long userId);
    
    /**
     * 更新权限
     * @param aclId 权限ID
     * @param permission 新权限类型
     * @param userId 操作用户ID
     * @return 更新结果
     */
    ServiceResult<Acl> updatePermission(Long aclId, String permission, Long userId);
    
    /**
     * 转移资源所有权
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param newOwnerId 新所有者ID
     * @param userId 当前所有者ID
     * @return 转移结果
     */
    ServiceResult<Boolean> transferOwnership(String resourceType, Long resourceId, Long newOwnerId, Long userId);
    
    // ============ 权限继承 ============
    
    /**
     * 继承父级权限
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param parentResourceId 父级资源ID
     * @param userId 操作用户ID
     * @return 继承结果
     */
    ServiceResult<List<Acl>> inheritPermissions(String resourceType, Long resourceId, Long parentResourceId, Long userId);
    
    /**
     * 获取有效权限（包含继承权限）
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param targetUserId 目标用户ID
     * @return 有效权限列表
     */
    ServiceResult<List<String>> getEffectivePermissions(String resourceType, Long resourceId, Long targetUserId);
    
    // ============ 权限统计 ============
    
    /**
     * 获取权限统计信息
     * @param userId 用户ID
     * @return 权限统计
     */
    ServiceResult<Map<String, Object>> getPermissionStatistics(Long userId);
    
    /**
     * 获取资源的权限汇总
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param userId 查询用户ID
     * @return 权限汇总
     */
    ServiceResult<Map<String, Object>> getResourcePermissionSummary(String resourceType, Long resourceId, Long userId);
} 