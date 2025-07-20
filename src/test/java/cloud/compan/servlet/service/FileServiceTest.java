package cloud.compan.servlet.service;

import cloud.compan.servlet.service.impl.FileServiceImpl;
import cloud.compan.servlet.dto.*;
import cloud.compan.servlet.model.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileService 单元测试
 */
@DisplayName("文件服务测试")
public class FileServiceTest {

    @InjectMocks
    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("文件上传 - 成功案例")
    void testUploadFileSuccess() {
        // Given
        Long userId = 1L;
        String fileName = "test.txt";
        String contentType = "text/plain";
        Long fileSize = 1024L;
        byte[] content = "Hello World".getBytes();

        // When
        ServiceResult<File> result = fileService.uploadFile(userId, fileName, contentType, fileSize, content);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("文件上传成功", result.getMessage());

        File file = result.getData();
        assertNotNull(file);
        assertEquals(userId, file.getUploaderId());
        assertEquals(fileName, file.getFileName());
        assertEquals(contentType, file.getMimeType());
        assertEquals("ACTIVE", file.getStatus());
        assertNotNull(file.getObjectHash());
        assertNotNull(file.getCreatedAt());
    }

    @Test
    @DisplayName("获取文件信息 - 成功案例")
    void testGetFileByIdSuccess() {
        // Given
        Long fileId = 1L;

        // When
        ServiceResult<File> result = fileService.getFileById(fileId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("获取文件信息成功", result.getMessage());

        File file = result.getData();
        assertNotNull(file);
        assertEquals(fileId, file.getFileId());
        assertEquals("test-file.txt", file.getFileName());
        assertEquals("ACTIVE", file.getStatus());
    }

    @Test
    @DisplayName("获取文件信息 - 无效文件ID")
    void testGetFileByIdInvalidId() {
        // Given
        Long fileId = -1L;

        // When
        ServiceResult<File> result = fileService.getFileById(fileId);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("文件ID无效", result.getMessage());
    }

    @Test
    @DisplayName("获取用户文件列表 - 成功案例")
    void testGetUserFilesSuccess() {
        // Given
        Long userId = 1L;
        SearchCriteria criteria = SearchCriteria.builder()
                .page(1)
                .size(10)
                .sortBy("fileName")
                .sortDirection("ASC")
                .build();

        // When
        ServiceResult<PageResultDTO<File>> result = fileService.getUserFiles(userId, criteria);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("获取用户文件列表成功", result.getMessage());

        PageResultDTO<File> pageResult = result.getData();
        assertNotNull(pageResult);
        assertNotNull(pageResult.getContent());
        assertEquals(10, pageResult.getContent().size());
        assertEquals(50L, pageResult.getTotal());
        assertEquals(1, pageResult.getPage());
    }

    @Test
    @DisplayName("删除文件 - 成功案例")
    void testDeleteFileSuccess() {
        // Given
        Long fileId = 1L;
        Long userId = 1L;

        // When
        ServiceResult<Boolean> result = fileService.deleteFile(fileId, userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("文件删除成功", result.getMessage());
        assertTrue(result.getData());
    }

    @Test
    @DisplayName("删除文件 - 无效文件ID")
    void testDeleteFileInvalidId() {
        // Given
        Long fileId = -1L;
        Long userId = 1L;

        // When
        ServiceResult<Boolean> result = fileService.deleteFile(fileId, userId);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("文件ID无效", result.getMessage());
    }

    @Test
    @DisplayName("下载文件 - 成功案例")
    void testDownloadFileSuccess() {
        // Given
        Long fileId = 1L;
        Long userId = 1L;

        // When
        ServiceResult<byte[]> result = fileService.downloadFile(fileId, userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("文件下载成功", result.getMessage());

        byte[] content = result.getData();
        assertNotNull(content);
        assertTrue(content.length > 0);
    }

    @Test
    @DisplayName("重命名文件 - 成功案例")
    void testRenameFileSuccess() {
        // Given
        Long fileId = 1L;
        String newName = "renamed-file.txt";
        Long userId = 1L;

        // When
        ServiceResult<FileDTO> result = fileService.renameFile(fileId, newName, userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("文件重命名成功", result.getMessage());

        FileDTO fileDTO = result.getData();
        assertNotNull(fileDTO);
        assertEquals(fileId, fileDTO.getFileId());
        assertEquals(newName, fileDTO.getFileName());
        assertNotNull(fileDTO.getUpdatedAt());
    }

    @Test
    @DisplayName("获取存储统计 - 成功案例")
    void testGetStorageStatsSuccess() {
        // Given
        Long userId = 1L;

        // When
        ServiceResult<StorageStatsDTO> result = fileService.getStorageStats(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("获取存储统计成功", result.getMessage());

        StorageStatsDTO stats = result.getData();
        assertNotNull(stats);
        assertEquals(35, stats.getFileCount());
        assertEquals(8, stats.getFolderCount());
    }

    @Test
    @DisplayName("批量操作文件 - 成功案例")
    void testBatchOperateFilesSuccess() {
        // Given
        java.util.List<Long> fileIds = java.util.Arrays.asList(1L, 2L, 3L);
        String action = "move";
        Long targetFolderId = 5L;
        Long userId = 1L;

        // When
        ServiceResult<Map<String, Object>> result = fileService.batchOperateFiles(fileIds, action, targetFolderId, userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("批量操作执行成功", result.getMessage());

        Map<String, Object> resultData = result.getData();
        assertNotNull(resultData);
        assertEquals(3, resultData.get("processed"));
        assertEquals(action, resultData.get("action"));
    }

    @Test
    @DisplayName("检查文件权限 - 有权限")
    void testCheckFilePermissionSuccess() {
        // Given
        Long fileId = 1L;
        Long userId = 1L;
        String permission = "read";

        // When
        ServiceResult<Boolean> result = fileService.checkFilePermission(fileId, userId, permission);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("权限检查通过", result.getMessage());
        assertTrue(result.getData());
    }

    @Test
    @DisplayName("获取文件权限列表 - 成功案例")
    void testGetFilePermissionsSuccess() {
        // Given
        Long fileId = 1L;
        Long userId = 1L;

        // When
        ServiceResult<java.util.List<Map<String, Object>>> result = fileService.getFilePermissions(fileId, userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("获取权限列表成功", result.getMessage());

        java.util.List<Map<String, Object>> permissions = result.getData();
        assertNotNull(permissions);
    }

    @Test
    @DisplayName("检查存储容量 - 容量足够")
    void testCheckStorageCapacitySuccess() {
        // Given
        Long userId = 1L;
        long additionalSize = 1024000L; // 1MB

        // When
        ServiceResult<Boolean> result = fileService.checkStorageCapacity(userId, additionalSize);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("存储容量检查通过", result.getMessage());
        assertTrue(result.getData());
    }

    @Test
    @DisplayName("更新存储使用量 - 成功案例")
    void testUpdateStorageUsageSuccess() {
        // Given
        Long userId = 1L;
        long sizeChange = 1024L;

        // When
        ServiceResult<Boolean> result = fileService.updateStorageUsage(userId, sizeChange);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("存储使用量更新成功", result.getMessage());
        assertTrue(result.getData());
    }

    @Test
    @DisplayName("获取文件类型统计 - 成功案例")
    void testGetFileTypeStatisticsSuccess() {
        // Given
        Long userId = 1L;

        // When
        ServiceResult<Map<String, Object>> result = fileService.getFileTypeStatistics(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("获取文件类型统计成功", result.getMessage());

        Map<String, Object> stats = result.getData();
        assertNotNull(stats);
        assertEquals(15, stats.get("image"));
        assertEquals(10, stats.get("document"));
        assertEquals(3, stats.get("video"));
    }

    @Test
    @DisplayName("病毒扫描 - 文件安全")
    void testScanFileForVirusSuccess() {
        // Given
        Long fileId = 1L;

        // When
        ServiceResult<Map<String, Object>> result = fileService.scanFileForVirus(fileId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("病毒扫描完成", result.getMessage());

        Map<String, Object> scanResult = result.getData();
        assertNotNull(scanResult);
        assertTrue((Boolean) scanResult.get("safe"));
        assertNotNull(scanResult.get("scanTime"));
    }

    @Test
    @DisplayName("验证文件完整性 - 成功案例")
    void testVerifyFileIntegritySuccess() {
        // Given
        Long fileId = 1L;

        // When
        ServiceResult<Boolean> result = fileService.verifyFileIntegrity(fileId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("文件完整性验证通过", result.getMessage());
        assertTrue(result.getData());
    }

    @Test
    @DisplayName("获取下载信息 - 成功案例")
    void testGetDownloadInfoSuccess() {
        // Given
        Long fileId = 1L;
        Long userId = 1L;

        // When
        ServiceResult<Map<String, Object>> result = fileService.getDownloadInfo(fileId, userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("获取下载信息成功", result.getMessage());

        Map<String, Object> info = result.getData();
        assertNotNull(info);
        assertEquals("/download/" + fileId, info.get("downloadUrl"));
    }

    @Test
    @DisplayName("获取预览信息 - 成功案例")
    void testGetPreviewInfoSuccess() {
        // Given
        Long fileId = 1L;
        Long userId = 1L;

        // When
        ServiceResult<Map<String, Object>> result = fileService.getPreviewInfo(fileId, userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("获取预览信息成功", result.getMessage());

        Map<String, Object> info = result.getData();
        assertNotNull(info);
        assertEquals("/preview/" + fileId, info.get("previewUrl"));
    }

    @Test
    @DisplayName("移动文件 - 成功案例")
    void testMoveFileSuccess() {
        // Given
        Long fileId = 1L;
        Long targetFolderId = 2L;
        Long userId = 1L;

        // When
        ServiceResult<FileDTO> result = fileService.moveFile(fileId, targetFolderId, userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("文件移动成功", result.getMessage());

        FileDTO fileDTO = result.getData();
        assertNotNull(fileDTO);
        assertEquals(fileId, fileDTO.getFileId());
        assertEquals(targetFolderId, fileDTO.getFolderId());
    }

    @Test
    @DisplayName("复制文件 - 成功案例")
    void testCopyFileSuccess() {
        // Given
        Long fileId = 1L;
        Long targetFolderId = 2L;
        String newFileName = "copy-of-file.txt";
        Long userId = 1L;

        // When
        ServiceResult<FileDTO> result = fileService.copyFile(fileId, targetFolderId, newFileName, userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("文件复制成功", result.getMessage());

        FileDTO fileDTO = result.getData();
        assertNotNull(fileDTO);
        assertEquals(newFileName, fileDTO.getFileName());
        assertEquals(targetFolderId, fileDTO.getFolderId());
    }
}
