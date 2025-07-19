package cloud.compan.servlet.dto.file;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 文件重命名请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileRenameDTO {
    @NotNull(message = "文件ID不能为空")
    @Min(value = 1, message = "文件ID必须大于0")
    private Long fileId;

    @NotBlank(message = "新文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过255个字符")
    private String newFileName;
}
