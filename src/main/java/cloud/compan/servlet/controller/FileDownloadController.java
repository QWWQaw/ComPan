package cloud.compan.servlet.controller;

import cloud.compan.servlet.service.transfer.StorageService;
import jakarta.servlet.http.HttpServletResponse;
import cloud.compan.servlet.annotations.Autowired;
import cloud.compan.servlet.annotations.GetMapping;
import cloud.compan.servlet.annotations.PathVariable;
import cloud.compan.servlet.annotations.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

@RestController
public class FileDownloadController {

    private final StorageService storageService;
    private static final Logger logger = LoggerFactory.getLogger(FileDownloadController.class);

    @Autowired
    public FileDownloadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping(path = "/api/files/{fileId}/download")
    public void downloadFile(@PathVariable String fileId, HttpServletResponse response) {
        InputStream fileStream = null;
        try {
            // 获取文件流 - 第31行
            fileStream = storageService.retrieveFile(fileId);

            if (fileStream == null) {
                logger.warn("文件不存在: {}", fileId);
                sendErrorResponse(response, 404, "File not found");
                return;
            }

            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileId + "\"");

            // 传输文件内容
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileStream.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, bytesRead);
            }
            response.getOutputStream().flush();
        } catch (Exception e) { // 捕获所有异常
            // 关键修改：添加更详细的错误日志
            logger.error("文件下载失败: fileId={}, error={}", fileId, e.getMessage(), e);
            sendErrorResponse(response, 500, "文件访问错误: " + e.getMessage());
        } finally {
            // 确保资源关闭
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    logger.warn("关闭文件流失败: {}", e.getMessage());
                }
            }
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) {
        try {
            response.sendError(status, message);
        } catch (IOException e) {
            // 记录错误日志
            System.err.println("Failed to send error response: " + e.getMessage());
        }
    }
}