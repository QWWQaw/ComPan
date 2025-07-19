package cloud.compan.servlet.dto.folder;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 文件夹重命名请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FolderRenameDTO {
    @NotNull(message = "文件夹ID不能为空")
    @Min(value = 1, message = "文件夹ID必须大于0")
    private Long folderId;

    @NotBlank(message = "新文件夹名称不能为空")
    @Size(max = 255, message = "文件夹名称长度不能超过255个字符")
    private String newFolderName;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
}
