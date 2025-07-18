package cloud.compan.servlet.dto.file;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 文件上传请求DTO
 * 用于Handler层接收文件上传请求参数
 */
public class FileUploadDTO {
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    private Long folderId; // 可选，上传到指定文件夹

    @NotNull(message = "文件内容不能为空")
    private Object filePart; // 实际的文件Part对象

    // 构造函数
    public FileUploadDTO() {}

    public FileUploadDTO(String fileName, Long folderId) {
        this.fileName = fileName;
        this.folderId = folderId;
    }

    // Getters and Setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }

    public Object getFilePart() { return filePart; }
    public void setFilePart(Object filePart) { this.filePart = filePart; }

    // 验证方法
    public boolean isValid() {
        return fileName != null && !fileName.trim().isEmpty() && filePart != null;
    }
}
