package cloud.compan.servlet.dto.folder;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 文件夹创建请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FolderCreateDTO {
    @NotBlank(message = "文件夹名称不能为空")
    @Size(max = 255, message = "文件夹名称长度不能超过255个字符")
    private String folderName;

    @Min(value = 1, message = "父文件夹ID必须大于0")
    private Long parentFolderId;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
}
