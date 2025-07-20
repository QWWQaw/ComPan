package cloud.compan.servlet.service;

import cloud.compan.servlet.web.SimpleTestBase;
import cloud.compan.servlet.dto.ServiceResult;
import cloud.compan.servlet.dto.PageResultDTO;
import cloud.compan.servlet.model.Folder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FolderService单元测试
 * 测试文件夹服务的功能
 */
public class FolderServiceTest extends SimpleTestBase {
    
    @Test
    void testFolderServiceInjection() {
        // 测试FolderService注入
        FolderService folderService = getService(FolderService.class);
        assertInjected(folderService, "FolderService应该被正确注入");
        
        // 验证服务不为空
        assertNotNull(folderService, "FolderService实例不应该为空");
    }
    
    @Test
    void testFolderServiceBasicMethods() {
        // 测试FolderService的基本方法调用
        FolderService folderService = getService(FolderService.class);
        assertNotNull(folderService, "FolderService应该被注入");
        
        // 测试获取用户文件夹列表
        ServiceResult<PageResultDTO<Folder>> result = folderService.getSubFolders(null, 1L, 1, 10, null, null);
        assertNotNull(result, "应该返回ServiceResult");
        assertTrue(result.isSuccess(), "应该返回成功结果");
        
        System.out.println("FolderService基本方法测试通过");
    }
    
    @Test
    void testFolderServiceCreateMethods() {
        // 测试创建文件夹方法
        FolderService folderService = getService(FolderService.class);
        assertNotNull(folderService, "FolderService应该被注入");
        
        // 测试创建文件夹
        ServiceResult<Folder> createResult = folderService.createFolder("test folder", 1L, 1L);
        assertNotNull(createResult, "创建文件夹应该返回结果");
        assertTrue(createResult.isSuccess(), "创建文件夹应该成功");
        
        System.out.println("FolderService创建方法测试通过");
    }
    
    @Test
    void testFolderServiceUpdateMethods() {
        // 测试更新文件夹方法
        FolderService folderService = getService(FolderService.class);
        assertNotNull(folderService, "FolderService应该被注入");
        
        // 测试更新文件夹
        ServiceResult<Folder> updateResult = folderService.renameFolder(1L, "updated folder", 1L);
        assertNotNull(updateResult, "更新文件夹应该返回结果");
        assertTrue(updateResult.isSuccess(), "更新文件夹应该成功");
        
        System.out.println("FolderService更新方法测试通过");
    }
    
    @Test
    void testFolderServiceDeleteMethods() {
        // 测试删除文件夹方法
        FolderService folderService = getService(FolderService.class);
        assertNotNull(folderService, "FolderService应该被注入");
        
        // 测试删除文件夹
        ServiceResult<Boolean> deleteResult = folderService.deleteFolder(1L, 1L);
        assertNotNull(deleteResult, "删除文件夹应该返回结果");
        assertTrue(deleteResult.isSuccess(), "删除文件夹应该成功");
        
        System.out.println("FolderService删除方法测试通过");
    }
    
    @Test
    void testFolderServiceImplementation() {
        // 测试FolderService实现
        FolderService folderService = getService(FolderService.class);
        assertNotNull(folderService, "FolderService应该被注入");
        
        // 验证服务实现
        assertTrue(folderService != null, "FolderService应该被正确实现");
    }
} 