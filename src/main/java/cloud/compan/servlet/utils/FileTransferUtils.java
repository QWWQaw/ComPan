// FileTransferUtils.java
package cloud.compan.servlet.utils;

import cloud.compan.servlet.model.transfer.FileChunk;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.UUID;

public class FileTransferUtils {

    private static final String TEMP_DIR = ConfigUtil.getProperty("upload.temp.dir", "./temp_uploads");
    private static final int CHUNK_SIZE = Integer.parseInt(ConfigUtil.getProperty("upload.chunk.size", "5242880"));

    public static String generateSessionId() {
        return "SESS_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static String generateFileId(String fileName) {
        // 使用UUID确保唯一性
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // 添加时间戳防止冲突
        long timestamp = System.currentTimeMillis();

        // 保留原始文件名后缀
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = fileName.substring(dotIndex);
        }

        return timestamp + "_" + uuid + extension;
    }

    public static int getChunkSize() {
        return CHUNK_SIZE;
    }

    public static String getTempDir() {
        return TEMP_DIR;
    }

    public static void saveChunkData(FileChunk chunk) {
        Path tempDir = Paths.get(TEMP_DIR);

        if (!Files.exists(tempDir)) {
            try {
                Files.createDirectories(tempDir);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create temp directory", e);
            }
        }

        Path chunkPath = tempDir.resolve(chunk.getSessionId() + "_" + chunk.getIndex() + ".tmp");

        // 确保输入流可重置
        InputStream inputStream = chunk.getData();
        if (!inputStream.markSupported()) {
            // 如果输入流不支持标记，创建一个可重置的副本
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int bytesRead;
            try {
                while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }
                inputStream = new ByteArrayInputStream(buffer.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read chunk data", e);
            }
        } else {
            // 重置输入流以便多次读取
            try {
                inputStream.reset();
            } catch (IOException e) {
                throw new RuntimeException("Failed to reset input stream", e);
            }
        }

        // 保存数据到文件
        try (OutputStream os = Files.newOutputStream(chunkPath, StandardOpenOption.CREATE)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save chunk data", e);
        }

        // 验证校验和
        try {
            inputStream.reset(); // 重置输入流
            if (!verifyChecksum(inputStream, chunk.getChecksum())) {
                try {
                    Files.deleteIfExists(chunkPath);
                } catch (IOException ignore) {}
                throw new RuntimeException("Chunk checksum mismatch");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to verify checksum", e);
        }
    }

    private static boolean verifyChecksum(InputStream inputStream, String expectedChecksum) {
        try {
            // 使用MD5计算校验和
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashedBytes = digest.digest();
            String actualChecksum = bytesToHex(hashedBytes);
            return actualChecksum.equalsIgnoreCase(expectedChecksum);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean verifyChecksum(Path file, String expectedChecksum) {
        try (InputStream is = Files.newInputStream(file)) {
            // 使用HashUtil计算文件的MD5（注意：这里需要MD5，但HashUtil只提供SHA-256，所以需要调整）
            // 由于不能修改HashUtil，我们这里临时使用MD5计算
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashedBytes = digest.digest();
            String actualChecksum = bytesToHex(hashedBytes);
            return actualChecksum.equalsIgnoreCase(expectedChecksum);
        } catch (Exception e) {
            return false;
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
