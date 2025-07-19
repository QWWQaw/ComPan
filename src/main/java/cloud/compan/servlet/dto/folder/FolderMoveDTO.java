package cloud.compan.servlet.dto.folder;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 文件夹移动请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FolderMoveDTO {
    @NotNull(message = "文件夹ID不能为空")
    @Min(value = 1, message = "文件夹ID必须大于0")
    private Long folderId;

    @Min(value = 1, message = "目标父文件夹ID必须大于0")
    private Long targetParentId;
}
