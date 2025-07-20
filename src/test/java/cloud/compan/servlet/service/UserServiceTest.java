package cloud.compan.servlet.service;

import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.LoginResultDTO;
import cloud.compan.servlet.dto.StorageStatsDTO;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.dto.SearchCriteria;
import cloud.compan.servlet.model.User;
import cloud.compan.servlet.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * UserService测试类
 */
public class UserServiceTest {
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    void testRegister() {
        // 执行测试
        ServiceResult<User> result = userService.register("testuser", "test@example.com", "password123");
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("用户注册成功", result.getMessage());
    }
    
    @Test
    void testLogin() {
        // 执行测试
        ServiceResult<LoginResultDTO> result = userService.login("testuser", "password123");
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("用户登录成功", result.getMessage());
    }
    
    @Test
    void testFindById() {
        // 执行测试
        ServiceResult<User> result = userService.findById(1L);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("根据ID查找用户成功", result.getMessage());
    }
    
    @Test
    void testUpdateProfile() {
        // 执行测试
        ServiceResult<User> result = userService.updateProfile(1L, "updateduser", "updated@example.com");
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("用户信息更新成功", result.getMessage());
    }
    
    @Test
    void testChangePassword() {
        // 执行测试
        ServiceResult<Boolean> result = userService.changePassword(1L, "oldpass", "newpass");
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        assertEquals("密码修改成功", result.getMessage());
    }
    
    @Test
    void testResetPassword() {
        // 执行测试
        ServiceResult<Boolean> result = userService.resetPassword(1L, "newpassword");
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
        assertEquals("密码重置成功", result.getMessage());
    }
    
    @Test
    void testExistsByUsername() {
        // 执行测试
        ServiceResult<Boolean> result = userService.existsByUsername("testuser");
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("用户名不存在", result.getMessage());
    }
    
    @Test
    void testExistsByEmail() {
        // 执行测试
        ServiceResult<Boolean> result = userService.existsByEmail("test@example.com");
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("邮箱不存在", result.getMessage());
    }
    
    @Test
    void testGetStorageStats() {
        // 执行测试
        ServiceResult<StorageStatsDTO> result = userService.getStorageStats(1L);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("获取存储统计成功", result.getMessage());
    }
    
    @Test
    void testSearchUsers() {
        // 执行测试
        ServiceResult<PageResultDTO<User>> result = userService.searchUsers("test", 1, 10);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("搜索用户成功", result.getMessage());
    }
    
    @Test
    void testFindPageByCriteria() {
        // 准备测试数据
        SearchCriteria criteria = new SearchCriteria("test")
                .page(1)
                .size(10)
                .sortBy("username")
                .sortDirection("asc");
        
        // 执行测试
        ServiceResult<PageResultDTO<User>> result = userService.findPageByCriteria(criteria);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("分页查询用户成功", result.getMessage());
    }
} 