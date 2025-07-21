package cloud.compan.servlet.service.impl;

import cloud.compan.servlet.model.StorageObject;
import cloud.compan.servlet.model.transfer.FileChunk;
import cloud.compan.servlet.model.transfer.UploadSession;
import cloud.compan.servlet.repository.StorageObjectRepository;
import cloud.compan.servlet.service.transfer.ChunkService;
import cloud.compan.servlet.service.transfer.StorageService;
import cloud.compan.servlet.utils.FileTransferUtils;
import cloud.compan.servlet.utils.HashUtil;
import jakarta.servlet.jsp.jstl.core.LoopTagSupport;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

public class ChunkServiceImpl implements ChunkService {

    private final StorageService storageService;
    private final StorageObjectRepository storageObjectRepository;
    public final Map<String, UploadSession> sessionMap = new HashMap<>();
    private final Set<String> processingSessions = new HashSet<>();

    public ChunkServiceImpl(StorageService storageService, StorageObjectRepository storageObjectRepository) {
        this.storageService = storageService;
        this.storageObjectRepository = storageObjectRepository;
    }

    @Override
    public UploadSession initUploadSession(String fileName, long fileSize) {
        UploadSession session = new UploadSession();
        session.setSessionId(FileTransferUtils.generateSessionId());
        session.setFileName(fileName);
        session.setFileSize(fileSize);
        session.setStartTime(new Date());

        sessionMap.put(session.getSessionId(), session);
        return session;
    }

    @Override
    public void saveChunk(FileChunk chunk) {
        if (!sessionMap.containsKey(chunk.getSessionId())) {
            throw new IllegalStateException("Invalid session ID: " + chunk.getSessionId());
        }

        FileTransferUtils.saveChunkData(chunk);

        UploadSession session = sessionMap.get(chunk.getSessionId());
        session.addCompletedChunk(chunk.getIndex()); // 修改这一行
    }

    @Override
    public String mergeChunks(String sessionId) {
        if (!sessionMap.containsKey(sessionId)) {
            throw new IllegalStateException("Invalid session ID: " + sessionId);
        }

        synchronized (processingSessions) {
            if (processingSessions.contains(sessionId)) {
                throw new IllegalStateException("Session is already being processed");
            }
            processingSessions.add(sessionId);
        }

        Path tempDir = Paths.get(FileTransferUtils.getTempDir());
        Path tempFile = null;

        try {
            UploadSession session = sessionMap.get(sessionId);

            Set<Integer> missing = getMissingChunks(sessionId);
            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing chunks: " + missing);
            }

            tempFile = tempDir.resolve("merged_" + sessionId + ".tmp");

            try (OutputStream os = Files.newOutputStream(tempFile, StandardOpenOption.CREATE)) {
                int totalChunks = (int) Math.ceil(session.getFileSize() / (double) FileTransferUtils.getChunkSize());

                for (int i = 0; i < totalChunks; i++) {
                    Path chunkPath = tempDir.resolve(sessionId + "_" + i + ".tmp");

                    try (InputStream is = Files.newInputStream(chunkPath)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                    } finally {
                        Files.deleteIfExists(chunkPath);
                    }
                }
            }

            // 使用HashUtil计算文件哈希
            String finalHash;
            try (InputStream is = Files.newInputStream(tempFile)) {
                HashUtil hashUtil = new HashUtil();
                finalHash = hashUtil.generateFileHash(is);
            }

            // 检查文件是否已存在
            Optional<StorageObject> existing = storageObjectRepository.findById(finalHash);
            if (existing.isPresent()) {
                // 文件已存在，增加引用计数
                StorageObject storageObject = existing.get();
                storageObject.setRefCount(storageObject.getRefCount() + 1);
                storageObjectRepository.save(storageObject);

                // 删除临时文件
                Files.deleteIfExists(tempFile);

                return finalHash;
            }

            // 存储文件
            try (InputStream is = Files.newInputStream(tempFile)) {
                String storagePath = storageService.storeFile(is, session.getFileSize(), session.getFileName());

                // 创建StorageObject
                StorageObject storageObject = new StorageObject();
                storageObject.setHash(finalHash);
                storageObject.setSize(session.getFileSize());
                storageObject.setStoragePath(storagePath);
                storageObject.setRefCount(1);
                storageObject.setCreatedAt(LocalDateTime.now());

                // 保存StorageObject
                storageObjectRepository.save(storageObject);

                return finalHash;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to merge chunks", e);
        } finally {
            // 确保临时文件被删除
            try {
                if (tempFile != null) {
                    Files.deleteIfExists(tempFile);
                }
            } catch (IOException e) {
                System.err.println("Failed to delete temporary file: " + e.getMessage());
            }

            synchronized (processingSessions) {
                processingSessions.remove(sessionId);
                sessionMap.remove(sessionId);
            }
        }
    }

    @Override
    public Set<Integer> getMissingChunks(String sessionId) {
        if (!sessionMap.containsKey(sessionId)) {
            throw new IllegalStateException("Invalid session ID: " + sessionId);
        }

        UploadSession session = sessionMap.get(sessionId);
        int totalChunks = (int) Math.ceil(session.getFileSize() / (double) FileTransferUtils.getChunkSize());

        Set<Integer> missingChunks = new HashSet<>();
        for (int i = 0; i < totalChunks; i++) {
            if (!session.getCompletedChunks().contains(i)) { // 修改这一行
                missingChunks.add(i);
            }
        }
        return missingChunks;
    }
}