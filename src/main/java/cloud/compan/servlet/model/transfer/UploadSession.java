package cloud.compan.servlet.model.transfer;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
public class UploadSession {
    private String sessionId;
    private String fileName;
    private long fileSize;
    private Date startTime;
    private Set<Integer> completedChunks = new HashSet<>(); // 初始化集合

    /**
     * 添加已完成的分块索引
     * @param index 分块索引
     */
    public void addCompletedChunk(int index) {
        completedChunks.add(index); // 实际添加索引
    }
}