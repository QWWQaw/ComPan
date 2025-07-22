package cloud.compan.servlet.service;

import java.util.List;
import java.util.Map;
import cloud.compan.servlet.dto.ServiceResult;

/**
 * 存储服务接口
 * 管理用户存储空间、使用统计和配额
 */
public interface StorageService {
    
    // ============ 存储统计 ============
    
    /**
     * 获取用户存储统计信息
     * @param userId 用户ID
     * @return 存储统计信息
     */
    ServiceResult<Map<String, Object>> getUserStorageStatistics(Long userId);
    
    /**
     * 获取用户存储使用明细
     * @param userId 用户ID
     * @param period 统计周期（day, week, month, year）
     * @return 使用明细
     */
    ServiceResult<List<Map<String, Object>>> getStorageUsageDetails(Long userId, String period);
    
    /**
     * 获取文件类型分布统计
     * @param userId 用户ID
     * @return 文件类型分布
     */
    ServiceResult<Map<String, Object>> getFileTypeDistribution(Long userId);
    
    /**
     * 获取文件夹大小统计
     * @param userId 用户ID
     * @param folderId 文件夹ID（可选，默认根目录）
     * @return 文件夹大小统计
     */
    ServiceResult<Map<String, Object>> getFolderSizeStatistics(Long userId, Long folderId);
    
    /**
     * 获取存储使用趋势
     * @param userId 用户ID
     * @param days 统计天数
     * @return 使用趋势数据
     */
    ServiceResult<List<Map<String, Object>>> getStorageUsageTrend(Long userId, int days);
    
    // ============ 配额管理 ============
    
    /**
     * 获取用户存储配额信息
     * @param userId 用户ID
     * @return 配额信息
     */
    ServiceResult<Map<String, Object>> getUserStorageQuota(Long userId);
    
    /**
     * 设置用户存储配额
     * @param userId 用户ID
     * @param quotaBytes 配额大小（字节）
     * @param adminUserId 管理员用户ID
     * @return 设置结果
     */
    ServiceResult<Boolean> setUserStorageQuota(Long userId, long quotaBytes, Long adminUserId);
    
    /**
     * 检查存储空间是否足够
     * @param userId 用户ID
     * @param requiredBytes 需要的空间大小
     * @return 检查结果
     */
    ServiceResult<Boolean> checkStorageAvailable(Long userId, long requiredBytes);
    
    /**
     * 更新用户存储使用量
     * @param userId 用户ID
     * @param sizeChange 大小变化（可以是负数）
     * @return 更新结果
     */
    ServiceResult<Map<String, Object>> updateStorageUsage(Long userId, long sizeChange);
    
    /**
     * 获取配额警告信息
     * @param userId 用户ID
     * @return 警告信息
     */
    ServiceResult<Map<String, Object>> getQuotaWarnings(Long userId);
    
    // ============ 存储优化 ============
    
    /**
     * 查找重复文件
     * @param userId 用户ID
     * @param minFileSize 最小文件大小（忽略小文件）
     * @return 重复文件列表
     */
    ServiceResult<List<Map<String, Object>>> findDuplicateFiles(Long userId, long minFileSize);
    
    /**
     * 查找大文件
     * @param userId 用户ID
     * @param minSize 最小大小阈值
     * @param limit 返回数量限制
     * @return 大文件列表
     */
    ServiceResult<List<Map<String, Object>>> findLargeFiles(Long userId, long minSize, int limit);
    
    /**
     * 查找空文件夹
     * @param userId 用户ID
     * @return 空文件夹列表
     */
    ServiceResult<List<Map<String, Object>>> findEmptyFolders(Long userId);
    
    /**
     * 查找老文件（很久未访问的文件）
     * @param userId 用户ID
     * @param daysAgo 多少天前
     * @return 老文件列表
     */
    ServiceResult<List<Map<String, Object>>> findOldFiles(Long userId, int daysAgo);
    
    /**
     * 获取存储优化建议
     * @param userId 用户ID
     * @return 优化建议
     */
    ServiceResult<Map<String, Object>> getStorageOptimizationSuggestions(Long userId);
    
    // ============ 系统级统计 ============
    
    /**
     * 获取系统存储统计（管理员功能）
     * @param adminUserId 管理员用户ID
     * @return 系统存储统计
     */
    ServiceResult<Map<String, Object>> getSystemStorageStatistics(Long adminUserId);
    
    /**
     * 获取用户存储排行榜（管理员功能）
     * @param adminUserId 管理员用户ID
     * @param limit 返回数量
     * @return 用户存储排行
     */
    ServiceResult<List<Map<String, Object>>> getUserStorageRanking(Long adminUserId, int limit);
    
    /**
     * 获取存储增长预测
     * @param adminUserId 管理员用户ID
     * @param daysAhead 预测天数
     * @return 增长预测
     */
    ServiceResult<Map<String, Object>> getStorageGrowthPrediction(Long adminUserId, int daysAhead);
    
    // ============ 存储清理 ============
    
    /**
     * 清理临时文件
     * @param userId 用户ID
     * @return 清理结果
     */
    ServiceResult<Map<String, Object>> cleanupTemporaryFiles(Long userId);
    
    /**
     * 清理回收站中的过期文件
     * @param userId 用户ID
     * @param retentionDays 保留天数
     * @return 清理结果
     */
    ServiceResult<Map<String, Object>> cleanupExpiredRecycledFiles(Long userId, int retentionDays);
    
    /**
     * 压缩文件以节省空间
     * @param userId 用户ID
     * @param fileIds 要压缩的文件ID列表
     * @return 压缩结果
     */
    ServiceResult<Map<String, Object>> compressFiles(Long userId, List<Long> fileIds);
    
    /**
     * 执行存储整理
     * @param userId 用户ID
     * @return 整理结果
     */
    ServiceResult<Map<String, Object>> defragmentStorage(Long userId);
} 