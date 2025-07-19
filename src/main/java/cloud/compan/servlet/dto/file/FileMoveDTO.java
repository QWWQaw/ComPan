package cloud.compan.servlet.dto.file;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 文件移动请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMoveDTO {
    @NotNull(message = "文件ID不能为空")
    @Min(value = 1, message = "文件ID必须大于0")
    private Long fileId;

    @Min(value = 1, message = "目标文件夹ID必须大于0")
    private Long targetFolderId;
}
