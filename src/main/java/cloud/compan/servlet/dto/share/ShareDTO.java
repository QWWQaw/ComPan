package cloud.compan.servlet.dto.share;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

/**
 * 创建分享请求DTO
 */
public class ShareCreateDTO {
    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    private String password; // 可选，分享密码
    private String expireAt; // 可选，过期时间
    private boolean allowDownload = true; // 是否允许下载
    private boolean allowPreview = true; // 是否允许预览

    // 构造函数
    public ShareCreateDTO() {}

    public ShareCreateDTO(Long fileId, String password, String expireAt) {
        this.fileId = fileId;
        this.password = password;
        this.expireAt = expireAt;
    }

    // Getters and Setters
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getExpireAt() { return expireAt; }
    public void setExpireAt(String expireAt) { this.expireAt = expireAt; }

    public boolean isAllowDownload() { return allowDownload; }
    public void setAllowDownload(boolean allowDownload) { this.allowDownload = allowDownload; }

    public boolean isAllowPreview() { return allowPreview; }
    public void setAllowPreview(boolean allowPreview) { this.allowPreview = allowPreview; }

    // 验证方法
    public boolean isValid() {
        return fileId != null;
    }

    public boolean hasPassword() {
        return password != null && !password.trim().isEmpty();
    }
}

/**
 * 分享信息DTO
 */
class ShareInfoDTO {
    private Long shareId;
    private String shareLink;
    private String shareUrl;
    private Long fileId;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private String sharedBy; // 分享者用户名
    private boolean hasPassword;
    private Timestamp expireAt;
    private boolean isExpired;
    private boolean allowDownload;
    private boolean allowPreview;
    private Timestamp createdAt;
    private Long accessCount; // 访问次数

    // 构造函数
    public ShareInfoDTO() {}

    // Getters and Setters
    public Long getShareId() { return shareId; }
    public void setShareId(Long shareId) { this.shareId = shareId; }

    public String getShareLink() { return shareLink; }
    public void setShareLink(String shareLink) { this.shareLink = shareLink; }

    public String getShareUrl() { return shareUrl; }
    public void setShareUrl(String shareUrl) { this.shareUrl = shareUrl; }

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getSharedBy() { return sharedBy; }
    public void setSharedBy(String sharedBy) { this.sharedBy = sharedBy; }

    public boolean isHasPassword() { return hasPassword; }
    public void setHasPassword(boolean hasPassword) { this.hasPassword = hasPassword; }

    public Timestamp getExpireAt() { return expireAt; }
    public void setExpireAt(Timestamp expireAt) { this.expireAt = expireAt; }

    public boolean isExpired() { return isExpired; }
    public void setExpired(boolean expired) { isExpired = expired; }

    public boolean isAllowDownload() { return allowDownload; }
    public void setAllowDownload(boolean allowDownload) { this.allowDownload = allowDownload; }

    public boolean isAllowPreview() { return allowPreview; }
    public void setAllowPreview(boolean allowPreview) { this.allowPreview = allowPreview; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Long getAccessCount() { return accessCount; }
    public void setAccessCount(Long accessCount) { this.accessCount = accessCount; }

    /**
     * 格式化文件大小
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";

        String[] units = {"B", "KB", "MB", "GB", "TB"};
        long size = fileSize;
        int unitIndex = 0;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return size + " " + units[unitIndex];
    }
}

/**
 * 分享访问请求DTO
 */
class ShareAccessDTO {
    private String shareLink;
    private String password; // 可选，如果分享有密码保护

    // 构造函数
    public ShareAccessDTO() {}

    public ShareAccessDTO(String shareLink, String password) {
        this.shareLink = shareLink;
        this.password = password;
    }

    // Getters and Setters
    public String getShareLink() { return shareLink; }
    public void setShareLink(String shareLink) { this.shareLink = shareLink; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // 验证方法
    public boolean isValid() {
        return shareLink != null && !shareLink.trim().isEmpty();
    }
}
