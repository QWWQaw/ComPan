package cloud.compan.servlet.controller;

import java.util.Map;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import cloud.compan.servlet.annotations.PutMapping;
import cloud.compan.servlet.annotations.RequestBody;
import cloud.compan.servlet.annotations.RestController;
import cloud.compan.servlet.dto.mapper.UserMapper;
import cloud.compan.servlet.service.UserService;
import cloud.compan.servlet.web.response.ApiResponseWrapper;

/**
 * 用户控制器
 * 处理用户相关的HTTP请求，包括认证、用户管理等功能
 */
@RestController("/api/test")
@Singleton
public class TestController extends BaseController {

    @Inject
    private UserService userService;

    @Inject
    private UserMapper userMapper;

    @PutMapping(path = "/api/test/change-password")
    public ApiResponseWrapper changePassword(@RequestBody Map<String, Object> requestData) {
        System.out.println("CALLED: TestController.changePassword()");
        System.out.println("REQUEST_PARAMS: " + requestData);
        
        String oldPassword = (String) requestData.get("old_password");
        String newPassword = (String) requestData.get("new_password");
        
        // Parameter validation
        requireNonEmpty(oldPassword, "old_password");
        requireNonEmpty(newPassword, "new_password");
        validateStringLength(newPassword, "new_password", 6, 100);
        
        // Mock password change logic
        return success("Password changed successfully", Map.of(
            "message", "Password updated",
            "timestamp", java.time.LocalDateTime.now()
        ));
    }
}