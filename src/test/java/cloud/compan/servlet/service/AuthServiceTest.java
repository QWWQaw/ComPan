package cloud.compan.servlet.service;

import cloud.compan.servlet.service.impl.AuthServiceImpl;
import cloud.compan.servlet.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthService 单元测试
 */
@DisplayName("认证服务测试")
public class AuthServiceTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("用户注册 - 成功案例")
    void testRegisterSuccess() {
        // Given
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";

        // When
        ServiceResult<UserDTO> result = authService.register(username, email, password);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("注册成功", result.getMessage());

        UserDTO user = result.getData();
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(10737418240L, user.getStorageLimit()); // 10GB
        assertEquals(0L, user.getStorageUsed());
    }

    @Test
    @DisplayName("验证注册数据 - 成功案例")
    void testValidateRegistrationDataSuccess() {
        // Given
        String username = "validuser";
        String email = "valid@example.com";
        String password = "validpassword";

        // When
        ServiceResult<Void> result = authService.validateRegistrationData(username, email, password);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("注册数据验证通过", result.getMessage());
    }

    @Test
    @DisplayName("验证注册数据 - 用户名为空")
    void testValidateRegistrationDataEmptyUsername() {
        // Given
        String username = "";
        String email = "valid@example.com";
        String password = "validpassword";

        // When
        ServiceResult<Void> result = authService.validateRegistrationData(username, email, password);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("用户名不能为空", result.getMessage());
    }

    @Test
    @DisplayName("验证注册数据 - 邮箱格式无效")
    void testValidateRegistrationDataInvalidEmail() {
        // Given
        String username = "validuser";
        String email = "invalidemail";
        String password = "validpassword";

        // When
        ServiceResult<Void> result = authService.validateRegistrationData(username, email, password);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("邮箱格式无效", result.getMessage());
    }

    @Test
    @DisplayName("验证注册数据 - 密码太短")
    void testValidateRegistrationDataShortPassword() {
        // Given
        String username = "validuser";
        String email = "valid@example.com";
        String password = "123";

        // When
        ServiceResult<Void> result = authService.validateRegistrationData(username, email, password);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("密码长度不能少于6位", result.getMessage());
    }

    @Test
    @DisplayName("用户登录 - 成功案例")
    void testLoginSuccess() {
        // Given
        String username = "testuser";
        String password = "password123";

        // When
        ServiceResult<LoginResultDTO> result = authService.login(username, password);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("登录成功", result.getMessage());

        LoginResultDTO loginResult = result.getData();
        assertNotNull(loginResult);
        assertNotNull(loginResult.getToken());
        assertEquals("Bearer", loginResult.getTokenType());
        assertEquals(7200L, loginResult.getExpiresIn());
        assertNotNull(loginResult.getUser());
    }

    @Test
    @DisplayName("用户登出 - 成功案例")
    void testLogoutSuccess() {
        // Given
        String token = "valid-jwt-token";

        // When
        ServiceResult<Void> result = authService.logout(token);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("登出成功", result.getMessage());
    }

    @Test
    @DisplayName("刷新Token - 成功案例")
    void testRefreshTokenSuccess() {
        // Given
        String token = "valid-jwt-token";

        // When
        ServiceResult<LoginResultDTO> result = authService.refreshToken(token);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Token刷新成功", result.getMessage());

        LoginResultDTO loginResult = result.getData();
        assertNotNull(loginResult);
        assertNotNull(loginResult.getToken());
        assertTrue(loginResult.getToken().startsWith("refreshed-token-"));
        assertEquals("Bearer", loginResult.getTokenType());
        assertEquals(7200L, loginResult.getExpiresIn());
    }

    @Test
    @DisplayName("刷新Token - Token为空")
    void testRefreshTokenEmpty() {
        // Given
        String token = "";

        // When
        ServiceResult<LoginResultDTO> result = authService.refreshToken(token);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Token为空", result.getMessage());
    }

    @Test
    @DisplayName("验证Token - 成功案例")
    void testValidateTokenSuccess() {
        // Given
        String token = "valid-jwt-token";

        // When
        ServiceResult<UserDTO> result = authService.validateToken(token);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Token验证成功", result.getMessage());

        UserDTO user = result.getData();
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
    }

    @Test
    @DisplayName("验证Token - 无效Token")
    void testValidateTokenInvalid() {
        // Given
        String token = "invalid-token";

        // When
        ServiceResult<UserDTO> result = authService.validateToken(token);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Token无效", result.getMessage());
    }

    @Test
    @DisplayName("从Token提取用户ID - 成功案例")
    void testExtractUserIdFromTokenSuccess() {
        // Given
        String token = "valid-jwt-token";

        // When
        ServiceResult<Long> result = authService.extractUserIdFromToken(token);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("用户ID提取成功", result.getMessage());
        assertEquals(1L, result.getData());
    }

    @Test
    @DisplayName("检查Token是否过期 - 未过期")
    void testIsTokenExpiredNotExpired() {
        // Given
        String token = "valid-token";

        // When
        ServiceResult<Boolean> result = authService.isTokenExpired(token);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertFalse(result.getData());
        assertEquals("Token未过期", result.getMessage());
    }

    @Test
    @DisplayName("检查Token是否过期 - 已过期")
    void testIsTokenExpiredExpired() {
        // Given
        String token = "expired-token";

        // When
        ServiceResult<Boolean> result = authService.isTokenExpired(token);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        assertEquals("Token已过期", result.getMessage());
    }

    @Test
    @DisplayName("修改密码 - 成功案例")
    void testChangePasswordSuccess() {
        // Given
        Long userId = 1L;
        String oldPassword = "oldpassword";
        String newPassword = "newpassword123";

        // When
        ServiceResult<Void> result = authService.changePassword(userId, oldPassword, newPassword);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("密码修改成功", result.getMessage());
    }

    @Test
    @DisplayName("修改密码 - 原密码错误")
    void testChangePasswordWrongOldPassword() {
        // Given
        Long userId = 1L;
        String oldPassword = "wrongpassword";
        String newPassword = "newpassword123";

        // When
        ServiceResult<Void> result = authService.changePassword(userId, oldPassword, newPassword);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("原密码不正确", result.getMessage());
    }

    @Test
    @DisplayName("验证密码强度 - 成功案例")
    void testValidatePasswordStrengthSuccess() {
        // Given
        String password = "strongpassword123";

        // When
        ServiceResult<Void> result = authService.validatePasswordStrength(password);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("密码强度验证通过", result.getMessage());
    }

    @Test
    @DisplayName("验证密码强度 - 密码太短")
    void testValidatePasswordStrengthTooShort() {
        // Given
        String password = "1234567";

        // When
        ServiceResult<Void> result = authService.validatePasswordStrength(password);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("密码长度不能少于8位", result.getMessage());
    }

    @Test
    @DisplayName("检查账户状态 - 成功案例")
    void testCheckAccountStatusSuccess() {
        // Given
        Long userId = 1L;

        // When
        ServiceResult<String> result = authService.checkAccountStatus(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("账户状态正常", result.getMessage());
        assertEquals("ACTIVE", result.getData());
    }

    @Test
    @DisplayName("记录登录尝试 - 成功案例")
    void testRecordLoginAttemptSuccess() {
        // Given
        String username = "testuser";
        boolean success = true;
        String ipAddress = "192.168.1.100";
        String userAgent = "Mozilla/5.0";

        // When
        ServiceResult<Void> result = authService.recordLoginAttempt(username, success, ipAddress, userAgent);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("登录尝试已记录", result.getMessage());
    }

    @Test
    @DisplayName("检查登录频率限制 - 未限制")
    void testCheckLoginRateLimitNotLimited() {
        // Given
        String username = "testuser";
        String ipAddress = "192.168.1.100";

        // When
        ServiceResult<Boolean> result = authService.checkLoginRateLimit(username, ipAddress);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertFalse(result.getData());
        assertEquals("未达到频率限制", result.getMessage());
    }
}
