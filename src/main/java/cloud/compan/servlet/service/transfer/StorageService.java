package cloud.compan.servlet.service.transfer;

import java.io.InputStream;

public interface StorageService {
    /**
     * 存储文件
     * @param data 文件数据流
     * @param size 文件大小
     * @param fileName 原始文件名
     * @return 存储路径
     */
    String storeFile(InputStream data, long size, String fileName);

    /**
     * 检索文件
     * @param fileId 文件ID（通常是文件哈希）
     * @return 文件数据流
     */
    InputStream retrieveFile(String fileId);

    /**
     * 删除文件
     * @param fileId 文件ID
     * @return 是否删除成功
     */
    boolean deleteFile(String fileId);
}