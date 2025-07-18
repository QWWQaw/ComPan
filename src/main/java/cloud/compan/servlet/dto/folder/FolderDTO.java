package cloud.compan.servlet.dto.folder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

/**
 * 文件夹创建请求DTO
 */
public class FolderCreateDTO {
    @NotBlank(message = "文件夹名称不能为空")
    private String folderName;

    private Long parentId; // 父文件夹ID，null表示在根目录创建

    // 构造函数
    public FolderCreateDTO() {}

    public FolderCreateDTO(String folderName, Long parentId) {
        this.folderName = folderName;
        this.parentId = parentId;
    }

    // Getters and Setters
    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    // 验证方法
    public boolean isValid() {
        return folderName != null && !folderName.trim().isEmpty();
    }
}

/**
 * 文件夹信息DTO
 */
class FolderInfoDTO {
    private Long folderId;
    private String folderName;
    private Long parentId;
    private String parentName; // 父文件夹名称
    private Long userId;
    private Long fileCount; // 文件数量
    private Long subFolderCount; // 子文件夹数量
    private Long totalSize; // 文件夹总大小
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String breadcrumbPath; // 面包屑路径

    // 构造函数
    public FolderInfoDTO() {}

    // Getters and Setters
    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }

    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getFileCount() { return fileCount; }
    public void setFileCount(Long fileCount) { this.fileCount = fileCount; }

    public Long getSubFolderCount() { return subFolderCount; }
    public void setSubFolderCount(Long subFolderCount) { this.subFolderCount = subFolderCount; }

    public Long getTotalSize() { return totalSize; }
    public void setTotalSize(Long totalSize) { this.totalSize = totalSize; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getBreadcrumbPath() { return breadcrumbPath; }
    public void setBreadcrumbPath(String breadcrumbPath) { this.breadcrumbPath = breadcrumbPath; }

    /**
     * 格式化文件夹大小
     */
    public String getFormattedTotalSize() {
        if (totalSize == null) return "0 B";

        String[] units = {"B", "KB", "MB", "GB", "TB"};
        long size = totalSize;
        int unitIndex = 0;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return size + " " + units[unitIndex];
    }
}

/**
 * 文件夹重命名请求DTO
 */
class FolderRenameDTO {
    @NotNull(message = "文件夹ID不能为空")
    private Long folderId;

    @NotBlank(message = "新文件夹名称不能为空")
    private String newFolderName;

    // 构造函数
    public FolderRenameDTO() {}

    public FolderRenameDTO(Long folderId, String newFolderName) {
        this.folderId = folderId;
        this.newFolderName = newFolderName;
    }

    // Getters and Setters
    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }

    public String getNewFolderName() { return newFolderName; }
    public void setNewFolderName(String newFolderName) { this.newFolderName = newFolderName; }

    // 验证方法
    public boolean isValid() {
        return folderId != null && newFolderName != null && !newFolderName.trim().isEmpty();
    }
}

/**
 * 文件夹移动请求DTO
 */
class FolderMoveDTO {
    @NotNull(message = "文件夹ID不能为空")
    private Long folderId;

    private Long targetParentId; // 目标父文件夹ID，null表示移动到根目录

    // 构造函数
    public FolderMoveDTO() {}

    public FolderMoveDTO(Long folderId, Long targetParentId) {
        this.folderId = folderId;
        this.targetParentId = targetParentId;
    }

    // Getters and Setters
    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }

    public Long getTargetParentId() { return targetParentId; }
    public void setTargetParentId(Long targetParentId) { this.targetParentId = targetParentId; }

    // 验证方法
    public boolean isValid() {
        return folderId != null;
    }
}
