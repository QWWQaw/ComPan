package cloud.compan.servlet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

/**
 * 登录结果数据传输对象
 * 封装登录成功后返回给客户端的数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResultDTO {

    @Valid
    @NotNull(message = "用户信息不能为空")
    private UserDTO user;

    @NotBlank(message = "令牌不能为空")
    private String token;

    @Positive(message = "过期时间必须为正数")
    private long expiresIn;

    @NotBlank(message = "令牌类型不能为空")
    @Builder.Default
    private String tokenType = "Bearer";
    
    public LoginResultDTO(UserDTO user, String token, long expiresIn) {
        this.user = user;
        this.token = token;
        this.expiresIn = expiresIn;
        this.tokenType = "Bearer";
    }
}
