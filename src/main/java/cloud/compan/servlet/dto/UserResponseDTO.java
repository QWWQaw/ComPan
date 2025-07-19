package cloud.compan.servlet.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.sql.Timestamp;

/**
 * 用户响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "用户ID必须大于0")
    private Long userId;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名必须是3-20位字符")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    @Size(max = 100, message = "用户状态长度不能超过100个字符")
    private String status;
}
