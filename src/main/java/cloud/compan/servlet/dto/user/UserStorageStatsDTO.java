package cloud.compan.servlet.dto.user;

public class UserStorageStatsDTO {
    private Long userId;
    private String username;
    private Long storageUsed;
    private Long storageLimit;
    private Long storageAvailable;
    private Integer fileCount;
    private Integer folderCount;
    private Double usagePercentage;

    // 构造函数
    public UserStorageStatsDTO() {}

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getStorageUsed() { return storageUsed; }
    public void setStorageUsed(Long storageUsed) { this.storageUsed = storageUsed; }

    public Long getStorageLimit() { return storageLimit; }
    public void setStorageLimit(Long storageLimit) { this.storageLimit = storageLimit; }

    public Long getStorageAvailable() { return storageAvailable; }
    public void setStorageAvailable(Long storageAvailable) { this.storageAvailable = storageAvailable; }

    public Integer getFileCount() { return fileCount; }
    public void setFileCount(Integer fileCount) { this.fileCount = fileCount; }

    public Integer getFolderCount() { return folderCount; }
    public void setFolderCount(Integer folderCount) { this.folderCount = folderCount; }

    public Double getUsagePercentage() { return usagePercentage; }
    public void setUsagePercentage(Double usagePercentage) { this.usagePercentage = usagePercentage; }

}
