package cloud.compan.servlet.dto;
import lombok.Data;

/**
 * 存储统计数据传输对象
 * 封装用户存储使用情况的统计信息
 */
@Data
public class StorageStatsDTO {
    private long storageLimit;
    private long storageUsed;
    private int fileCount;
    private int folderCount;
    private long availableSpace;
    private double usagePercentage;
    
    public StorageStatsDTO() {}
    
    public StorageStatsDTO(long storageLimit, long storageUsed, int fileCount, int folderCount) {
        this.storageLimit = storageLimit;
        this.storageUsed = storageUsed;
        this.fileCount = fileCount;
        this.folderCount = folderCount;
        this.availableSpace = storageLimit - storageUsed;
        this.usagePercentage = storageLimit > 0 ? (double) storageUsed / storageLimit * 100.0 : 0.0;
    }
    
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
    
    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }
    
    public int getFolderCount() {
        return folderCount;
    }
    
    public void setFolderCount(int folderCount) {
        this.folderCount = folderCount;
    }
    
    public long getAvailableSpace() {
        return availableSpace;
    }
    
    public void setAvailableSpace(long availableSpace) {
        this.availableSpace = availableSpace;
    }
    
    public double getUsagePercentage() {
        return usagePercentage;
    }
    
    public void setUsagePercentage(double usagePercentage) {
        this.usagePercentage = usagePercentage;
    }
} 