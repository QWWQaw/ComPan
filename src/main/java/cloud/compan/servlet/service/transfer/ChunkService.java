// ChunkService.java
package cloud.compan.servlet.service.transfer;

import cloud.compan.servlet.model.transfer.FileChunk;
import cloud.compan.servlet.model.transfer.UploadSession;

import java.util.Set;

public interface ChunkService {
    UploadSession initUploadSession(String fileName, long fileSize);
    void saveChunk(FileChunk chunk);
    String mergeChunks(String sessionId);
    Set<Integer> getMissingChunks(String sessionId);
}