package cloud.compan.servlet.dto.file;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件操作结果DTO
 */
@Data
@NoArgsConstructor
public class FileOperationResultDTO {
    private Long fileId;

    @Size(max = 255, message = "文件名长度不能超过255个字符")
    private String fileName;

    @NotBlank(message = "操作类型不能为空")
    private String operation; // 操作类型：rename, move, delete, upload

    @NotNull(message = "操作结果不能为空")
    private Boolean success;

    private String message;

    private Long oldFolderId; // 原文件夹ID（用于移动操作）

    private Long newFolderId; // 新文件夹ID（用于移动操作）

    @Size(max = 255, message = "原文件名长度不能超过255个字符")
    private String oldFileName; // 原文件名（用于重命名操作）

    // 静态工厂方法
    public static FileOperationResultDTO success(String operation, Long fileId, String fileName, String message) {
        FileOperationResultDTO dto = new FileOperationResultDTO();
        dto.setOperation(operation);
        dto.setFileId(fileId);
        dto.setFileName(fileName);
        dto.setSuccess(true);
        dto.setMessage(message);
        return dto;
    }

    public static FileOperationResultDTO error(String operation, String message) {
        FileOperationResultDTO dto = new FileOperationResultDTO();
        dto.setOperation(operation);
        dto.setSuccess(false);
        dto.setMessage(message);
        return dto;
    }
}
