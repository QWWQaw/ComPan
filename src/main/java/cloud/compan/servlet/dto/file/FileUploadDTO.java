package cloud.compan.servlet.dto.file;

/**
 * 文件上传请求DTO
 */
public class FileUploadDTO {
    private String fileName;
    private Long folderId;
    private String description;
    private Long fileSize;

    // 构造函数
    public FileUploadDTO() {}

    public FileUploadDTO(String fileName, Long folderId, String description) {
        this.fileName = fileName;
        this.folderId = folderId;
        this.description = description;
    }

    // Getters and Setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    // 验证方法
    public boolean isValid() {
        return fileName != null && !fileName.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "FileUploadDTO{" +
                "fileName='" + fileName + '\'' +
                ", folderId=" + folderId +
                ", description='" + description + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }
}
