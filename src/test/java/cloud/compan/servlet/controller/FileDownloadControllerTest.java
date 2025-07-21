package cloud.compan.servlet.controller;

import cloud.compan.servlet.service.transfer.StorageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import cloud.compan.servlet.annotations.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileDownloadControllerTest {

    @Test
    void testDownloadFileSuccess() throws IOException {
        // 准备测试数据
        String fileId = "test_file.txt";
        byte[] fileContent = "Test file content".getBytes();

        // 模拟存储服务
        StorageService storageService = mock(StorageService.class);
        when(storageService.retrieveFile(fileId))
                .thenReturn(new ByteArrayInputStream(fileContent));

        // 创建控制器
        FileDownloadController controller = new FileDownloadController(storageService);

        // 创建模拟响应
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 调用下载方法
        controller.downloadFile(fileId, response);

        // 验证响应
        assertEquals(200, response.getStatus());
        assertEquals("application/octet-stream", response.getContentType());
        assertEquals("attachment; filename=\"test_file.txt\"", response.getHeader("Content-Disposition"));
        assertArrayEquals(fileContent, response.getContentAsByteArray());
    }

    @Test
    void testDownloadFileNotFound() {
        // 模拟存储服务
        StorageService storageService = mock(StorageService.class);
        when(storageService.retrieveFile("non_existent_file.txt"))
                .thenReturn(null);

        // 创建控制器
        FileDownloadController controller = new FileDownloadController(storageService);

        // 创建模拟响应
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 调用下载方法
        controller.downloadFile("non_existent_file.txt", response);

        // 验证响应
        assertEquals(404, response.getStatus());
        assertEquals("File not found", response.getErrorMessage());
    }

    @Test
    void testDownloadFileError() {
        // 模拟存储服务
        StorageService storageService = mock(StorageService.class);
        when(storageService.retrieveFile("error_file.txt"))
                .thenThrow(new RuntimeException("File access error"));

        // 创建控制器
        FileDownloadController controller = new FileDownloadController(storageService);

        // 创建模拟响应
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 调用下载方法
        controller.downloadFile("error_file.txt", response);

        // 验证响应状态码
        assertEquals(500, response.getStatus());

        // 关键修改：更新错误消息断言
        assertEquals("文件访问错误: File access error", response.getErrorMessage());
    }
}
