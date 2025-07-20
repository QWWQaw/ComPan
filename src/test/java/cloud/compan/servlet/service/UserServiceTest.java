package cloud.compan.servlet.service;

import cloud.compan.servlet.service.impl.UserServiceImpl;
import cloud.compan.servlet.dto.*;
import cloud.compan.servlet.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService 单元测试
 */
@DisplayName("用户服务测试")
public class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

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
        ServiceResult<User> result = userService.register(username, email, password);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("注册成功", result.getMessage());

        User user = result.getData();
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    @DisplayName("用户登录 - 成功案例")
    void testLoginSuccess() {
        // Given
        String username = "testuser";
        String password = "password123";

        // When
        ServiceResult<LoginResultDTO> result = userService.login(username, password);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("登录成功", result.getMessage());

        LoginResultDTO loginResult = result.getData();
        assertNotNull(loginResult);
        assertNotNull(loginResult.getToken());
        assertEquals("Bearer", loginResult.getTokenType());
        assertEquals(7200L, loginResult.getExpiresIn());

        UserDTO userDTO = loginResult.getUser();
        assertNotNull(userDTO);
        assertEquals(username, userDTO.getUsername());
    }

    @Test
    @DisplayName("根据用户名查找用户 - 成功案例")
    void testFindByUsernameSuccess() {
        // Given
        String username = "testuser";

        // When
        ServiceResult<User> result = userService.findByUsername(username);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("根据用户名查找用户成功", result.getMessage());

        User user = result.getData();
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertTrue(user.getEmail().contains(username));
    }

    @Test
    @DisplayName("根据用户名查找用户 - 用户名为空")
    void testFindByUsernameEmpty() {
        // Given
        String username = "";

        // When
        ServiceResult<User> result = userService.findByUsername(username);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("用户名不能为空", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("根据ID查找用户 - 成功案例")
    void testFindByIdSuccess() {
        // Given
        Long userId = 1L;

        // When
        ServiceResult<User> result = userService.findById(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("获取用户信息成功", result.getMessage());

        User user = result.getData();
        assertNotNull(user);
        assertEquals(userId, user.getUserId());
    }

    @Test
    @DisplayName("根据ID查找用户 - 无效ID")
    void testFindByIdInvalidId() {
        // Given
        Long userId = -1L;

        // When
        ServiceResult<User> result = userService.findById(userId);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("用户ID无效", result.getMessage());
    }

    @Test
    @DisplayName("更新用户档案 - 成功案例")
    void testUpdateProfileSuccess() {
        // Given
        Long userId = 1L;
        String newUsername = "newusername";
        String newEmail = "new@example.com";

        // When
        ServiceResult<User> result = userService.updateProfile(userId, newUsername, newEmail);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("用户信息更新成功", result.getMessage());

        User user = result.getData();
        assertNotNull(user);
        assertEquals(newUsername, user.getUsername());
        assertEquals(newEmail, user.getEmail());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    @DisplayName("修改密码 - 成功案例")
    void testChangePasswordSuccess() {
        // Given
        Long userId = 1L;
        String oldPassword = "oldpassword";
        String newPassword = "newpassword123";

        // When
        ServiceResult<Boolean> result = userService.changePassword(userId, oldPassword, newPassword);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("密码修改成功", result.getMessage());
        assertTrue(result.getData());
    }

    @Test
    @DisplayName("修改密码 - 原密码错误")
    void testChangePasswordWrongOldPassword() {
        // Given
        Long userId = 1L;
        String oldPassword = "wrongpassword";
        String newPassword = "newpassword123";

        // When
        ServiceResult<Boolean> result = userService.changePassword(userId, oldPassword, newPassword);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("原密码不正确", result.getMessage());
    }

    @Test
    @DisplayName("修改密码 - 新密码太短")
    void testChangePasswordShortNewPassword() {
        // Given
        Long userId = 1L;
        String oldPassword = "oldpassword";
        String newPassword = "123";

        // When
        ServiceResult<Boolean> result = userService.changePassword(userId, oldPassword, newPassword);

        // Then
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("新密码长度不能少于6位", result.getMessage());
    }

    @Test
    @DisplayName("获取存储统计 - 成功案例")
    void testGetStorageStatsSuccess() {
        // Given
        Long userId = 1L;

        // When
        ServiceResult<StorageStatsDTO> result = userService.getStorageStats(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("获取存储统计成功", result.getMessage());

        StorageStatsDTO stats = result.getData();
        assertNotNull(stats);
        assertTrue(stats.getStorageLimit() > 0);
        assertTrue(stats.getStorageUsed() >= 0);
        assertTrue(stats.getFileCount() >= 0);
        assertTrue(stats.getFolderCount() >= 0);
        assertEquals(20.0, stats.getUsagePercentage(), 0.1);
    }

    @Test
    @DisplayName("检查存储限制 - 空间足够")
    void testCheckStorageLimitSufficient() {
        // Given
        Long userId = 1L;
        Long additionalSize = 1000000L; // 1MB

        // When
        ServiceResult<Boolean> result = userService.checkStorageLimit(userId, additionalSize);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        assertEquals("存储空间检查通过", result.getMessage());
    }

    @Test
    @DisplayName("检查存储限制 - 空间不足")
    void testCheckStorageLimitInsufficient() {
        // Given
        Long userId = 1L;
        Long additionalSize = 10000000000L; // 10GB (超过可用空间)

        // When
        ServiceResult<Boolean> result = userService.checkStorageLimit(userId, additionalSize);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertFalse(result.getData());
        assertEquals("存储空间不足", result.getMessage());
    }

    @Test
    @DisplayName("删除用户 - 成功案例")
    void testDeleteByIdSuccess() {
        // Given
        Long userId = 1L;

        // When
        ServiceResult<Boolean> result = userService.deleteById(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("用户删除成功", result.getMessage());
        assertTrue(result.getData());
    }

    @Test
    @DisplayName("分页查询用户 - 成功案例")
    void testFindPageByCriteriaSuccess() {
        // Given
        SearchCriteria criteria = SearchCriteria.builder()
                .page(1)
                .size(10)
                .keyword("test")
                .sortBy("username")
                .sortDirection("ASC")
                .build();

        // When
        ServiceResult<PageResultDTO<User>> result = userService.findPageByCriteria(criteria);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("查询用户列表成功", result.getMessage());

        PageResultDTO<User> pageResult = result.getData();
        assertNotNull(pageResult);
        assertNotNull(pageResult.getContent());
        assertEquals(10, pageResult.getContent().size());
        assertEquals(100L, pageResult.getTotal());
        assertEquals(1, pageResult.getPage());
        assertEquals(10, pageResult.getSize());
    }
}
