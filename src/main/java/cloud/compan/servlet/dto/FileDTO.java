package cloud.compan.servlet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * 文件数据传输对象
 * 用于API响应和前端交互的文件信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDTO {
    
    @Positive(message = "文件ID必须为正数")
    private Long fileId;

    @Positive(message = "上传者ID必须为正数")
    private Long uploaderId;

    @Size(max = 50, message = "上传者名称长度不能超过50个字符")
    private String uploaderName;

    @Positive(message = "文件夹ID必须为正数")
    private Long folderId;

    @Size(max = 100, message = "文件夹名称长度不能超过100个字符")
    private String folderName;

    @Size(max = 500, message = "文件夹路径长度不能超过500个字符")
    private String folderPath;

    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255个字符")
    private String fileName;

    @Size(max = 100, message = "MIME类型长度不能超过100个字符")
    private String mimeType;

    @PositiveOrZero(message = "文件大小不能为负数")
    private Long fileSize;

    @Size(max = 64, message = "对象哈希长度不能超过64个字符")
    private String objectHash;

    @Size(max = 20, message = "状态长度不能超过20个字符")
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
        if (fileSize == null) return "0 B";

        long size = fileSize;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", (double) size, units[unitIndex]);
    }
}
