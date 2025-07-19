package cloud.compan.servlet.dto.file;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.sql.Timestamp;

/**
 * 文件信息DTO
 * 用于Service层返回文件详细信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfoDTO {
    @NotNull(message = "文件ID不能为空")
    @Min(value = 1, message = "文件ID必须大于0")
    private Long fileId;

    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255个字符")
    private String fileName;

    @NotNull(message = "文件大小不能为空")
    @Min(value = 0, message = "文件大小不能为负数")
    private Long fileSize;

    @Size(max = 100, message = "MIME类型长度不能超过100个字符")
    private String mimeType;

    @Min(value = 1, message = "文件夹ID必须大于0")
    private Long folderId;

    @Size(max = 255, message = "文件夹名称长度不能超过255个字符")
    private String folderName; // 可选，文件夹名称

    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "用户ID必须大于0")
    private Long userId;

    @Size(max = 64, message = "文件哈希长度不能超过64个字符")
    private String fileHash;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private String downloadUrl; // 下载链接

    /**
     * 格式化文件大小为人类可读格式
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

    /**
     * 获取文件扩展名
     */
    public String getFileExtension() {
        if (fileName == null) return "";
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1).toLowerCase() : "";
    }
}
