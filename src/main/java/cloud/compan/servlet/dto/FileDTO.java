package cloud.compan.servlet.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件数据传输对象
 * 用于API响应和前端交互的文件信息
 */

@Data
public class FileDTO {
    
    private Long fileId;
    private Long uploaderId;
    private String uploaderName;
    private Long folderId;
    private String folderName;
    private String folderPath;
    private String fileName;
    private String mimeType;
    private Long fileSize;
    private String objectHash;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    
    // 扩展字段
    private String downloadUrl;
    private String previewUrl;
    private String thumbnailUrl;
    private Boolean canRead;
    private Boolean canWrite;
    private Boolean canDelete;
    private Boolean isShared;
    private String fileExtension;
    private String fileIcon;
    
    // 默认构造函数
    public FileDTO() {}
    
    // 常用构造函数
    public FileDTO(Long fileId, String fileName, String mimeType, Long fileSize, LocalDateTime createdAt) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }
    
    // ============ Getters and Setters ============


    // ============ 工具方法 ============
    
    /**
     * 从文件名获取扩展名
     */
    public void extractFileExtension() {
        if (fileName != null && fileName.contains(".")) {
            this.fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
    }
    
    /**
     * 生成下载URL
     */
    public void generateDownloadUrl() {
        if (fileId != null) {
            this.downloadUrl = "/api/v1/files/" + fileId + "/download";
        }
    }
    
    /**
     * 生成预览URL
     */
    public void generatePreviewUrl() {
        if (fileId != null) {
            this.previewUrl = "/api/v1/files/" + fileId + "/preview";
        }
    }
    
    /**
     * 生成缩略图URL
     */
    public void generateThumbnailUrl(String size) {
        if (fileId != null) {
            this.thumbnailUrl = "/api/v1/files/" + fileId + "/thumbnail?size=" + (size != null ? size : "small");
        }
    }
    
    /**
     * 根据MIME类型设置文件图标
     */
    public void setFileIconByMimeType() {
        if (mimeType == null) {
            this.fileIcon = "file";
            return;
        }
        
        if (mimeType.startsWith("image/")) {
            this.fileIcon = "image";
        } else if (mimeType.startsWith("video/")) {
            this.fileIcon = "video";
        } else if (mimeType.startsWith("audio/")) {
            this.fileIcon = "audio";
        } else if (mimeType.contains("pdf")) {
            this.fileIcon = "pdf";
        } else if (mimeType.contains("document") || mimeType.contains("word")) {
            this.fileIcon = "document";
        } else if (mimeType.contains("spreadsheet") || mimeType.contains("excel")) {
            this.fileIcon = "spreadsheet";
        } else if (mimeType.contains("presentation") || mimeType.contains("powerpoint")) {
            this.fileIcon = "presentation";
        } else if (mimeType.contains("zip") || mimeType.contains("archive")) {
            this.fileIcon = "archive";
        } else {
            this.fileIcon = "file";
        }
    }
    
    /**
     * 格式化文件大小为可读格式
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "0 B";
        }
        
        long size = fileSize;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", (double) size, units[unitIndex]);
    }
    
    @Override
    public String toString() {
        return "FileDTO{" +
                "fileId=" + fileId +
                ", fileName='" + fileName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", fileSize=" + fileSize +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
} 