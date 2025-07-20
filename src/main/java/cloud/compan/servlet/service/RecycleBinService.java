package cloud.compan.servlet.service;

import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.PageResultDTO;
import java.util.List;
import java.util.Map;

/**
 * 回收站服务接口
 * 管理已删除文件和文件夹的回收站功能
 */
public interface RecycleBinService {
    
    // ============ 回收站查询 ============
    
    /**
     * 获取用户回收站内容
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @param resourceType 资源类型过滤（file, folder, all）
     * @param sortBy 排序字段
     * @param sortOrder 排序方向
     * @return 回收站内容列表
     */
    ServiceResult<PageResultDTO<Map<String, Object>>> getRecycleBinContents(Long userId, int page, int size, 
                                                                            String resourceType, String sortBy, String sortOrder);
    
    /**
     * 搜索回收站内容
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    ServiceResult<PageResultDTO<Map<String, Object>>> searchRecycleBin(Long userId, String keyword, int page, int size);
    
    /**
     * 获取回收站统计信息
     * @param userId 用户ID
     * @return 统计信息（总数量、总大小、各类型数量等）
     */
    ServiceResult<Map<String, Object>> getRecycleBinStatistics(Long userId);
    
    // ============ 回收站操作 ============
    
    /**
     * 移动文件到回收站
     * @param resourceType 资源类型（file, folder）
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 移动结果
     */
    ServiceResult<Boolean> moveToRecycleBin(String resourceType, Long resourceId, Long userId);
    
    /**
     * 批量移动到回收站
     * @param items 要删除的项目列表（包含resourceType和resourceId）
     * @param userId 用户ID
     * @return 批量移动结果
     */
    ServiceResult<Map<String, Object>> batchMoveToRecycleBin(List<Map<String, Object>> items, Long userId);
    
    /**
     * 从回收站恢复
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param targetFolderId 恢复到的目标文件夹ID（可选，默认原位置）
     * @param userId 用户ID
     * @return 恢复结果
     */
    ServiceResult<Map<String, Object>> restoreFromRecycleBin(String resourceType, Long resourceId, 
                                                            Long targetFolderId, Long userId);
    
    /**
     * 批量恢复
     * @param items 要恢复的项目列表
     * @param targetFolderId 目标文件夹ID（可选）
     * @param userId 用户ID
     * @return 批量恢复结果
     */
    ServiceResult<Map<String, Object>> batchRestoreFromRecycleBin(List<Map<String, Object>> items, 
                                                                 Long targetFolderId, Long userId);
    
    /**
     * 永久删除
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 删除结果
     */
    ServiceResult<Boolean> permanentlyDelete(String resourceType, Long resourceId, Long userId);
    
    /**
     * 批量永久删除
     * @param items 要永久删除的项目列表
     * @param userId 用户ID
     * @return 批量删除结果
     */
    ServiceResult<Map<String, Object>> batchPermanentlyDelete(List<Map<String, Object>> items, Long userId);
    
    /**
     * 清空回收站
     * @param userId 用户ID
     * @param resourceType 资源类型（可选，为空表示清空所有）
     * @return 清空结果
     */
    ServiceResult<Map<String, Object>> emptyRecycleBin(Long userId, String resourceType);
    
    // ============ 回收站管理 ============
    
    /**
     * 检查资源是否在回收站
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 检查结果
     */
    ServiceResult<Boolean> isInRecycleBin(String resourceType, Long resourceId, Long userId);
    
    /**
     * 获取回收站项目详情
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 项目详情
     */
    ServiceResult<Map<String, Object>> getRecycleBinItemDetails(String resourceType, Long resourceId, Long userId);
    
    /**
     * 设置自动清理策略
     * @param userId 用户ID
     * @param retentionDays 保留天数
     * @param autoCleanEnabled 是否启用自动清理
     * @return 设置结果
     */
    ServiceResult<Boolean> setAutoCleanPolicy(Long userId, int retentionDays, boolean autoCleanEnabled);
    
    /**
     * 获取自动清理策略
     * @param userId 用户ID
     * @return 清理策略
     */
    ServiceResult<Map<String, Object>> getAutoCleanPolicy(Long userId);
    
    /**
     * 执行自动清理（清理超过保留期的项目）
     * @param userId 用户ID
     * @return 清理结果
     */
    ServiceResult<Map<String, Object>> executeAutoClean(Long userId);
    
    /**
     * 获取即将自动清理的项目
     * @param userId 用户ID
     * @param days 多少天内将被清理
     * @return 即将清理的项目列表
     */
    ServiceResult<List<Map<String, Object>>> getItemsToBeAutoCleared(Long userId, int days);
} 