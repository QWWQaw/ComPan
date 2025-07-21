// LocalStorageServiceImpl.java (完整实现)
package cloud.compan.servlet.service.impl;

import cloud.compan.servlet.model.StorageObject;
import cloud.compan.servlet.service.transfer.StorageService;
import cloud.compan.servlet.utils.ConfigUtil;
import cloud.compan.servlet.utils.FileTransferUtils;

import java.io.*;
import java.nio.file.*;
import java.util.Date;

public class LocalStorageServiceImpl implements StorageService {

    private final String basePath;
    private final Path storageDir;

    public LocalStorageServiceImpl() {
        this.basePath = ConfigUtil.getProperty("storage.local.path", "./uploads");
        this.storageDir = Paths.get(basePath);

        // 创建存储目录
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directory", e);
        }
    }

    @Override
    public String storeFile(InputStream data, long size, String fileName) {
        // 生成唯一存储路径
        String fileId = FileTransferUtils.generateFileId(fileName);
        Path filePath = storageDir.resolve(fileId);

        try (OutputStream os = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;

            while ((bytesRead = data.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;

                // 验证文件大小
                if (totalBytes > size) {
                    throw new IOException("Actual file size exceeds expected size");
                }
            }

            // 验证文件大小
            if (totalBytes != size) {
                throw new IOException("Incomplete file upload. Expected: " + size + ", Actual: " + totalBytes);
            }

            return fileId;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public InputStream retrieveFile(String fileId) {
        Path filePath = storageDir.resolve(fileId);

        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File not found: " + fileId);
        }

        try {
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve file", e);
        }
    }

    @Override
    public boolean deleteFile(String fileId) {
        Path filePath = storageDir.resolve(fileId);

        if (!Files.exists(filePath)) {
            return false;
        }

        try {
            Files.delete(filePath);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}