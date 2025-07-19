package cloud.compan.servlet.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.sql.Timestamp;

/**
 * 用户活动日志DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityLogDTO {
    @NotNull(message = "日志ID不能为空")
    private Long logId;

    @NotBlank(message = "操作类型不能为空")
    @Size(max = 50, message = "操作类型长度不能超过50个字符")
    private String action;

    @Size(max = 500, message = "操作详情长度不能超过500个字符")
    private String details;

    @Size(max = 45, message = "IP地址长度不能超过45个字符")
    private String ipAddress;

    @Size(max = 500, message = "用户代理长度不能超过500个字符")
    private String userAgent;

    private Timestamp createdAt;
}
