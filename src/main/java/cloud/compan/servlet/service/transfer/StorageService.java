// StorageService.java (新增接口)
package cloud.compan.servlet.service.transfer;

import java.io.InputStream;

public interface StorageService {
    String storeFile(InputStream data, long size, String fileName);
    InputStream retrieveFile(String fileId);
    boolean deleteFile(String fileId);
}