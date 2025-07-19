package cloud.compan.servlet.dto.share;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 分享创建请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareCreateDTO {
    @NotBlank(message = "资源类型不能为空")
    @Pattern(regexp = "^(file|folder)$", message = "资源类型只能是file或folder")
    private String resourceType; // "file" 或 "folder"

    @NotNull(message = "资源ID不能为空")
    @Min(value = 1, message = "资源ID必须大于0")
    private Long resourceId;

    @Min(value = 1, message = "过期天数必须大于0")
    @Max(value = 365, message = "过期天数不能超过365天")
    private Integer expiryDays;

    @Size(min = 4, max = 20, message = "分享密码长度必须在4-20位之间")
    private String password;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
}
