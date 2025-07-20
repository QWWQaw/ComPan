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
    private Long totalStorage;  // 修正属性名

    @PositiveOrZero(message = "已使用存储不能为负数")
    private Long usedStorage;   // 修正属性名

    @PositiveOrZero(message = "可用空间不能为负数")
    private Long availableStorage; // 添加可用空间

    @DecimalMin(value = "0.0", message = "使用百分比不能为负数")
    @DecimalMax(value = "100.0", message = "使用百分比不能超过100")
    private Double usagePercentage; // 添加使用百分比

    @PositiveOrZero(message = "文件数量不能为负数")
    private Integer fileCount;

    @PositiveOrZero(message = "文件夹数量不能为负数")
    private Integer folderCount;

    public StorageStatsDTO(long totalStorage, long usedStorage, int fileCount, int folderCount) {
        this.totalStorage = totalStorage;
        this.usedStorage = usedStorage;
        this.fileCount = fileCount;
        this.folderCount = folderCount;
        this.availableStorage = totalStorage - usedStorage;
        this.usagePercentage = totalStorage > 0 ? (double) usedStorage / totalStorage * 100.0 : 0.0;
    }

    // 自定义setter方法以保持数据一致性
    public void setTotalStorage(long totalStorage) {
        this.totalStorage = totalStorage;
        this.availableStorage = totalStorage - usedStorage;
        this.usagePercentage = totalStorage > 0 ? (double) usedStorage / totalStorage * 100.0 : 0.0;
    }
    
    public void setUsedStorage(long usedStorage) {
        this.usedStorage = usedStorage;
        this.availableStorage = totalStorage - usedStorage;
        this.usagePercentage = totalStorage > 0 ? (double) usedStorage / totalStorage * 100.0 : 0.0;
    }
}
