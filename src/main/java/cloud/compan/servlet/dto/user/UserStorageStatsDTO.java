package cloud.compan.servlet.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStorageStatsDTO {
    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "用户ID必须大于0")
    private Long userId;

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;

    @NotNull(message = "已用存储空间不能为空")
    @Min(value = 0, message = "已用存储空间不能为负数")
    private Long storageUsed;

    @NotNull(message = "存储空间限制不能为空")
    @Min(value = 1, message = "存储空间限制必须大于0")
    private Long storageLimit;

    @NotNull(message = "可用存储空间不能为空")
    @Min(value = 0, message = "可用存储空间不能为负数")
    private Long storageAvailable;

    @NotNull(message = "文件数量不能为空")
    @Min(value = 0, message = "文件数量不能为负数")
    private Integer fileCount;

    @NotNull(message = "文件夹数量不能为空")
    @Min(value = 0, message = "文件夹数量不能为负数")
    private Integer folderCount;

    @NotNull(message = "使用百分比不能为空")
    @DecimalMin(value = "0.0", message = "使用百分比不能小于0")
    @DecimalMax(value = "100.0", message = "使用百分比不能大于100")
    private Double usagePercentage;
}
