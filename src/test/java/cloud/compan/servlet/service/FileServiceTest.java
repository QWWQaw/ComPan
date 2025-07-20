package cloud.compan.servlet.service;

import cloud.compan.servlet.web.SimpleTestBase;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.dto.SearchCriteria;
import cloud.compan.servlet.model.File;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FileService单元测试
 * 测试文件服务的功能
 */
public class FileServiceTest extends SimpleTestBase {
    
    @Test
    void testFileServiceInjection() {
        // 测试FileService注入
        FileService fileService = getService(FileService.class);
        assertInjected(fileService, "FileService应该被正确注入");
        
        // 验证服务不为空
        assertNotNull(fileService, "FileService实例不应该为空");
    }
    
    @Test
    void testFileServiceBasicMethods() {
        // 测试FileService的基本方法调用
        FileService fileService = getService(FileService.class);
        assertNotNull(fileService, "FileService应该被注入");
        
        // 测试获取用户文件列表
        SearchCriteria criteria = new SearchCriteria().page(1).size(10);
        ServiceResult<PageResultDTO<File>> result = fileService.getUserFiles(1L, criteria);
        assertNotNull(result, "应该返回ServiceResult");
        assertTrue(result.isSuccess(), "应该返回成功结果");
        
        System.out.println("FileService基本方法测试通过");
    }
    
    @Test
    void testFileServiceCreateMethods() {
        // 测试创建文件方法
        FileService fileService = getService(FileService.class);
        assertNotNull(fileService, "FileService应该被注入");
        
        // 测试创建文件
        ServiceResult<File> createResult = fileService.uploadFile(1L, "test.txt", "text/plain", 1024L, "test content".getBytes());
        assertNotNull(createResult, "创建文件应该返回结果");
        assertTrue(createResult.isSuccess(), "创建文件应该成功");
        
        System.out.println("FileService创建方法测试通过");
    }
    
    @Test
    void testFileServiceUpdateMethods() {
        // 测试更新文件方法
        FileService fileService = getService(FileService.class);
        assertNotNull(fileService, "FileService应该被注入");
        
        // 测试更新文件
        ServiceResult<cloud.compan.servlet.dto.FileDTO> updateResult = fileService.renameFile(1L, "updated.txt", 1L);
        assertNotNull(updateResult, "更新文件应该返回结果");
        assertTrue(updateResult.isSuccess(), "更新文件应该成功");
        
        System.out.println("FileService更新方法测试通过");
    }
    
    @Test
    void testFileServiceDeleteMethods() {
        // 测试删除文件方法
        FileService fileService = getService(FileService.class);
        assertNotNull(fileService, "FileService应该被注入");
        
        // 测试删除文件
        ServiceResult<Boolean> deleteResult = fileService.deleteFile(1L, 1L);
        assertNotNull(deleteResult, "删除文件应该返回结果");
        assertTrue(deleteResult.isSuccess(), "删除文件应该成功");
        
        System.out.println("FileService删除方法测试通过");
    }
    
    @Test
    void testFileServiceImplementation() {
        // 测试FileService实现
        FileService fileService = getService(FileService.class);
        assertNotNull(fileService, "FileService应该被注入");
        
        // 验证服务实现
        assertTrue(fileService != null, "FileService应该被正确实现");
    }
} 