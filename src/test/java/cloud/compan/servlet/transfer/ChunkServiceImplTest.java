package cloud.compan.servlet.transfer;

import cloud.compan.servlet.model.StorageObject;
import cloud.compan.servlet.model.transfer.FileChunk;
import cloud.compan.servlet.model.transfer.UploadSession;
import cloud.compan.servlet.repository.StorageObjectRepository;
import cloud.compan.servlet.service.impl.ChunkServiceImpl;
import cloud.compan.servlet.service.transfer.ChunkService;
import cloud.compan.servlet.service.transfer.StorageService;
import cloud.compan.servlet.utils.FileTransferUtils;
import cloud.compan.servlet.utils.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ChunkServiceImplTest {

    private ChunkService chunkService;
    private StorageService storageService;
    private StorageObjectRepository storageObjectRepository;

    @BeforeEach
    void setUp() {
        storageService = mock(StorageService.class);
        storageObjectRepository = mock(StorageObjectRepository.class);

        chunkService = new ChunkServiceImpl(storageService, storageObjectRepository);
    }

    @Test
    void testInitUploadSession() {
        UploadSession session = chunkService.initUploadSession("test.txt", 1024);

        assertNotNull(session.getSessionId());
        assertEquals("test.txt", session.getFileName());
        assertEquals(1024, session.getFileSize());
        assertNotNull(session.getStartTime());
        assertTrue(session.getCompletedChunks().isEmpty());
    }

    @Test
    void testSaveChunk() throws IOException {
        // 创建测试会话
        UploadSession session = chunkService.initUploadSession("test.txt", 1024);
        String sessionId = session.getSessionId();

        // 创建测试分块
        byte[] chunkData = "Test chunk data".getBytes();
        FileChunk chunk = new FileChunk();
        chunk.setSessionId(sessionId);
        chunk.setIndex(0);
        chunk.setData(new ByteArrayInputStream(chunkData));
        chunk.setChecksum(HashUtil.calculateMD5(chunkData));

        // 保存分块
        chunkService.saveChunk(chunk);

        // 验证分块已保存
        Path chunkPath = Paths.get(FileTransferUtils.getTempDir(), sessionId + "_0.tmp");
        assertTrue(Files.exists(chunkPath));

        // 验证会话状态更新
        assertTrue(session.getCompletedChunks().contains(0));

        // 清理测试文件
        Files.deleteIfExists(chunkPath);
    }

    @Test
    void testMergeChunks() throws Exception {
        // 创建测试会话
        UploadSession session = chunkService.initUploadSession("test.txt", 30);
        String sessionId = session.getSessionId();

        // 创建两个测试分块
        byte[] chunk1Data = "First chunk data".getBytes();
        byte[] chunk2Data = "Second chunk data".getBytes();

        // 保存分块1
        FileChunk chunk1 = new FileChunk();
        chunk1.setSessionId(sessionId);
        chunk1.setIndex(0);
        chunk1.setData(new ByteArrayInputStream(chunk1Data));
        chunk1.setChecksum(HashUtil.calculateMD5(chunk1Data));
        chunkService.saveChunk(chunk1);

        // 保存分块2
        FileChunk chunk2 = new FileChunk();
        chunk2.setSessionId(sessionId);
        chunk2.setIndex(1);
        chunk2.setData(new ByteArrayInputStream(chunk2Data));
        chunk2.setChecksum(HashUtil.calculateMD5(chunk2Data));
        chunkService.saveChunk(chunk2);

        // 模拟存储服务
        String storagePath = "/storage/test_file";
        when(storageService.storeFile(any(), anyLong(), anyString())).thenReturn(storagePath);

        // 模拟仓库行为
        when(storageObjectRepository.findById(anyString())).thenReturn(Optional.empty());

        // 合并分块
        String fileId = chunkService.mergeChunks(sessionId);

        // 验证结果
        assertNotNull(fileId);

        // 验证存储服务调用
        verify(storageService).storeFile(any(), eq(30L), eq("test.txt"));

        // 验证仓库调用
        verify(storageObjectRepository).save(any(StorageObject.class));

        // 验证临时文件已清理
        Path chunkPath1 = Paths.get(FileTransferUtils.getTempDir(), sessionId + "_0.tmp");
        Path mergedFile = Paths.get(FileTransferUtils.getTempDir(), "merged_" + sessionId + ".tmp");

        assertFalse(Files.exists(chunkPath1));
        assertFalse(Files.exists(mergedFile));
    }

    @Test
    void testGetMissingChunksWithMultipleChunks() {
        // 创建测试会话 - 需要3个分块
        int chunkSize = FileTransferUtils.getChunkSize();
        long fileSize = chunkSize * 3; // 15MB文件
        UploadSession session = chunkService.initUploadSession("test.txt", fileSize);
        String sessionId = session.getSessionId();

        // 模拟上传块 0 和块 2
        byte[] dataBytes = new byte[chunkSize];
        Arrays.fill(dataBytes, (byte) 1);

        // 上传分块0
        FileChunk chunk0 = new FileChunk();
        chunk0.setSessionId(sessionId);
        chunk0.setIndex(0);
        chunk0.setData(new ByteArrayInputStream(dataBytes));
        chunk0.setChecksum(HashUtil.calculateMD5(dataBytes));
        chunkService.saveChunk(chunk0);

        // 上传分块2
        FileChunk chunk2 = new FileChunk();
        chunk2.setSessionId(sessionId);
        chunk2.setIndex(2);
        chunk2.setData(new ByteArrayInputStream(dataBytes));
        chunk2.setChecksum(HashUtil.calculateMD5(dataBytes));
        chunkService.saveChunk(chunk2);

        // 获取缺失分块
        Set<Integer> missingChunks = chunkService.getMissingChunks(sessionId);

        // 验证结果，应该缺失分块1
        assertEquals(1, missingChunks.size(), "应该只有1个缺失分块");
        assertTrue(missingChunks.contains(1), "缺失分块应该是索引1");
    }

    @Test
    void testMergeChunksWithExistingFile() throws Exception {
        // 创建测试会话
        UploadSession session = chunkService.initUploadSession("test.txt", 30);
        String sessionId = session.getSessionId();

        // 创建测试分块
        byte[] chunkData = "Test chunk data".getBytes();
        FileChunk chunk = new FileChunk();
        chunk.setSessionId(sessionId);
        chunk.setIndex(0);
        chunk.setData(new ByteArrayInputStream(chunkData));
        chunk.setChecksum(HashUtil.calculateMD5(chunkData));
        chunkService.saveChunk(chunk);

        // 计算预期的文件哈希
        HashUtil hashUtil = new HashUtil();
        String expectedHash = hashUtil.generateFileHash(new ByteArrayInputStream(chunkData));

        // 创建已有的存储对象
        StorageObject existingObject = new StorageObject();
        existingObject.setHash(expectedHash);
        existingObject.setRefCount(1);

        // 模拟文件已存在
        when(storageObjectRepository.findById(expectedHash)).thenReturn(Optional.of(existingObject));

        // 合并分块
        String fileId = chunkService.mergeChunks(sessionId);

        // 验证结果
        assertEquals(expectedHash, fileId);

        // 验证引用计数增加
        verify(storageObjectRepository).save(existingObject);
        assertEquals(2, existingObject.getRefCount());

        // 验证没有创建新文件
        verify(storageService, never()).storeFile(any(), anyLong(), anyString());
    }
}