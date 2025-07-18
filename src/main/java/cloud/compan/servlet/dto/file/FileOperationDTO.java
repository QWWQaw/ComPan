package cloud.compan.servlet.dto.file;

/**
 * 文件重命名请求DTO
 */
class FileRenameDTO {
    private Long fileId;
    private String newFileName;

    // 构造函数
    public FileRenameDTO() {}

    public FileRenameDTO(Long fileId, String newFileName) {
        this.fileId = fileId;
        this.newFileName = newFileName;
    }

    // Getters and Setters
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public String getNewFileName() { return newFileName; }
    public void setNewFileName(String newFileName) { this.newFileName = newFileName; }

    // 验证方法
    public boolean isValid() {
        return fileId != null && newFileName != null && !newFileName.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "FileRenameDTO{" +
                "fileId=" + fileId +
                ", newFileName='" + newFileName + '\'' +
                '}';
    }
}

/**
 * 文件移动请求DTO
 */
class FileMoveDTO {
    private Long fileId;
    private Long targetFolderId;

    // 构造函数
    public FileMoveDTO() {}

    public FileMoveDTO(Long fileId, Long targetFolderId) {
        this.fileId = fileId;
        this.targetFolderId = targetFolderId;
    }

    // Getters and Setters
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public Long getTargetFolderId() { return targetFolderId; }
    public void setTargetFolderId(Long targetFolderId) { this.targetFolderId = targetFolderId; }

    // 验证方法
    public boolean isValid() {
        return fileId != null;
    }
}

/**
 * 文件操作结果DTO
 */
class FileOperationResultDTO {
    private Long fileId;
    private String fileName;
    private String operation; // 操作类型：rename, move, delete, upload
    private boolean success;
    private String message;
    private Long oldFolderId; // 原文件夹ID（用于移动操作）
    private Long newFolderId; // 新文件夹ID（用于移动操作）
    private String oldFileName; // 原文件名（用于重命名操作）

    // 构造函数
    public FileOperationResultDTO() {}

    // 静态工厂方法
    public static FileOperationResultDTO success(String operation, Long fileId, String fileName, String message) {
        FileOperationResultDTO dto = new FileOperationResultDTO();
        dto.setOperation(operation);
        dto.setFileId(fileId);
        dto.setFileName(fileName);
        dto.setSuccess(true);
        dto.setMessage(message);
        return dto;
    }

    public static FileOperationResultDTO error(String operation, String message) {
        FileOperationResultDTO dto = new FileOperationResultDTO();
        dto.setOperation(operation);
        dto.setSuccess(false);
        dto.setMessage(message);
        return dto;
    }

    // Getters and Setters
    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getOldFolderId() { return oldFolderId; }
    public void setOldFolderId(Long oldFolderId) { this.oldFolderId = oldFolderId; }

    public Long getNewFolderId() { return newFolderId; }
    public void setNewFolderId(Long newFolderId) { this.newFolderId = newFolderId; }

    public String getOldFileName() { return oldFileName; }
    public void setOldFileName(String oldFileName) { this.oldFileName = oldFileName; }
}
