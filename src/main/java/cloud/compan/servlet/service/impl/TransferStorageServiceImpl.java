package cloud.compan.servlet.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.Service;
import cloud.compan.servlet.service.transfer.StorageService;

/**
 * 文件传输存储服务实现类
 * 负责文件的上传、下载和删除操作
 */
@Service
@Singleton
public class TransferStorageServiceImpl implements StorageService {
    
    private static final String STORAGE_DIR = "temp_uploads";
    private final Path storagePath;
    
    public TransferStorageServiceImpl() {
        this.storagePath = Paths.get(STORAGE_DIR);
        createStorageDirectoryIfNotExists();
    }
    
    /**
     * 创建存储目录（如果不存在）
     */
    private void createStorageDirectoryIfNotExists() {
        try {
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory", e);
        }
    }
    
    @Override
    public String storeFile(InputStream data, long size, String fileName) {
        try {
            // 生成唯一的文件ID（使用UUID）
            String fileId = UUID.randomUUID().toString();
            Path filePath = storagePath.resolve(fileId);
            
            // 将文件数据写入存储
            Files.copy(data, filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return fileId;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + fileName, e);
        }
    }
    
    @Override
    public InputStream retrieveFile(String fileId) {
        try {
            Path filePath = storagePath.resolve(fileId);
            
            if (!Files.exists(filePath)) {
                return null; // 文件不存在
            }
            
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve file: " + fileId, e);
        }
    }
    
    @Override
    public boolean deleteFile(String fileId) {
        try {
            Path filePath = storagePath.resolve(fileId);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + fileId, e);
        }
    }
} 