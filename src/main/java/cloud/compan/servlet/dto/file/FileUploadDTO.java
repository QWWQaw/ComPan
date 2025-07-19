package cloud.compan.servlet.dto.file;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 文件上传请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadDTO {
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255个字符")
    private String fileName;

    @Min(value = 1, message = "文件夹ID必须大于0")
    private Long folderId;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    @NotNull(message = "文件大小不能为空")
    @Min(value = 1, message = "文件大小必须大于0")
    private Long fileSize;
}
