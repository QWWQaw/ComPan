package cloud.compan.servlet.service;

import cloud.compan.servlet.web.SimpleTestBase;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.SearchCriteria;
import cloud.compan.servlet.dto.StorageStatsDTO;
import cloud.compan.servlet.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

/**
 * 服务层测试
 * 使用简化的测试框架
 */
public class SimpleServiceTest extends SimpleTestBase {
    
    @Test
    void testUserServiceInjection() {
        // 测试UserService注入
        UserService userService = getService(UserService.class);
        assertInjected(userService, "UserService应该被正确注入");
        
        // 测试基本方法调用
        ServiceResult<User> result = userService.findById(1L);
        assertNotNull(result, "应该返回ServiceResult");
        assertTrue(result.isSuccess(), "应该返回成功结果");
    }
    
    @Test
    void testAuthServiceInjection() {
        // 测试AuthService注入
        AuthService authService = getService(AuthService.class);
        assertInjected(authService, "AuthService应该被正确注入");
        
        // 测试基本方法调用
        ServiceResult<Boolean> result = authService.isTokenExpired("test-token");
        assertNotNull(result, "应该返回ServiceResult");
        assertTrue(result.isSuccess(), "应该返回成功结果");
    }
    
    @Test
    void testFileServiceInjection() {
        // 测试FileService注入
        FileService fileService = getService(FileService.class);
        assertInjected(fileService, "FileService应该被正确注入");
        
        // 测试基本方法调用
        ServiceResult<StorageStatsDTO> result = fileService.getStorageStats(1L);
        assertNotNull(result, "应该返回ServiceResult");
        assertTrue(result.isSuccess(), "应该返回成功结果");
    }
    
    @Test
    void testFolderServiceInjection() {
        // 测试FolderService注入
        FolderService folderService = getService(FolderService.class);
        assertInjected(folderService, "FolderService应该被正确注入");
        
        // 测试基本方法调用
        ServiceResult<Map<String, Object>> result = folderService.getFolderStatistics(1L, 1L);
        assertNotNull(result, "应该返回ServiceResult");
        assertTrue(result.isSuccess(), "应该返回成功结果");
    }
} 