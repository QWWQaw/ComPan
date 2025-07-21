package cloud.compan.servlet.model.transfer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Getter
@Setter
@ToString

public class FileChunk {
    private String sessionId;
    private int index;
    private InputStream data;
    private String checksum;

    // 添加方法确保数据可重置
    public InputStream getResettableData() {
        if (data.markSupported()) {
            try {
                data.reset();
            } catch (Exception e) {
                // 处理异常
            }
            return data;
        }

        // 如果不支持重置，创建内存副本
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] dataBytes = new byte[8192];
            int bytesRead;
            while ((bytesRead = data.read(dataBytes)) != -1) {
                buffer.write(dataBytes, 0, bytesRead);
            }
            return new ByteArrayInputStream(buffer.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create resettable stream", e);
        }
    }

    // 其他 getter/setter
}
