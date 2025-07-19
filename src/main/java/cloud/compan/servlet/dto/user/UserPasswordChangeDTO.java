package cloud.compan.servlet.dto.user;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用户密码修改请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordChangeDTO {
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 50, message = "新密码长度必须在6-50位之间")
    private String newPassword;
}
