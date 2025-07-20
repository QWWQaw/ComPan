package cloud.compan.servlet.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;

/**
 * 存储统计数据传输对象
 * 封装用户存储使用情况的统计信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageStatsDTO {
    @PositiveOrZero(message = "存储限制不能为负数")
    private long storageLimit;

    @PositiveOrZero(message = "已使用存储不能为负数")
    private long storageUsed;

    @PositiveOrZero(message = "文件数量不能为负数")
    private int fileCount;

    @PositiveOrZero(message = "文件夹数量不能为负数")
    private int folderCount;

    @PositiveOrZero(message = "可用空间不能为负数")
    private long availableSpace;

    @DecimalMin(value = "0.0", message = "使用百分比不能为负数")
    @DecimalMax(value = "100.0", message = "使用百分比不能超过100")
    private double usagePercentage;
    
    public StorageStatsDTO(long storageLimit, long storageUsed, int fileCount, int folderCount) {
        this.storageLimit = storageLimit;
        this.storageUsed = storageUsed;
        this.fileCount = fileCount;
        this.folderCount = folderCount;
        this.availableSpace = storageLimit - storageUsed;
        this.usagePercentage = storageLimit > 0 ? (double) storageUsed / storageLimit * 100.0 : 0.0;
    }

    // getters and setters

    public long getStorageLimit() {
        return storageLimit;
    }

    public void setStorageLimit(long storageLimit) {
        this.storageLimit = storageLimit;
        this.availableSpace = storageLimit - storageUsed;
        this.usagePercentage = storageLimit > 0 ? (double) storageUsed / storageLimit * 100.0 : 0.0;
    }
    
    public long getStorageUsed() {
        return storageUsed;
    }

    public void setStorageUsed(long storageUsed) {
        this.storageUsed = storageUsed;
        this.availableSpace = storageLimit - storageUsed;
        this.usagePercentage = storageLimit > 0 ? (double) storageUsed / storageLimit * 100.0 : 0.0;
    }

    public int getFileCount() {
        return fileCount;
    }

}
