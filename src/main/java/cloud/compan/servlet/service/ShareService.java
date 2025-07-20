package cloud.compan.servlet.service;

import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.model.Share;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 分享服务接口
 * 继承BaseService获得基础CRUD能力，同时提供文件分享相关的专门业务逻辑
 */
public interface ShareService {
    
    // ============ 分享创建相关 ============
    
    /**
     * 创建文件分享链接
     * @param fileId 文件ID
     * @param shareType 分享类型（public, private, password）
     * @param password 分享密码（可选）
     * @param expireTime 过期时间（可选）
     * @param maxDownloads 最大下载次数（可选）
     * @param userId 用户ID
     * @return 分享结果
     */
    ServiceResult<Share> createFileShare(Long fileId, String shareType, String password, 
                                        LocalDateTime expireTime, Integer maxDownloads, Long userId);
    
    /**
     * 创建文件夹分享链接
     * @param folderId 文件夹ID
     * @param shareType 分享类型
     * @param password 分享密码（可选）
     * @param expireTime 过期时间（可选）
     * @param allowDownload 是否允许下载
     * @param userId 用户ID
     * @return 分享结果
     */
    ServiceResult<Share> createFolderShare(Long folderId, String shareType, String password, 
                                          LocalDateTime expireTime, Boolean allowDownload, Long userId);
    
    /**
     * 批量创建分享链接
     * @param resourceIds 资源ID列表（文件或文件夹）
     * @param resourceType 资源类型（file, folder）
     * @param shareType 分享类型
     * @param expireTime 过期时间
     * @param userId 用户ID
     * @return 批量创建结果
     */
    ServiceResult<List<Share>> createBatchShares(List<Long> resourceIds, String resourceType, 
                                                String shareType, LocalDateTime expireTime, Long userId);
    
    // ============ 分享查询相关 ============
    
    /**
     * 根据分享码获取分享信息
     * @param shareCode 分享码
     * @param password 分享密码（可选）
     * @return 分享信息
     */
    ServiceResult<Share> getShareByCode(String shareCode, String password);
    
    /**
     * 获取用户的分享列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @param shareType 分享类型过滤（可选）
     * @param status 状态过滤（可选）
     * @return 分享列表
     */
    ServiceResult<PageResultDTO<Share>> getUserShares(Long userId, int page, int size, 
                                                     String shareType, String status);
    
    /**
     * 获取分享详情
     * @param shareId 分享ID
     * @param userId 用户ID
     * @return 分享详情
     */
    ServiceResult<Share> getShareDetails(Long shareId, Long userId);
    
    /**
     * 搜索分享
     * @param keyword 搜索关键词
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    ServiceResult<PageResultDTO<Share>> searchShares(String keyword, Long userId, int page, int size);
    
    /**
     * 获取分享的访问记录
     * @param shareId 分享ID
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 访问记录
     */
    ServiceResult<PageResultDTO<Map<String, Object>>> getShareAccessLogs(Long shareId, Long userId, 
                                                                         int page, int size);
    
    // ============ 分享访问相关 ============
    
    /**
     * 访问分享链接
     * @param shareCode 分享码
     * @param password 分享密码（可选）
     * @param request HTTP请求（用于记录IP等信息）
     * @return 分享内容信息
     */
    ServiceResult<Map<String, Object>> accessShare(String shareCode, String password, HttpServletRequest request);
    
    /**
     * 下载分享的文件
     * @param shareCode 分享码
     * @param fileId 文件ID（文件夹分享时需要）
     * @param password 分享密码（可选）
     * @param request HTTP请求
     * @return 下载信息
     */
    ServiceResult<Map<String, Object>> downloadSharedFile(String shareCode, Long fileId, 
                                                          String password, HttpServletRequest request);
    
    /**
     * 预览分享的文件
     * @param shareCode 分享码
     * @param fileId 文件ID
     * @param password 分享密码（可选）
     * @param request HTTP请求
     * @return 预览信息
     */
    ServiceResult<Map<String, Object>> previewSharedFile(String shareCode, Long fileId, 
                                                         String password, HttpServletRequest request);
    
    /**
     * 获取分享文件夹的内容
     * @param shareCode 分享码
     * @param folderId 文件夹ID（可选，默认根目录）
     * @param password 分享密码（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 文件夹内容
     */
    ServiceResult<PageResultDTO<Map<String, Object>>> getSharedFolderContents(String shareCode, Long folderId, 
                                                                              String password, int page, int size);
    
    // ============ 分享操作相关 ============
    
    /**
     * 更新分享设置
     * @param shareId 分享ID
     * @param shareType 分享类型
     * @param password 分享密码
     * @param expireTime 过期时间
     * @param maxDownloads 最大下载次数
     * @param userId 用户ID
     * @return 更新结果
     */
    ServiceResult<Share> updateShareSettings(Long shareId, String shareType, String password, 
                                            LocalDateTime expireTime, Integer maxDownloads, Long userId);
    
    /**
     * 启用/禁用分享
     * @param shareId 分享ID
     * @param enabled 是否启用
     * @param userId 用户ID
     * @return 操作结果
     */
    ServiceResult<Share> toggleShareStatus(Long shareId, Boolean enabled, Long userId);
    
    /**
     * 删除分享
     * @param shareId 分享ID
     * @param userId 用户ID
     * @return 删除结果
     */
    ServiceResult<Boolean> deleteShare(Long shareId, Long userId);
    
    /**
     * 批量删除分享
     * @param shareIds 分享ID列表
     * @param userId 用户ID
     * @return 批量删除结果
     */
    ServiceResult<Map<String, Object>> deleteBatchShares(List<Long> shareIds, Long userId);
    
    /**
     * 重新生成分享码
     * @param shareId 分享ID
     * @param userId 用户ID
     * @return 新的分享信息
     */
    ServiceResult<Share> regenerateShareCode(Long shareId, Long userId);
    
    // ============ 分享权限相关 ============
    
    /**
     * 检查用户对分享的权限
     * @param shareId 分享ID
     * @param userId 用户ID
     * @param permission 权限类型（read, write, delete）
     * @return 权限检查结果
     */
    ServiceResult<Boolean> checkSharePermission(Long shareId, Long userId, String permission);
    
    /**
     * 验证分享密码
     * @param shareCode 分享码
     * @param password 密码
     * @return 验证结果
     */
    ServiceResult<Boolean> validateSharePassword(String shareCode, String password);
    
    /**
     * 检查分享是否有效
     * @param shareCode 分享码
     * @return 检查结果
     */
    ServiceResult<Boolean> isShareValid(String shareCode);
    
    // ============ 分享统计相关 ============
    
    /**
     * 获取分享统计信息
     * @param shareId 分享ID
     * @param userId 用户ID
     * @return 统计信息（访问次数、下载次数等）
     */
    ServiceResult<Map<String, Object>> getShareStatistics(Long shareId, Long userId);
    
    /**
     * 获取用户的分享统计
     * @param userId 用户ID
     * @return 用户分享统计
     */
    ServiceResult<Map<String, Object>> getUserShareStatistics(Long userId);
    
    /**
     * 增加分享访问次数
     * @param shareId 分享ID
     * @param request HTTP请求
     * @return 更新结果
     */
    ServiceResult<Void> incrementShareVisits(Long shareId, HttpServletRequest request);
    
    /**
     * 增加分享下载次数
     * @param shareId 分享ID
     * @param fileId 文件ID（可选）
     * @param request HTTP请求
     * @return 更新结果
     */
    ServiceResult<Void> incrementShareDownloads(Long shareId, Long fileId, HttpServletRequest request);
    
    // ============ 分享安全相关 ============
    
    /**
     * 检查分享访问频率限制
     * @param shareCode 分享码
     * @param clientIP 客户端IP
     * @return 检查结果
     */
    ServiceResult<Boolean> checkShareAccessLimit(String shareCode, String clientIP);
    
    /**
     * 记录分享访问日志
     * @param shareId 分享ID
     * @param action 操作类型
     * @param request HTTP请求
     * @return 记录结果
     */
    ServiceResult<Void> logShareAccess(Long shareId, String action, HttpServletRequest request);
    
    /**
     * 生成分享码
     * @return 分享码
     */
    String generateShareCode();
    
    /**
     * 清理过期分享
     * @return 清理结果
     */
    ServiceResult<Map<String, Object>> cleanupExpiredShares();
} 