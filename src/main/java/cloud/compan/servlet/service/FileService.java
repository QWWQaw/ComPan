package cloud.compan.servlet.service;

import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.dto.FileDTO;
import cloud.compan.servlet.model.File;
import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 文件服务接口
 * 继承BaseService获得基础CRUD能力，同时提供文件管理相关的专门业务逻辑
 */
public interface FileService {
    
    // ============ 文件上传相关 ============
    
    /**
     * 文件上传
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param mimeType 文件类型
     * @param folderId 父文件夹ID（可选，默认根目录）
     * @param fileStream 文件流
     * @param fileHash 文件哈希值（用于秒传检测）
     * @param userId 用户ID
     * @return 上传结果，包含文件信息
     */
    ServiceResult<FileDTO> uploadFile(String fileName, long fileSize, String mimeType, 
                                     Long folderId, InputStream fileStream, 
                                     String fileHash, Long userId);
    
    /**
     * 检查文件是否已存在（秒传检测）
     * @param fileHash 文件哈希值
     * @param userId 用户ID
     * @return 检查结果，如果存在返回文件信息
     */
    ServiceResult<FileDTO> checkFileExists(String fileHash, Long userId);
    
    /**
     * 分片上传初始化
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param mimeType 文件类型
     * @param folderId 父文件夹ID
     * @param chunkSize 分片大小
     * @param userId 用户ID
     * @return 初始化结果，包含上传ID
     */
    ServiceResult<Map<String, Object>> initMultipartUpload(String fileName, long fileSize, 
                                                          String mimeType, Long folderId, 
                                                          int chunkSize, Long userId);
    
    // ============ 文件查询相关 ============
    
    /**
     * 根据文件夹ID获取文件列表
     * @param folderId 文件夹ID（null表示根目录）
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortOrder 排序方向
     * @param search 搜索关键词
     * @param mimeType 文件类型过滤
     * @return 文件列表
     */
    ServiceResult<PageResultDTO<FileDTO>> getFilesByFolder(Long folderId, Long userId, 
                                                          int page, int size, 
                                                          String sortBy, String sortOrder, 
                                                          String search, String mimeType);
    
    /**
     * 获取文件详情
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 文件详情
     */
    ServiceResult<FileDTO> getFileDetails(Long fileId, Long userId);
    
    /**
     * 搜索文件
     * @param keyword 搜索关键词
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    ServiceResult<PageResultDTO<FileDTO>> searchFiles(String keyword, Long userId, 
                                                     int page, int size);
    
    /**
     * 获取用户的所有文件（用于管理）
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 文件列表
     */
    ServiceResult<PageResultDTO<FileDTO>> getAllUserFiles(Long userId, int page, int size);
    
    // ============ 文件下载相关 ============
    
    /**
     * 获取文件下载信息
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 下载信息（包含文件路径、大小等）
     */
    ServiceResult<Map<String, Object>> getDownloadInfo(Long fileId, Long userId);
    
    /**
     * 获取文件预览信息
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 预览信息
     */
    ServiceResult<Map<String, Object>> getPreviewInfo(Long fileId, Long userId);
    
    /**
     * 获取文件缩略图信息
     * @param fileId 文件ID
     * @param size 缩略图尺寸（small, medium, large）
     * @param userId 用户ID
     * @return 缩略图信息
     */
    ServiceResult<Map<String, Object>> getThumbnailInfo(Long fileId, String size, Long userId);
    
    // ============ 文件操作相关 ============
    
    /**
     * 重命名文件
     * @param fileId 文件ID
     * @param newFileName 新文件名
     * @param userId 用户ID
     * @return 重命名结果
     */
    ServiceResult<FileDTO> renameFile(Long fileId, String newFileName, Long userId);
    
    /**
     * 移动文件到指定文件夹
     * @param fileId 文件ID
     * @param targetFolderId 目标文件夹ID
     * @param userId 用户ID
     * @return 移动结果
     */
    ServiceResult<FileDTO> moveFile(Long fileId, Long targetFolderId, Long userId);
    
    /**
     * 复制文件到指定文件夹
     * @param fileId 文件ID
     * @param targetFolderId 目标文件夹ID
     * @param newFileName 新文件名（可选）
     * @param userId 用户ID
     * @return 复制结果
     */
    ServiceResult<FileDTO> copyFile(Long fileId, Long targetFolderId, String newFileName, Long userId);
    
    /**
     * 删除文件（移入回收站）
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 删除结果
     */
    ServiceResult<Boolean> deleteFile(Long fileId, Long userId);
    
    /**
     * 批量操作文件
     * @param fileIds 文件ID列表
     * @param action 操作类型（delete, move, copy）
     * @param targetFolderId 目标文件夹ID（move和copy时需要）
     * @param userId 用户ID
     * @return 批量操作结果
     */
    ServiceResult<Map<String, Object>> batchOperateFiles(List<Long> fileIds, String action, 
                                                        Long targetFolderId, Long userId);
    
    // ============ 文件权限相关 ============
    
    /**
     * 检查用户对文件的权限
     * @param fileId 文件ID
     * @param userId 用户ID
     * @param permission 权限类型（read, write, delete）
     * @return 权限检查结果
     */
    ServiceResult<Boolean> checkFilePermission(Long fileId, Long userId, String permission);
    
    /**
     * 获取文件的权限列表
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 权限列表
     */
    ServiceResult<List<Map<String, Object>>> getFilePermissions(Long fileId, Long userId);
    
    // ============ 存储管理相关 ============
    
    /**
     * 检查用户存储容量
     * @param userId 用户ID
     * @param additionalSize 要添加的文件大小
     * @return 容量检查结果
     */
    ServiceResult<Boolean> checkStorageCapacity(Long userId, long additionalSize);
    
    /**
     * 更新用户存储使用量
     * @param userId 用户ID
     * @param sizeChange 大小变化（可以是负数）
     * @return 更新结果
     */
    ServiceResult<Boolean> updateStorageUsage(Long userId, long sizeChange);
    
    /**
     * 获取文件类型统计
     * @param userId 用户ID
     * @return 文件类型统计信息
     */
    ServiceResult<Map<String, Object>> getFileTypeStatistics(Long userId);
    
    // ============ 文件安全相关 ============
    
    /**
     * 病毒扫描文件
     * @param fileId 文件ID
     * @return 扫描结果
     */
    ServiceResult<Map<String, Object>> scanFileForVirus(Long fileId);
    
    /**
     * 验证文件完整性
     * @param fileId 文件ID
     * @return 验证结果
     */
    ServiceResult<Boolean> verifyFileIntegrity(Long fileId);
    
    /**
     * 记录文件访问日志
     * @param fileId 文件ID
     * @param userId 用户ID
     * @param action 操作类型
     * @param request HTTP请求（用于获取IP等信息）
     * @return 记录结果
     */
    ServiceResult<Void> logFileAccess(Long fileId, Long userId, String action, HttpServletRequest request);
} 