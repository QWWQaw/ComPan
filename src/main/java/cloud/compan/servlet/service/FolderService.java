package cloud.compan.servlet.service;

import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.model.Folder;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 文件夹服务接口
 * 继承BaseService获得基础CRUD能力，同时提供文件夹管理相关的专门业务逻辑
 */
public interface FolderService  {
    
    // ============ 文件夹创建相关 ============
    
    /**
     * 创建文件夹
     * @param folderName 文件夹名称
     * @param parentFolderId 父文件夹ID（null表示根目录）
     * @param userId 用户ID
     * @return 创建结果
     */
    ServiceResult<Folder> createFolder(String folderName, Long parentFolderId, Long userId);
    
    /**
     * 批量创建文件夹
     * @param folderNames 文件夹名称列表
     * @param parentFolderId 父文件夹ID
     * @param userId 用户ID
     * @return 批量创建结果
     */
    ServiceResult<List<Folder>> createFolders(List<String> folderNames, Long parentFolderId, Long userId);
    
    // ============ 文件夹查询相关 ============
    
    /**
     * 获取用户的根目录文件夹
     * @param userId 用户ID
     * @return 根目录文件夹
     */
    ServiceResult<Folder> getRootFolder(Long userId);
    
    /**
     * 根据父文件夹ID获取子文件夹列表
     * @param parentFolderId 父文件夹ID（null表示根目录）
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortOrder 排序方向
     * @return 子文件夹列表
     */
    ServiceResult<PageResultDTO<Folder>> getSubFolders(Long parentFolderId, Long userId, 
                                                       int page, int size, 
                                                       String sortBy, String sortOrder);
    
    /**
     * 获取文件夹详情
     * @param folderId 文件夹ID
     * @param userId 用户ID
     * @return 文件夹详情
     */
    ServiceResult<Folder> getFolderDetails(Long folderId, Long userId);
    
    /**
     * 获取文件夹路径（面包屑导航）
     * @param folderId 文件夹ID
     * @param userId 用户ID
     * @return 文件夹路径列表
     */
    ServiceResult<List<Map<String, Object>>> getFolderPath(Long folderId, Long userId);
    
    /**
     * 搜索文件夹
     * @param keyword 搜索关键词
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    ServiceResult<PageResultDTO<Folder>> searchFolders(String keyword, Long userId, int page, int size);
    
    /**
     * 获取文件夹树形结构
     * @param rootFolderId 根文件夹ID（null表示用户根目录）
     * @param userId 用户ID
     * @param maxDepth 最大深度
     * @return 文件夹树
     */
    ServiceResult<Map<String, Object>> getFolderTree(Long rootFolderId, Long userId, int maxDepth);
    
    // ============ 文件夹操作相关 ============
    
    /**
     * 重命名文件夹
     * @param folderId 文件夹ID
     * @param newFolderName 新文件夹名称
     * @param userId 用户ID
     * @return 重命名结果
     */
    ServiceResult<Folder> renameFolder(Long folderId, String newFolderName, Long userId);
    
    /**
     * 移动文件夹
     * @param folderId 文件夹ID
     * @param targetParentFolderId 目标父文件夹ID
     * @param userId 用户ID
     * @return 移动结果
     */
    ServiceResult<Folder> moveFolder(Long folderId, Long targetParentFolderId, Long userId);
    
    /**
     * 复制文件夹
     * @param folderId 文件夹ID
     * @param targetParentFolderId 目标父文件夹ID
     * @param newFolderName 新文件夹名称（可选）
     * @param userId 用户ID
     * @param copyContents 是否复制内容
     * @return 复制结果
     */
    ServiceResult<Folder> copyFolder(Long folderId, Long targetParentFolderId, 
                                    String newFolderName, Long userId, boolean copyContents);
    
    /**
     * 删除文件夹（移入回收站）
     * @param folderId 文件夹ID
     * @param userId 用户ID
     * @param deleteContents 是否删除内容
     * @return 删除结果
     */
    ServiceResult<Boolean> deleteFolder(Long folderId, Long userId, boolean deleteContents);
    
    /**
     * 批量操作文件夹
     * @param folderIds 文件夹ID列表
     * @param action 操作类型（delete, move, copy）
     * @param targetParentFolderId 目标父文件夹ID
     * @param userId 用户ID
     * @return 批量操作结果
     */
    ServiceResult<Map<String, Object>> batchOperateFolders(List<Long> folderIds, String action, 
                                                          Long targetParentFolderId, Long userId);
    
    // ============ 文件夹权限相关 ============

    ServiceResult<Boolean> deleteFolder(Long folderId, Long userId);

    ServiceResult<Map<String, Object>> getFolderSize(Long folderId, Long userId);

    /**
     * 检查用户对文件夹的权限
     * @param folderId 文件夹ID
     * @param userId 用户ID
     * @param permission 权限类型（read, write, delete）
     * @return 权限检查结果
     */
    ServiceResult<Boolean> checkFolderPermission(Long folderId, Long userId, String permission);
    
    /**
     * 获取文件夹的权限列表
     * @param folderId 文件夹ID
     * @param userId 用户ID
     * @return 权限列表
     */
    ServiceResult<List<Map<String, Object>>> getFolderPermissions(Long folderId, Long userId);
    
    /**
     * 设置文件夹权限
     * @param folderId 文件夹ID
     * @param targetUserId 目标用户ID
     * @param permission 权限类型
     * @param userId 操作用户ID
     * @return 设置结果
     */
    ServiceResult<Boolean> setFolderPermission(Long folderId, Long targetUserId, 
                                              String permission, Long userId);
    
    // ============ 文件夹统计相关 ============
    
    /**
     * 获取文件夹统计信息
     * @param folderId 文件夹ID
     * @param userId 用户ID
     * @return 统计信息（文件数量、子文件夹数量、总大小等）
     */
    ServiceResult<Map<String, Object>> getFolderStatistics(Long folderId, Long userId);
    
    /**
     * 获取用户的文件夹数量统计
     * @param userId 用户ID
     * @return 文件夹数量统计
     */
    ServiceResult<Map<String, Object>> getUserFolderStatistics(Long userId);
    
    /**
     * 检查文件夹名称是否存在
     * @param folderName 文件夹名称
     * @param parentFolderId 父文件夹ID
     * @param userId 用户ID
     * @return 检查结果
     */
    ServiceResult<Boolean> checkFolderNameExists(String folderName, Long parentFolderId, Long userId);
    
    /**
     * 验证文件夹路径
     * @param folderPath 文件夹路径
     * @param userId 用户ID
     * @return 验证结果
     */
    ServiceResult<Boolean> validateFolderPath(String folderPath, Long userId);
    
    /**
     * 记录文件夹访问日志
     * @param folderId 文件夹ID
     * @param userId 用户ID
     * @param action 操作类型
     * @param request HTTP请求
     * @return 记录结果
     */
    ServiceResult<Void> logFolderAccess(Long folderId, Long userId, String action, HttpServletRequest request);
} 